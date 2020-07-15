package de.george.lrentnode.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.TriConsumer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptorRegistry;
import de.george.lrentnode.util.ClassUtil.G3ClassHolder;

public class G3ClassContainer {

	private List<G3Class> classes;
	private List<Integer> classVersions;

	public G3ClassContainer() {
		classes = new ArrayList<>();
		classVersions = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Class> T getClass(String type) {
		for (G3Class clazz : classes) {
			if (clazz.typeEqual(type)) {
				return (T) clazz;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Class> T getClass(Set<String> types) {
		for (G3Class clazz : classes) {
			if (types.contains(clazz.getClassName())) {
				return (T) clazz;
			}
		}
		return null;
	}

	public <T extends G3Class> T getClass(Class<? extends ClassDescriptor> descriptor) {
		return getClass(ClassDescriptor.getName(descriptor));
	}

	public <T extends G3Class> T getClassOrDerived(Class<? extends ClassDescriptor> descriptor) {
		T clazz = getClass(descriptor);
		if (clazz == null) {
			clazz = getClass(PropertyDescriptorRegistry.getInstance().derivedPropertySetNames(descriptor));
		}
		return clazz;
	}

	public <T extends G3Class> Optional<T> getClassOptional(String type) {
		return Optional.ofNullable(getClass(type));
	}

	public <T extends G3Class> Optional<T> getClassOptional(Class<? extends ClassDescriptor> descriptor) {
		return Optional.ofNullable(getClass(descriptor));
	}

	public int getClassVersion(G3Class clazz) {
		return classVersions.get(classes.indexOf(clazz));
	}

	public List<G3Class> getClasses() {
		return Collections.unmodifiableList(classes);
	}

	public boolean addClass(G3ClassHolder holder) {
		return addClass(holder.getClazz(), holder.getClassVersion());
	}

	public boolean addClass(G3Class clazz, int classVersion) {
		if (hasClass(clazz.getClassName())) {
			return false;
		}

		classes.add(clazz);
		classVersions.add(classVersion);
		return true;
	}

	public boolean hasClass(String type) {
		return getClass(type) != null;
	}

	public boolean hasClass(Class<? extends ClassDescriptor> descriptor) {
		return getClass(descriptor) != null;
	}

	public int getClassCount() {
		return classes.size();
	}

	public void removeClass(Class<? extends ClassDescriptor> descriptor) {
		removeClass(ClassDescriptor.getName(descriptor));
	}

	public void removeClass(String type) {
		G3Class clazz = getClass(type);
		if (clazz != null) {
			int index = classes.indexOf(clazz);
			classes.remove(clazz);
			classVersions.remove(index);
		}
	}

	public boolean replaceClass(G3Class clazz) {
		for (int i = 0; i < classes.size(); i++) {
			if (classes.get(i).getClassName().equals(clazz.getClassName())) {
				classes.set(i, clazz);
				return true;
			}
		}
		return false;
	}

	protected void writeClasses(TriConsumer<G3FileWriter, G3Class, Integer> consumer, G3FileWriter writer) {
		writer.writeInt(getClassCount());
		for (int i = 0; i < getClassCount(); i++) {
			consumer.accept(writer, classes.get(i), classVersions.get(i));
		}
	}

	public <T extends G3Serializable> T getProperty(PropertyDescriptor<T> desc) {
		G3Class propertySet = getClassOrDerived(desc.getPropertySet());
		return Objects.requireNonNull(propertySet).property(desc);
	}

	public <T extends G3Serializable> Optional<T> getPropertyNoThrow(PropertyDescriptor<T> desc) {
		G3Class propertySet = getClassOrDerived(desc.getPropertySet());
		return Optional.ofNullable(propertySet).flatMap(ps -> ps.propertyNoThrow(desc));
	}

	public <T extends G3Serializable> T getProperty(PropertyDescriptor<T> desc, int occurence) {
		G3Class propertySet = getClassOrDerived(desc.getPropertySet());
		return Objects.requireNonNull(propertySet).property(desc, occurence);
	}

	public boolean hasProperty(PropertyDescriptor<?> desc) {
		G3Class propertySet = getClassOrDerived(desc.getPropertySet());
		return Optional.ofNullable(propertySet).map(ps -> ps.hasProperty(desc)).orElse(false);
	}
}
