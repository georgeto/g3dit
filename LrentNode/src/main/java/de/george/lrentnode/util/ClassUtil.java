package de.george.lrentnode.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileReaderVirtual;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.ClassTypes;
import de.george.lrentnode.classes.DefaultClass;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gInt;

public class ClassUtil {
	private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	private static final byte[] SUB_CLASS_IDENTIFIER = Misc.asByte("010001010001");
	/**
	 * Doch nicht konstant
	 * <ul>
	 * <li>{@code 5300 5300}: aktuellste Version</li>
	 * <li>{@code 5200 5200}: kommt in manchen Templates/Materials vor</li>
	 * <li>{@code 5100 5100}: nur Guid?</li>
	 * <li>{@code 0100 0100}: kommt in Materials/... vor, nur wenn Datei kein Genomefile ist,
	 * gefolgt von String und Guid?</li>
	 * </ul>
	 */
	private static final byte[] TYPE_TO_VERSION_FILLER = Misc.asByte("0100005300");
	private static final int DEADCODE = 0xDEADC0DE;

	public static final class G3ClassHolder {
		private G3Class clazz;
		private int classVersion;

		public G3ClassHolder(G3Class clazz) {
			this(clazz, clazz.getClassVersion());
		}

		public G3ClassHolder(G3Class clazz, int classVersion) {
			this.clazz = clazz;
			this.classVersion = classVersion;
		}

		public G3Class getClazz() {
			return clazz;
		}

		public void setClazz(G3Class clazz) {
			this.clazz = clazz;
		}

		public int getClassVersion() {
			return classVersion;
		}

		public void setClassVersion(int classVersion) {
			this.classVersion = classVersion;
		}
	}

	public static G3ClassHolder readClass(G3FileReader reader) {
		int classVersion = reader.readShort(); // Version der Klasse, kann -1 sein, bei Subclasses,
												// dann kein DEC0ADDE
		reader.skip(SUB_CLASS_IDENTIFIER.length);
		String className = reader.readEntry();
		// bCPropertyObjectFactory::ReadObject: part of TYPE_TO_SIZE_FILLER is discarded
		reader.skip(TYPE_TO_VERSION_FILLER.length);
		G3Class clazz = ClassTypes.getClassInstance(className, reader);
		reader.skip(4); // Skip DEC0ADDE
		return new G3ClassHolder(clazz, classVersion);
	}

	public static G3Class readSubClass(G3FileReader reader) {
		reader.skip(SUB_CLASS_IDENTIFIER.length); // subClassIdentifier - always constant
		String className = reader.readEntry();
		reader.skip(TYPE_TO_VERSION_FILLER.length);
		return ClassTypes.getClassInstance(className, reader);
	}

	public static G3ClassHolder readTemplateClass(G3FileReader reader) {
		int classVersion = reader.readShort(); // Version der Klasse, kann -1 sein, bei Subclasses,
												// dann kein DEC0ADDE
		reader.readInt(); // Skip PreSize
		String preType = reader.readEntry();
		reader.skip(SUB_CLASS_IDENTIFIER.length); // subClassIdentifier - always constant
		String className = reader.readEntry();
		if (!className.equals(preType)) {
			logger.warn("First and second name of the template class do not match: {} - {}", preType, className);
		}
		// Header
		reader.skip(TYPE_TO_VERSION_FILLER.length);
		G3Class clazz = ClassTypes.getClassInstance(className, reader);
		reader.skip(4); // Skip DEC0ADDE
		return new G3ClassHolder(clazz, classVersion);
	}

	public static void skipClass(G3FileReader reader) {
		int classVersion = reader.readShort(); // Version der Klasse, kann -1 sein, bei Subclasses,
												// dann kein DEC0ADDE
		reader.skip(SUB_CLASS_IDENTIFIER.length);
		String className = reader.readEntry();
		// bCPropertyObjectFactory::ReadObject: part of TYPE_TO_SIZE_FILLER is discarded
		reader.skip(TYPE_TO_VERSION_FILLER.length);
		int innerClassVersion = reader.readShort(); // Class version
		int size = reader.readInt();

		reader.skip(size + 4); // Skip class + DEC0ADDE
	}

	public static void writeClass(G3FileWriter writer, G3Class clazz, int classVersion) {
		writer.writeUnsignedShort(classVersion);
		writeSubClass(writer, clazz);
		writer.writeInt(DEADCODE);
	}

	public static void writeClass(G3FileWriter writer, G3ClassHolder clazz) {
		writeClass(writer, clazz.getClazz(), clazz.getClassVersion());
	}

	public static void writeSubClass(G3FileWriter writer, G3Class clazz) {
		writer.write(SUB_CLASS_IDENTIFIER);
		writer.writeEntry(clazz.getClassName());
		writer.write(TYPE_TO_VERSION_FILLER);
		clazz.write(writer);
	}

	public static void writeTemplateClass(G3FileWriter writer, G3Class clazz, int classVersion) {
		writer.writeUnsignedShort(classVersion);

		int sizeOffset = writer.getSize();
		writer.writeInt(-1);
		writer.writeEntry(clazz.getClassName());

		writeSubClass(writer, clazz);

		writer.replaceInt(writer.getSize() - sizeOffset - 4 - 2, sizeOffset); // End Class Entry /
																				// DEADCODE-2

		writer.writeInt(DEADCODE);
	}

	public static void writeTemplateClass(G3FileWriter writer, G3ClassHolder clazz) {
		writeTemplateClass(writer, clazz.getClazz(), clazz.getClassVersion());
	}

	public static <C extends G3Class> C clone(C clazz) {
		G3FileWriterEx writer = new G3FileWriterEx();
		writeSubClass(writer, clazz);

		G3FileReaderEx reader = new G3FileReaderEx(writer.getData());
		reader.setStringtable(writer.getStringtable());
		return (C) readSubClass(reader);
	}

	public static List<ClassProperty<?>> readClassItems(int propertyTypeCount, int propertyClassVersion, G3FileReader reader) {
		List<ClassProperty<?>> properties = new ArrayList<>();
		for (int i = 0; i < propertyTypeCount; i++) {
			properties.add(new ClassProperty<>(reader));
		}
		return properties;
	}

	public static void write(List<ClassProperty<?>> properties, G3FileWriter writer) {
		for (ClassProperty<?> prop : properties) {
			prop.write(writer);
		}
	}

	/**
	 * createInventoryStack für node und lrentdat Dateien
	 *
	 * @param amount
	 * @param stackType gEStackType
	 * @param guid
	 * @param isTemplate
	 * @return
	 */
	public static G3Class createInventoryStack(int amount, int stackType, String guid, int quality) {
		return createInventoryStack(amount, stackType, guid, quality, false);
	}

	/**
	 * createInventoryStack für Templates
	 *
	 * @param amount
	 * @param guid
	 * @param isTemplate
	 * @return
	 */
	public static G3Class createInventoryStack(int amount, String guid, int quality) {
		return createInventoryStack(amount, 0, guid, quality, true);
	}

	private static G3Class createInventoryStack(int amount, int stackType, String guid, int quality, boolean isInsideTreasureSet) {
		G3Class stack = new DefaultClass("gCInventoryStack", 1);
		stack.addProperty(CD.gCInventoryStack.Amount, new gInt(amount));
		stack.addProperty(CD.gCInventoryStack.Quality, new gInt(quality));
		stack.addProperty(CD.gCInventoryStack.QuickSlot, new gInt(-1));
		stack.addProperty(CD.gCInventoryStack.SortIndex, new gInt(-1));
		if (!isInsideTreasureSet) {
			stack.addProperty(CD.gCInventoryStack.Generated, new bTPropertyContainer<>(stackType));
		} else {
			stack.addProperty(new ClassProperty<>("Generated", "bool", new gBool(stackType == 0 ? false : true)));
		}
		stack.addProperty(CD.gCInventoryStack.Learned, new gBool(false));
		stack.addProperty(CD.gCInventoryStack.ActivationCount, new gInt(0));
		if (!isInsideTreasureSet) {
			stack.addProperty(CD.gCInventoryStack.TransactionCount, new gInt(0));
		}
		stack.addProperty(CD.gCInventorySlot.Template, new eCEntityProxy(guid));
		stack.addProperty(CD.gCInventorySlot.Item, new eCEntityProxy(null));
		stack.addProperty(CD.gCInventorySlot.Slot, new bTPropertyContainer<>(gESlot.gESlot_None));
		return stack;
	}

	public static G3Class createInventorySlot(String templateGuid, String itemGuid, int slotType) {
		G3Class stack = new DefaultClass("gCInventorySlot", 1);
		stack.addProperty(CD.gCInventorySlot.Template, new eCEntityProxy(templateGuid));
		stack.addProperty(CD.gCInventorySlot.Item, new eCEntityProxy(itemGuid));
		stack.addProperty(CD.gCInventorySlot.Slot, new bTPropertyContainer<>(slotType));
		return stack;
	}

	@SuppressWarnings("unchecked")
	public static void setDefaultProperties(G3Class clazz) {
		Iterable<PropertyDescriptor<?>> properties = CD.getClassDescriptors()
				.filter(c -> ClassDescriptor.getName(c).equals(clazz.getClassName())).flatMap(ClassDescriptor::getAllProperties);

		for (PropertyDescriptor<?> propertyDesc : properties) {
			if (!propertyDesc.hasDefaultValue()) {
				continue;
			}

			G3Serializable defaultValue = PropertyUtil.clone(propertyDesc.getDefaultValue());
			if (!clazz.hasProperty(propertyDesc)) {
				clazz.addProperty((PropertyDescriptor<G3Serializable>) propertyDesc, defaultValue);
			} else {
				clazz.setPropertyData((PropertyDescriptor<G3Serializable>) propertyDesc, defaultValue);
			}
		}
	}

	public static G3Class createDefaultPropertySet(Class<? extends ClassDescriptor> descriptor) {
		return createDefaultPropertySet(ClassDescriptor.getName(descriptor));
	}

	public static G3Class createDefaultPropertySet(String propertySetName) {
		return readSubClass(new G3FileReaderVirtual(Constants.DEFAULT_PROPERTY_SETS.get(propertySetName)));
	}

	public static G3ClassContainer wrapPropertySet(G3Class propertySet) {
		G3ClassContainer container = new G3ClassContainer();
		container.addClass(propertySet, -1);
		return container;
	}
}
