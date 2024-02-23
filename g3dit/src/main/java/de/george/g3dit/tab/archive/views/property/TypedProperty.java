package de.george.g3dit.tab.archive.views.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.AbstractProperty;

import de.george.lrentnode.properties.bTObjArray_bTPropertyContainer;
import de.george.lrentnode.properties.bTPropertyContainer;

@SuppressWarnings("rawtypes")
public abstract class TypedProperty extends AbstractProperty {
	private static final Logger logger = LoggerFactory.getLogger(TypedProperty.class);
	private final String name;
	private final Class<?> type;

	public TypedProperty(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public String getShortDescription() {
		return type.getSimpleName();
	}

	@Override
	public String getCategory() {
		return "";

	}

	@Override
	public Class<?> getType() {
		if (bTPropertyContainer.class.isAssignableFrom(type)) {
			return G3EnumWrapper.class;
		}

		if (bTObjArray_bTPropertyContainer.class.isAssignableFrom(type)) {
			return G3EnumArrayWrapper.class;
		}

		PropertyValueConverter<?, ?> converter = PropertyValueConverterRegistry.getInstance().getConverter(type);
		if (converter != null) {
			return converter.getValueType();
		}

		return type;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	/*@foff
	@Override
	public Object getValue() {
		G3Serializable value = classProperty.getValue();
		if (value instanceof bTPropertyContainer) {
			return new G3EnumWrapper(((bTPropertyContainer) value).getEnumValue(), G3Enums.byG3Type(classProperty.getType()));
		}

		if (value instanceof bTObjArray_bTPropertyContainer) {
			return new G3EnumArrayWrapper(((bTObjArray_bTPropertyContainer<?>) value).getNativeEntries(),
					G3Enums.byG3Type(classProperty.getType()));
		}

		if (value instanceof bTPropertyObject) {
			return null;
		}

		try {
			if (value != null) {
				PropertyValueConverter<G3Serializable, Object> converter = PropertyValueConverterRegistry.getInstance()
						.getConverter(value.getClass());
				if (converter != null) {
					return converter.convertTo(value);
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to display the value of the property '{}'.", getName(), e);
			TaskDialogs.error(null, I.tr("Error while displaying"),
					I.trf("Error while displaying the value of the property ''{0}''.", getName()));
			throw new RuntimeException(e);
		}

		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.l2fprod.common.propertysheet.Property#setValue(java.lang.Object)
	 */
	/* @foff
	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		try {
			if (classProperty.getValue() instanceof bTPropertyContainer) {
				((bTPropertyContainer<?>) classProperty.getValue()).setEnumValue(((G3EnumWrapper) value).getEnumValue());
				return;
			}

			if (classProperty.getValue() instanceof bTObjArray_bTPropertyContainer) {
				((bTObjArray_bTPropertyContainer<?>) classProperty.getValue())
						.setNativeEntries(((G3EnumArrayWrapper) value).getEnumValues());
				return;
			}

			if (classProperty.getValue() != null) {
				PropertyValueConverter<G3Serializable, Object> converter = PropertyValueConverterRegistry.getInstance()
						.getConverter(classProperty.getValue().getClass());
				if (converter != null) {
					classProperty.setValue(converter.convertFrom(classProperty.getValue(), value));
					return;
				}
			}

			classProperty.setValue((G3Serializable) value);
		} catch (Exception e) {
			logger.warn("Failed to parse the entered value '{}' for the property '{}'.", value, getName(), e);
			TaskDialogs.error(null, "Fehler beim Parsen",
					I.trf("Error while parsing the entered value ''{0}'' for property ''{1}''.", value, getName()));
		} finally {
			// Call afterwards, as it triggers the PropertySheetPanelListeners
			super.setValue(value);
		}
	}

	@Override
	public int hashCode() {
		return classProperty.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return classProperty == other;
	}

	@Override
	public String toString() {
		return "name=" + getName() + ", displayName=" + getDisplayName() + ", type=" + getType() + ", category=" + getCategory()
				+ ", editable=" + isEditable() + ", value=" + getValue();
	}

	@Override
	public Property getParentProperty() {
		return parent;
	}

	@Override
	public Property[] getSubProperties() {
		return subProperties.toArray(new Property[0]);
	}

	@Override
	public void readFromObject(Object object) {
		// We don't need this
	}

	@Override
	public void writeToObject(Object object) {
		// We don't need this
	}*/
}
