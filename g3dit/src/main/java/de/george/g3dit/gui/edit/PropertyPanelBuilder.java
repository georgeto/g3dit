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

public interface PropertyPanelBuilder<B extends PropertyPanelBuilderContext<B, T>, T extends PropertyPanelBase<T>> {
	T addHeadline(String text);

	B add(String text);

	B add(PropertyHandler<?> handler);

	B add(PropertyDescriptor<?> descriptor);

	B add(PropertyDescriptor<?> descriptor, Function<G3ClassContainer, G3Class> propertySetExtractor);

	<C extends G3Class, V extends G3Serializable> B add(Class<? extends ClassDescriptor> propertySet, Function<C, V> getter,
			BiConsumer<C, V> setter, Supplier<V> defaultSupplier, Class<V> dataType, String dataTypeName);
}
