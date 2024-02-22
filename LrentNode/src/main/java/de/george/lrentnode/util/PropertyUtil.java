package de.george.lrentnode.util;

import java.io.IOException;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import de.george.g3utils.io.G3FileReaderVirtual;
import de.george.g3utils.io.G3FileWriterVirtual;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptorRegistery;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.eCEntityProxy;

public abstract class PropertyUtil {

	private static final ImmutableList<PropertyDescriptor<bCString>> TREASURE_SET_PROERTIES = ImmutableList.of(
			CD.gCInventory_PS.TreasureSet1, CD.gCInventory_PS.TreasureSet2, CD.gCInventory_PS.TreasureSet3, CD.gCInventory_PS.TreasureSet4,
			CD.gCInventory_PS.TreasureSet5);

	/**
	 * @param i Nummer des TreasureSets
	 * @return TreasureSet Property mit dem Namen TreasureSet{@code i}.
	 */
	public static PropertyDescriptor<bCString> GetTreasureSetProperty(int i) {
		return TREASURE_SET_PROERTIES.get(i - 1);
	}

	public static ImmutableList<PropertyDescriptor<bCString>> GetTreasureSetProperties() {
		return TREASURE_SET_PROERTIES;
	}

	public static <T extends G3Serializable> T clone(T source) {
		return fromBytes(toBytes(source), (Class<T>) source.getClass());
	}

	public static <T extends G3Serializable> ClassProperty<T> clone(ClassProperty<T> source) {
		G3FileWriterVirtual writer = new G3FileWriterVirtual(256);
		source.write(writer);
		try (G3FileReaderVirtual reader = new G3FileReaderVirtual(writer.getData())) {
			return new ClassProperty<>(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] toBytes(G3Serializable source) {
		G3FileWriterVirtual writer = new G3FileWriterVirtual(256);
		writer.write(source);
		return writer.getData();
	}

	public static <T extends G3Serializable> T fromBytes(byte[] source, Class<T> type) {
		try (G3FileReaderVirtual reader = new G3FileReaderVirtual(source)) {
			return reader.read(type, source.length);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compares two {@link G3Serializable}s in their binary representation.
	 *
	 * @param p1
	 * @param p2
	 * @return {@code true} if {@code p1} and {@code p2} are equal in serialized form.
	 */
	public static boolean compareSerializable(G3Serializable p1, G3Serializable p2) {
		if (p1 == null || p2 == null) {
			return p1 == p2;
		}

		if (p1.getClass() != p2.getClass()) {
			return false;
		}

		return Arrays.equals(toBytes(p1), toBytes(p2));
	}

	@FunctionalInterface
	public interface ClassPropertyVisitor {
		boolean visitProperty(ClassProperty<?> property, G3Class propertySet);
	}

	public static void visitProperties(eCEntity entity, ClassPropertyVisitor visitor) {
		for (G3Class propertySet : entity.getClasses()) {
			if (!processPropertySet(propertySet, visitor)) {
				return;
			}
			for (SubClassDescriptor desc : SubClassDescriptorRegistery.getInstance().lookupSubClasses(propertySet.getClassName()))
				for (G3Class nestedPropertySet : desc.getList(propertySet))
					if (!processPropertySet(nestedPropertySet, visitor))
						return;
		}
	}

	private static boolean processPropertySet(G3Class propertySet, ClassPropertyVisitor visitor) {
		for (ClassProperty<?> property : propertySet.properties()) {
			if (!visitor.visitProperty(property, propertySet)) {
				return false;
			}
		}
		return true;
	}

	@FunctionalInterface
	public interface ReferenceVisitor {
		boolean visitReference(eCEntityProxy value, ClassProperty<?> property, G3Class propertySet);
	}

	public static void visitTemplateReferences(eCEntity entity, ReferenceVisitor visitor) {
		visitProperties(entity, (property, propertySet) -> {
			if (property.getType().equals("eCTemplateEntityProxy")) {
				if (!visitor.visitReference((eCEntityProxy) property.getValue(), property, propertySet)) {
					return false;
				}
			} else if (property.getType().equals("bTObjArray<class eCTemplateEntityProxy>")) {
				bTObjArray_eCEntityProxy values = (bTObjArray_eCEntityProxy) property.getValue();
				for (eCEntityProxy value : values.getEntries()) {
					if (!visitor.visitReference(value, property, propertySet)) {
						return false;
					}
				}
			}
			return true;
		});
	}

	public static void visitEntityReferences(eCEntity entity, ReferenceVisitor visitor) {
		visitProperties(entity, (property, propertySet) -> {
			if (property.getType().equals("eCEntityProxy") || property.getType().equals("eCPropertySetProxy")) {
				if (!visitor.visitReference((eCEntityProxy) property.getValue(), property, propertySet)) {
					return false;
				}
			} else if (property.getType().equals("bTObjArray<class eCEntityProxy>")) {
				bTObjArray_eCEntityProxy values = (bTObjArray_eCEntityProxy) property.getValue();
				for (eCEntityProxy value : values.getEntries()) {
					if (!visitor.visitReference(value, property, propertySet)) {
						return false;
					}
				}
			}
			return true;
		});
	}
}
