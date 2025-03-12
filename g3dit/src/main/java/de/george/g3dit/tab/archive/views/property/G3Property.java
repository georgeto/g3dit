package de.george.g3dit.tab.archive.views.property;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.Property;
import com.teamunify.i18n.I;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptorRegistry;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bTObjArray_bTPropertyContainer;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.bTPropertyObject;
import de.george.lrentnode.properties.eCEntityProxy;

@SuppressWarnings("rawtypes")
public class G3Property extends AbstractProperty {
	private static final Logger logger = LoggerFactory.getLogger(G3Property.class);

	private String propertySet;
	private ClassProperty classProperty;

	private Property parent;
	private List<Property> subProperties = ImmutableList.of();
	private boolean displayErrorShown;

	public G3Property(ClassProperty classProperty) {
		this(classProperty, null);
	}

	public G3Property(ClassProperty classProperty, String propertySet) {
		this.propertySet = propertySet;
		this.classProperty = classProperty;
		if (classProperty.getValue() instanceof bTPropertyObject propertyObject) {
			subProperties = ImmutableList.copyOf(FluentIterable.from(propertyObject.getClazz().properties())
					.transform(p -> new G3Property(p, propertyObject.getClassName())));
		}
	}

	public ClassProperty getClassProperty() {
		return classProperty;
	}

	@Override
	public String getName() {
		return classProperty.getName();
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public String getShortDescription() {
		return classProperty.getType().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	@Override
	public String getCategory() {
		if (propertySet != null) {
			return PropertyDescriptorRegistry.getInstance().lookupProperty(propertySet, classProperty.getName())
					.map(PropertyDescriptor::getCategory).orElse("");
		} else {
			return "";
		}

	}

	@Override
	public Class<?> getType() {
		G3Serializable value = classProperty.getValue();
		if (value instanceof bTPropertyContainer) {
			return G3EnumWrapper.class;
		}

		if (value instanceof bTObjArray_bTPropertyContainer) {
			return G3EnumArrayWrapper.class;
		}

		if (value instanceof eCEntityProxy || value instanceof bCPropertyID) {
			boolean template = classProperty.getType().equals("eCTemplateEntityProxy");
			return template ? TemplateProxyMarker.class : EntityProxyMarker.class;
		}

		if (value == null) {
			return Object.class;
		}

		PropertyValueConverter<?, ?> converter = PropertyValueConverterRegistry.getInstance().getConverter(value.getClass());
		if (converter != null) {
			return converter.getValueType();
		}

		return value.getClass();
	}

	@Override
	public boolean isEditable() {
		return true;
	}

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
			if (!displayErrorShown) {
				logger.warn("Failed to display the value of the property '{}'.", getName(), e);
				TaskDialogs.error(null, I.tr("Error while displaying"),
						I.trf("Error while displaying the value of the property ''{0}''.", getName()));
				displayErrorShown = true;
			}
			throw new RuntimeException(e);
		}

		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.l2fprod.common.propertysheet.Property#setValue(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		try {
			// Reset display error shown state on set
			displayErrorShown = false;

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
			TaskDialogs.error(null, I.tr("Error while parsing"),
					I.trf("Error while parsing the entered value ''{0}'' for property ''{1}''.", value, getName()));
			displayErrorShown = true;
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
	}
}
