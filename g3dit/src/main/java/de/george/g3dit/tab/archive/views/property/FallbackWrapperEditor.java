package de.george.g3dit.tab.archive.views.property;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;

public class FallbackWrapperEditor implements PropertyEditor {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	private final PropertyEditorRegistry registry;
	private PropertyEditor editor;

	private FallbackWrapper wrapper;

	public FallbackWrapperEditor(PropertyEditorRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setValue(Object value) {
		this.wrapper = (FallbackWrapper) value;
		this.editor = registry.createPropertyEditor(new AbstractProperty() {
			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public String getShortDescription() {
				return null;
			}

			@Override
			public Class<?> getType() {
				return wrapper.valueType;
			}

			@Override
			public boolean isEditable() {
				return true;
			}

			@Override
			public String getCategory() {
				return null;
			}

			@Override
			public void readFromObject(Object object) {}

			@Override
			public void writeToObject(Object object) {}
		});
		for (var listener : listeners.getPropertyChangeListeners())
			editor.addPropertyChangeListener(listener);
		editor.setValue(wrapper.value);
	}

	@Override
	public Object getValue() {
		wrapper.value = editor.getValue();
		for (var listener : listeners.getPropertyChangeListeners())
			editor.removePropertyChangeListener(listener);
		this.editor = null;
		FallbackWrapper result = wrapper;
		this.wrapper = null;
		return result;
	}

	@Override
	public boolean isPaintable() {
		return editor.isPaintable();
	}

	@Override
	public void paintValue(Graphics gfx, Rectangle box) {
		editor.paintValue(gfx, box);
	}

	@Override
	public String getJavaInitializationString() {
		return editor.getJavaInitializationString();
	}

	@Override
	public String getAsText() {
		return editor.getAsText();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		editor.setAsText(text);
	}

	@Override
	public String[] getTags() {
		return editor.getTags();
	}

	@Override
	public Component getCustomEditor() {
		return editor != null ? editor.getCustomEditor() : null;
	}

	@Override
	public boolean supportsCustomEditor() {
		return editor.supportsCustomEditor();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
		if (editor != null)
			editor.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
		if (editor != null)
			editor.removePropertyChangeListener(listener);
	}
}
