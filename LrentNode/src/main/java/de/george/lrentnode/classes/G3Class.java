package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.util.ClassUtil;

public abstract class G3Class {
	protected String className;

	protected int deadcodePosition;

	protected int propertyClassVersion;
	protected int propertyTypeCount;
	protected List<ClassProperty<?>> properties;

	protected int classVersion;

	public G3Class(String className, G3FileReader reader) {
		this.className = className;
		read(reader);
	}

	public G3Class(String className, int classVersion) {
		this.className = className;
		this.classVersion = classVersion;
		propertyClassVersion = 30; // 0x1E00
		properties = new ArrayList<>();
	}

	private void read(G3FileReader reader) {
		// @foff
		/*
		-> eCEntity::Write
		for (ProperySet ps : this->PropertySets)
			WriteShort(ps.GetVersion())
			bCAccessorPropertyObject::Write(ps)

		-- SUB_CLASS_IDENTIFIER --
		-> bCAccessorPropertyObject::Write
		01 00: WriteShort(1) => discard
		01	 : WriteBool(bCAccessorPropertyObject.HasPropertyObject()) => stop read if 00

		-> bCPropertyObjectSingleton::WriteObject
		01 00: WriteShort(1) => discard
		01	 : WriteBool(bCPropertyObjectBase.IsPersistable()) => stop read if 00
		XX XX: WriteString(bCPropertyObjectBase.GetClassName())

		-- TYPE_TO_SIZE_FILLER --
		-> bCPropertyObjectFactory::WriteObject
		01 00: WriteShort(1) => discard
		00 : WriteBool(bCPropertyObjectBase.IsRoot()) => discard
		53 00: WriteShort(0x53) => discard

		-> bTPropertyObject::Write
		53 00: bCPropertyObjectBase.Write() -> WriteShort(0x53) => compatibility with old files
		unsigned long size = bTPropertyObject::WriteData()
		XX XX XX XX : bTPropertyObject::Write -> WriteUnsignedLong(size)

		-> bTPropertyObject::WriteData
		53 00: WriteShort(0x53)
		XX XX XX XX: WriteLong(PropertyCount)
		WriteProperties()

		-> bCObjectBase::Write
		*/
		// @fon

		// bCPropertyObjectBase::Read
		int version = reader.readShort();

		// bTPropertyObject::Read
		deadcodePosition = reader.readInt() + reader.getPos();

		// bCPropertyObjectBase::ReadData
		// No write support for older versions needed -> Gothic 3 discards info anyway
		if (version == 0x01) {
			reader.readEntry();
		}

		if (version <= 0x51) {
			reader.readGUID();
		}

		// Properties
		propertyClassVersion = reader.readShort();
		propertyTypeCount = reader.readInt();
		properties = ClassUtil.readClassItems(propertyTypeCount, propertyClassVersion, reader);

		readPreClassVersion(reader);
		classVersion = reader.readShort();
		readPostClassVersion(reader);
	}

	protected void readPreClassVersion(G3FileReader reader) {};

	protected void readPostClassVersion(G3FileReader reader) {};

	public final void write(G3FileWriter writer) {
		writer.writeUnsignedShort(0x53);

		// Dummy für Size
		int sizeOffset = writer.getSize();
		writer.writeInt(-1);

		// Properties
		writer.writeUnsignedShort(propertyClassVersion).writeInt(properties.size());
		ClassUtil.write(properties, writer);

		// Klassenspezifische Daten vor Version
		writePreClassVersion(writer);

		writer.writeUnsignedShort(classVersion);

		// Klassenspezifische Daten nach Version
		writePostClassVersion(writer);

		// Size Dummys ersetzen
		writer.replaceInt(writer.getSize() - sizeOffset - 4, sizeOffset); // Size Until Dead Code
	}

	protected void writePreClassVersion(G3FileWriter writer) {};

	protected void writePostClassVersion(G3FileWriter writer) {};

	public String getClassName() {
		return className;
	}

	public boolean typeEqual(String type) {
		return className.equals(type);
	}

	public int getClassVersion() {
		return classVersion;
	}

	public G3Class addProperty(ClassProperty<?> property) {
		properties.add(property);
		return this;
	}

	public G3Class addProperty(ClassProperty<?> property, String predecessor) {
		if (predecessor == null) {
			properties.add(0, property);
		}

		for (int i = 0; i < properties.size(); i++) {
			if (properties.get(i).nameEqual(predecessor)) {
				properties.add(i + 1, property);
				return this;
			}
		}
		addProperty(property);
		return this;
	}

	public <T extends G3Serializable> G3Class addProperty(PropertyDescriptor<T> desc, T value) {
		properties.add(new ClassProperty<>(desc.getName(), desc.getDataTypeName(), value));
		return this;
	}

	public <T extends G3Serializable> G3Class addProperty(PropertyDescriptor<T> desc, T value, PropertyDescriptor<?> predecessor) {
		ClassProperty<T> property = new ClassProperty<>(desc.getName(), desc.getDataTypeName(), value);
		if (predecessor == null) {
			properties.add(0, property);
		}

		for (int i = 0; i < properties.size(); i++) {
			if (properties.get(i).nameEqual(predecessor.getName())) {
				properties.add(i + 1, property);
				return this;
			}
		}
		addProperty(property);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> Optional<T> propertyNoThrow(String name) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name)) {
				return Optional.ofNullable((T) property.getValue());
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> T property(String name) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name)) {
				return (T) property.getValue();
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(name));
	}

	public <T extends G3Serializable> Optional<T> propertyNoThrow(String name, Class<T> type) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name)) {
				return Optional.ofNullable(type.cast(property));
			}
		}
		return Optional.empty();
	}

	public <T extends G3Serializable> T property(String name, Class<T> type) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name)) {
				return type.cast(property.getValue());
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(name));
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> T property(String name, int occurence) {
		int skipped = 0;
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name) && occurence == ++skipped) {
				return (T) property.getValue();
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(name));
	}

	public <T extends G3Serializable> T property(String name, Class<T> type, int occurence) {
		int skipped = 0;
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(name) && occurence == ++skipped) {
				return type.cast(property.getValue());
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(name));
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> T property(PropertyDescriptor<T> desc) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(desc.getName())) {
				return (T) property.getValue();
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(desc.getName()));
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> Optional<T> propertyNoThrow(PropertyDescriptor<T> desc) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(desc.getName())) {
				return Optional.ofNullable((T) property.getValue());
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> T property(PropertyDescriptor<T> desc, int occurence) {
		int skipped = 0;
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(desc.getName()) && occurence == ++skipped) {
				return (T) property.getValue();
			}
		}
		throw new IllegalArgumentException(getNoSuchPropertyErrorMessage(desc.getName()));
	}

	public boolean hasProperty(PropertyDescriptor<?> desc) {
		return properties.stream().filter(p -> p.nameEqual(desc.getName())).findAny().isPresent();
	}

	public boolean hasProperty(String name) {
		return properties.stream().filter(p -> p.nameEqual(name)).findAny().isPresent();
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Serializable> void setPropertyData(PropertyDescriptor<T> desc, T data) {
		for (ClassProperty<?> property : properties) {
			if (property.nameEqual(desc.getName())) {
				((ClassProperty<T>) property).setValue(data);
				return;
			}
		}

		addProperty(desc, data);
	}

	@Override
	public String toString() {
		return getClassName();
	}

	public List<ClassProperty<?>> properties() {
		return properties;
	}

	public int propertyCount() {
		return properties.size();
	}

	private String getNoSuchPropertyErrorMessage(String name) {
		return "Die Klasse '" + className + "' hat keine Property '" + name + "'.";
	}

	public void onChildrenAvailable(long oldChangeTime, long newChangeTime, TemplateEntity creator) {
		// @foff
		/*
		 * - eCPointLight_PS: this.OnUpdatedWorldMatrix
		 * - eCVisualAnimation_PS: this.GetEntity().UpdateInternals(false)
		 * - gCAnchor_PS: this.RebuildInteractPoints()
		 * - gCInventory_PS: Stuff
		 * - gCParty_PS: Nothing...
		 */
		// @fon
		// TODO: Implement
	}
}
