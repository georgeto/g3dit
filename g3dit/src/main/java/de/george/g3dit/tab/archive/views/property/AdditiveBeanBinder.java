package de.george.g3dit.tab.archive.views.property;

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;

import javax.swing.UIManager;

import com.l2fprod.common.model.DefaultBeanInfoResolver;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Binds a bean object to a PropertySheet.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class AdditiveBeanBinder implements PropertyChangeListener {

	private Object object = null;
	private BeanInfo info = null;
	private PropertySheetPanel sheet = null;

	public AdditiveBeanBinder(Object object, PropertySheetPanel sheet) {
		this(object, sheet, new DefaultBeanInfoResolver().getBeanInfo(object));
	}

	public AdditiveBeanBinder(Object object, PropertySheetPanel sheet, BeanInfo info) {

		if (info == null) {
			throw new IllegalArgumentException(String.format("Cannot find %s for %s", BeanInfo.class.getSimpleName(), object.getClass()));
		}

		this.object = object;
		this.sheet = sheet;
		this.info = info;

		bind();
	}

	public void bind() {
		Arrays.stream(info.getPropertyDescriptors()).map(PropertyDescriptorAdapter::new).forEach(sheet::addProperty);
		sheet.readFromObject(object);
		sheet.addPropertySheetChangeListener(this);
	}

	public void unbind() {
		sheet.removePropertySheetChangeListener(this);
		for (Property prop : sheet.getProperties()) {
			if (prop instanceof PropertyDescriptorAdapter adapter) {
				if (Arrays.asList(info.getPropertyDescriptors()).contains(adapter.getDescriptor())) {
					sheet.removeProperty(prop);
				}
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		Property prop = (Property) event.getSource();

		if (prop instanceof PropertyDescriptorAdapter adapter) {
			if (Arrays.asList(info.getPropertyDescriptors()).contains(adapter.getDescriptor())) {
				try {
					prop.writeToObject(object);
				} catch (RuntimeException e) {

					if (e.getCause() instanceof PropertyVetoException) {
						UIManager.getLookAndFeel().provideErrorFeedback(sheet);
						prop.setValue(event.getOldValue());
					} else {
						throw e;
					}
				}
			}
		}
	}
}
