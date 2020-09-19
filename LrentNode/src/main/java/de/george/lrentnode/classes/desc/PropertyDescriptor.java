package de.george.lrentnode.classes.desc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.properties.bTArray;

public class PropertyDescriptor<T extends G3Serializable> {
	private static final Pattern ELEMENT_TYPE_PATTERN = Pattern.compile(".*?Array<\\s*(?:class|struct|enum)?\\s*(.+)>\\s*$");

	private final String name;
	private final Class<T> dataType;
	private final String dataTypeName;
	private final String elementDataTypeName;
	private final Class<? extends ClassDescriptor> propertySet;
	private final String category;
	private final T defaultValue;

	public PropertyDescriptor(String name, Class<?> dataType, String dataTypeName, String category,
			Class<? extends ClassDescriptor> propertySet) {
		this(name, dataType, dataTypeName, category, propertySet, null);
	}

	@SuppressWarnings("unchecked")
	public PropertyDescriptor(String name, Class<?> dataType, String dataTypeName, String category,
			Class<? extends ClassDescriptor> propertySet, T defaultValue) {
		this.name = name;
		// bCClassNameBase::UnMangle(), which is used to determine the data type name of
		// bTPropertyTypes, is broken, as it simply strips the first word of the demangled name, to
		// get rid of the enum/class/struct prefix. However, this also removes the unsigned prefix
		// for certain types.
		this.dataTypeName = dataTypeName.replaceFirst("^unsigned ", "");
		this.propertySet = propertySet;
		this.category = category;
		this.dataType = (Class<T>) dataType;
		if (bTArray.class.isAssignableFrom(dataType)) {
			Matcher matcher = ELEMENT_TYPE_PATTERN.matcher(dataTypeName);
			if (!matcher.matches()) {
				throw new IllegalArgumentException(dataTypeName);
			}
			elementDataTypeName = matcher.group(1).trim();
		} else {
			elementDataTypeName = null;
		}
		if (defaultValue != null) {
			this.defaultValue = defaultValue;
		} else {
			this.defaultValue = (T) PropertyInstantiator.getPropertyDefaultValue(name, dataTypeName).orElse(null);
		}
	}

	public String getName() {
		return name;
	}

	public Class<T> getDataType() {
		return dataType;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public boolean hasElementDataTypeName() {
		return elementDataTypeName != null;
	}

	public String getElementDataTypeName() {
		return elementDataTypeName;
	}

	public String getCategory() {
		return category;
	}

	public Class<? extends ClassDescriptor> getPropertySet() {
		return propertySet;
	}

	public String getPropertySetName() {
		return ClassDescriptor.getName(propertySet);
	}

	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		return dataTypeName + " " + ClassDescriptor.getName(propertySet) + "." + name;
	}
}
