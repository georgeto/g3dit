package de.george.g3dit.gui.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.google.common.base.Joiner;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanelDef.Builder;
import de.george.g3dit.gui.edit.adapter.DescriptorPropertyAdapter;
import de.george.g3dit.gui.edit.adapter.GetterSetterPropertyAdapter;
import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3dit.gui.edit.handler.LabelPropertyHandler;
import de.george.g3dit.gui.edit.handler.PropertyHandler;
import de.george.g3dit.gui.edit.handler.PropertyPanelHandlerFactory;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.bTObjArray_Generic;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("rawtypes")
public class PropertyPanelBase<T extends PropertyPanelBase<T>> implements PropertyPanelBuilder<Builder<T>, T> {
	private EditorContext ctx;
	private JPanel content;
	private List<EditProperty> properties;
	private boolean headline;

	public PropertyPanelBase(EditorContext ctx) {
		this(ctx, "fillx", "[grow]", null);
	}

	public PropertyPanelBase(EditorContext ctx, String layoutConstraints) {
		this(ctx, layoutConstraints, null, null);
	}

	public PropertyPanelBase(EditorContext ctx, String layoutConstraints, String colConstraints) {
		this(ctx, layoutConstraints, colConstraints, null);
	}

	public PropertyPanelBase(EditorContext ctx, String layoutConstraints, String colConstraints, String rowConstraints) {
		this.ctx = ctx;
		content = new JPanel(new MigLayout(join("ins 0", layoutConstraints), colConstraints, rowConstraints));
		properties = new ArrayList<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addHeadline(String text) {
		content.add(SwingUtils.createBoldLabel(text), join("spanx", properties.isEmpty() ? "" : "newline, gaptop u"));
		headline = true;
		return (T) this;
	}

	@Override
	public Builder<T> add(String text) {
		return PropertyPanelDef.with(this).handler(new LabelPropertyHandler(text)).constraints(newline());
	}

	@Override
	public Builder<T> add(PropertyHandler<?> handler) {
		return PropertyPanelDef.with(this).handler(handler).constraints(newline());
	}

	@Override
	public Builder<T> add(PropertyDescriptor<?> descriptor) {
		return add(descriptor, null);
	}

	@Override
	public Builder<T> add(PropertyDescriptor<?> descriptor, Function<G3ClassContainer, G3Class> propertySetExtractor) {
		return PropertyPanelDef.with(this).adapter(new DescriptorPropertyAdapter(descriptor, propertySetExtractor))
				.name(descriptor.getName()).constraints(newline());
	}

	@Override
	public <C extends G3Class, V extends G3Serializable> Builder<T> add(Class<? extends ClassDescriptor> propertySet,
			Function<C, V> getter, BiConsumer<C, V> setter, Supplier<V> defaultSupplier, Class<V> dataType, String dataTypeName) {
		return PropertyPanelDef.with(this)
				.adapter(new GetterSetterPropertyAdapter<>(propertySet, getter, setter, defaultSupplier, dataType, dataTypeName))
				.constraints(newline());
	}

	@SuppressWarnings("unchecked")
	public <C extends G3Class, V extends G3Serializable> Builder<T> add(Class<? extends ClassDescriptor> propertySet,
			Function<C, List<V>> getter, BiConsumer<C, List<V>> setter, Class<V> dataType) {
		return this.<C, bTObjArray_Generic<V>>add(propertySet, (ps) -> new bTObjArray_Generic<>(dataType, getter.apply(ps)),
				(ps, l) -> setter.accept(ps, l.getEntries()), () -> new bTObjArray_Generic<>(dataType), (Class) bTObjArray_Generic.class,
				"bTObjArray_Generic");
	}

	@SuppressWarnings("unchecked")
	protected T addInternal(PropertyPanelDef def) {

		PropertyHandler<?> handler;
		PropertyAdapter<?> adapter;
		if (def.hasAdapter()) {
			handler = PropertyPanelHandlerFactory.create(ctx, def);
			adapter = def.getAdapter();
		} else {
			handler = def.getHandler();
			adapter = null;
		}
		content.add(handler.getContent(), def.getConstraints());
		properties.add(new EditProperty(handler, adapter, def));
		return (T) this;
	}

	public void removeAll() {
		content.removeAll();
		content.revalidate();
		properties.clear();
	}

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("unchecked")
	public void load(G3ClassContainer container) {
		properties.forEach(p -> {
			boolean visible = !p.def.isHidden(container);
			p.handler.getContent().setVisible(visible);
			if (visible) {
				p.handler.load(container, p.adapter);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void save(G3ClassContainer container) {
		properties.forEach(p -> {
			if (!p.def.isHidden(container) && p.def.isEditable()) {
				p.handler.save(container, p.adapter);
			}
		});
	}

	protected String newline() {
		if (content.getComponentCount() == 0) {
			return "";
		}

		return headline ? "newline, gapleft i" : "newline";
	}

	private static final String join(String... constraints) {
		return Joiner.on(", ").skipNulls().join(constraints);
	}

	private static class EditProperty {
		public final PropertyHandler handler;
		public final PropertyAdapter adapter;
		public final PropertyPanelDef def;

		public EditProperty(PropertyHandler handler, PropertyAdapter adapter, PropertyPanelDef def) {
			this.handler = handler;
			this.adapter = adapter;
			this.def = def;
		}
	}
}
