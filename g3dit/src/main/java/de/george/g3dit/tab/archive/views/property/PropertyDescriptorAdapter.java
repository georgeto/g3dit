package de.george.g3dit.tab.archive.views.property;

import java.beans.PropertyDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.propertysheet.AbstractProperty;
import com.teamunify.i18n.I;

import de.george.g3utils.io.G3Serializable;

/**
 * PropertyDescriptorAdapter.<br>
 */
class PropertyDescriptorAdapter extends AbstractProperty {
	private static final Logger logger = LoggerFactory.getLogger(PropertyDescriptorAdapter.class);

	private PropertyDescriptor descriptor;

	public PropertyDescriptorAdapter() {
		super();
	}

	public PropertyDescriptorAdapter(PropertyDescriptor descriptor) {
		this();
		setDescriptor(descriptor);
	}

	public void setDescriptor(PropertyDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public PropertyDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public String getName() {
		return descriptor.getName();
	}

	@Override
	public String getDisplayName() {
		return descriptor.getDisplayName();
	}

	@Override
	public String getShortDescription() {
		return descriptor.getShortDescription();
	}

	@Override
	public Class<?> getType() {
		PropertyValueConverter<?, ?> converter = PropertyValueConverterRegistry.getInstance().getConverter(descriptor.getPropertyType());
		if (converter != null) {
			return converter.getValueType();
		}

		return descriptor.getPropertyType();
	}

	@Override
	public Object clone() {
		PropertyDescriptorAdapter clone = new PropertyDescriptorAdapter(descriptor);
		clone.setValue(getValue());
		return clone;
	}

	@Override
	public void readFromObject(Object object) {
		try {
			Object value = descriptor.getReadMethod().invoke(object, (Object[]) null);

			if (value != null) {
				PropertyValueConverter<G3Serializable, Object> converter = PropertyValueConverterRegistry.getInstance()
						.getConverter(value.getClass());
				if (converter != null) {
					setValue(converter.convertTo((G3Serializable) value));
					return;
				}
			}

			setValue(value);
		} catch (Exception e) {
			logger.warn("Failed to display the value of the property '{}'.", getName(), e);
			TaskDialogs.error(null, I.tr("Error while displaying"),
					I.trf("Error while displaying the value of the property ''{0}''.", getName()));
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeToObject(Object object) {
		try {
			PropertyValueConverter<G3Serializable, Object> converter = PropertyValueConverterRegistry.getInstance()
					.getConverter(descriptor.getPropertyType());
			if (converter != null) {
				G3Serializable oldValue = (G3Serializable) descriptor.getReadMethod().invoke(object, (Object[]) null);
				G3Serializable value = converter.convertFrom(oldValue, getValue());
				descriptor.getWriteMethod().invoke(object, new Object[] {value});
			} else {
				descriptor.getWriteMethod().invoke(object, new Object[] {getValue()});
			}
			readFromObject(object);
		} catch (Exception e) {
			logger.warn("Failed to parse the entered value '{}' for the property '{}'.", getValue(), getName(), e);
			TaskDialogs.error(null, I.tr("Error while parsing"),
					I.trf("Error while parsing the entered value ''{0}'' for property ''{1}''.", getValue(), getName()));
		}
	}

	@Override
	public boolean isEditable() {
		return descriptor.getWriteMethod() != null;
	}

	@Override
	public String getCategory() {
		if (descriptor instanceof ExtendedPropertyDescriptor) {
			return ((ExtendedPropertyDescriptor) descriptor).getCategory();
		} else {
			return null;
		}
	}

}
