package de.george.g3dit.gui.edit;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.george.g3dit.gui.edit.handler.PropertyHandler;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;

@SuppressWarnings("unchecked")
public abstract class PropertyPanelBuilderContext<B extends PropertyPanelBuilderContext<B, T>, T extends PropertyPanelBase<T>>
		implements PropertyPanelBuilder<B, T> {
	protected final PropertyPanelBase<T> base;

	public PropertyPanelBuilderContext(PropertyPanelBase<T> base) {
		this.base = base;
	}

	@Override
	public T addHeadline(String text) {
		return done().addHeadline(text);
	}

	@Override
	public B add(String text) {
		return (B) done().add(text);
	}

	@Override
	public B add(PropertyHandler<?> handler) {
		return (B) done().add(handler);
	}

	@Override
	public B add(PropertyDescriptor<?> descriptor) {
		return (B) done().add(descriptor);
	}

	@Override
	public B add(PropertyDescriptor<?> descriptor, Function<G3ClassContainer, G3Class> propertySetExtractor) {
		return (B) done().add(descriptor, propertySetExtractor);
	}

	@Override
	public <C extends G3Class, V extends G3Serializable> B add(Class<? extends ClassDescriptor> propertySet, Function<C, V> getter,
			BiConsumer<C, V> setter, Supplier<V> defaultSupplier, Class<V> dataType, String dataTypeName) {
		return (B) done().add(propertySet, getter, setter, defaultSupplier, dataType, dataTypeName);
	}

	public T done() {
		return base.addInternal(b());
	}

	protected abstract PropertyPanelDef b();
}
