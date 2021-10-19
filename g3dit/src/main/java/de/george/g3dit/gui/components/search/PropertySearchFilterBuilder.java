package de.george.g3dit.gui.components.search;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableMap;
import com.jidesoft.swing.AutoCompletion;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import de.george.g3dit.gui.complete.FunctionalComboBoxSearchable;
import de.george.g3dit.gui.renderer.FunctionalListCellRenderer;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.archive.views.property.G3Property;
import de.george.g3dit.util.PropertySheetUtil;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptorRegistery;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.properties.compare.CompareOperation;
import de.george.lrentnode.properties.compare.PropertyComparator;
import de.george.lrentnode.properties.compare.PropertyComparatorRegistry;
import de.george.lrentnode.util.PropertyUtil;
import net.miginfocom.swing.MigLayout;

@SearchFilterBuilderDesc(title = "Property")
public class PropertySearchFilterBuilder<T extends G3ClassContainer> implements SearchFilterBuilder<T> {
	private static final List<Class<ClassDescriptor>> NON_EMPTY_ENTITY_PROPERTY_SETS = CD.getClassDescriptors()
			.filter(CD.eCEntityPropertySet.class::isAssignableFrom).filter(d -> ClassDescriptor.getAllProperties(d).findAny().isPresent())
			.sortedBy(ClassDescriptor::getName).toImmutableList();

	private static final ImmutableMap<CompareOperation, String> RENDER_COMPARE_OPERATION = ImmutableMap.<CompareOperation, String>builder()
			.put(CompareOperation.Equals, "==").put(CompareOperation.NotEquals, "!=").put(CompareOperation.GreaterThanEquals, ">=")
			.put(CompareOperation.LessThanEquals, "<=").put(CompareOperation.Greater, ">").put(CompareOperation.Less, "<")
			.put(CompareOperation.EqualsIgnoreCase, "== (case insensitive)")
			.put(CompareOperation.NotEqualsIgnoreCase, "!= (case insensitive)").put(CompareOperation.Contains, "Contains")
			.put(CompareOperation.ContainsIgnoreCase, "Contains (case insensitive)").put(CompareOperation.StartsWith, "Starts With")
			.put(CompareOperation.StartsWithIgnoreCase, "Starts With (case insensitive)").put(CompareOperation.EndsWith, "Ends With")
			.put(CompareOperation.EndsWithIgnoreCase, "Ends With (case insensitive)").put(CompareOperation.Regex, "Regex")
			.put(CompareOperation.RegexIgnoreCase, "Regex (case insensitive)").put(CompareOperation.ContainsElement, "Contains").build();

	private JComponent comp;

	private JComboBoxExt<Class<ClassDescriptor>> cbPropertySet;
	private EventList<PropertyDescriptor<?>> properties;
	private JComboBoxExt<PropertyDescriptor<?>> cbProperty;
	private EventList<PropertyDescriptor<?>> subProperties;
	private JComboBoxExt<PropertyDescriptor<?>> cbSubProperty;
	private EventList<CompareOperation> operations;
	private JComboBoxExt<CompareOperation> cbOperation;

	private PropertyDescriptor<?> propertyDesc;
	private PropertyDescriptor<?> subPropertyDesc;
	private ClassProperty<G3Serializable> property;
	private PropertyComparator<G3Serializable> comparator;
	private CompareOperation operation;

	private PropertySheetPanel field;

	public PropertySearchFilterBuilder() {
		cbPropertySet = new JComboBoxExt<>(NON_EMPTY_ENTITY_PROPERTY_SETS, ClassDescriptor.class.getClass());
		cbPropertySet.setRenderer(new FunctionalListCellRenderer<>(ClassDescriptor::getName));
		new AutoCompletion(cbPropertySet, new FunctionalComboBoxSearchable<>(cbPropertySet, ClassDescriptor::getName));
		cbPropertySet.setSelectedIndex(-1);

		// TODO: Support bTPropertyObjects

		properties = new BasicEventList<>();
		cbProperty = createPropertyComboBox(properties);

		subProperties = new BasicEventList<>();
		cbSubProperty = createPropertyComboBox(subProperties);
		cbSubProperty.setVisible(false);

		operations = new BasicEventList<>();
		cbOperation = new JComboBoxExt<>(GlazedListsSwing.eventComboBoxModel(operations));
		cbOperation.setRenderer(new FunctionalListCellRenderer<>(RENDER_COMPARE_OPERATION::get));

		field = PropertySheetUtil.createPropertyField();

		cbPropertySet.addItemListener(e -> {
			fillProperties(cbPropertySet.getSelectedItem(), properties);
			cbProperty.setSelectedIndex(-1);
		});

		Supplier<CompareOperation> resetCompareOperation = () -> {
			comparator = null;

			// Reset search operation
			CompareOperation prevSelected = cbOperation.getSelectedItem();
			operations.clear();
			cbOperation.setSelectedIndex(-1);
			return prevSelected;
		};

		Runnable updateCompareOperation = () -> {
			CompareOperation prevSelected = resetCompareOperation.get();

			// Lookup comparator
			comparator = PropertyComparatorRegistry.getInstance().getComparator(getLeafPropertyDesc().getDataType());
			operations.addAll(comparator.getSupportedCompareOperations());
			// TODO: Allow nested PropertyComparator for containers
			cbOperation.setSelectedItem(operations.contains(prevSelected) ? prevSelected : CompareOperation.Equals);
		};

		Runnable resetPropertyInstance = () -> {
			property = null;
			field.setProperties(new Property[] {});
		};

		cbProperty.addItemListener(e -> {
			propertyDesc = cbProperty.getSelectedItem();
			if (propertyDesc == null) {
				cbProperty.setToolTipText(null);
				cbSubProperty.setVisible(false);
				resetCompareOperation.get();
				resetPropertyInstance.run();
				return;
			} else {
				// Update cbProperty tooltip text
				cbProperty.setToolTipText(propertyDesc.getDataTypeName());
			}

			// Sub class
			SubClassDescriptor subClassDesc = getSubClassDesc();
			if (subClassDesc != null) {
				fillProperties(subClassDesc.getSubPropertySet(), subProperties);
				cbSubProperty.setSelectedIndex(-1);
				cbSubProperty.setVisible(true);
			} else {
				cbSubProperty.setVisible(false);
				updateCompareOperation.run();
			}
		});

		cbSubProperty.addItemListener(e -> {
			subPropertyDesc = cbSubProperty.getSelectedItem();
			if (subPropertyDesc == null) {
				cbSubProperty.setToolTipText(null);
				resetCompareOperation.get();
				resetPropertyInstance.run();
				return;
			} else {
				// Update cbProperty tooltip text
				cbSubProperty.setToolTipText(subPropertyDesc.getDataTypeName());
				updateCompareOperation.run();
			}
		});

		cbOperation.addItemListener(e -> {
			operation = cbOperation.getSelectedItem();
			if (operation == null) {
				resetPropertyInstance.run();
				return;
			}

			PropertyDescriptor<?> leafPropertyDesc = getLeafPropertyDesc();

			// Initialize value editing field
			String valueTypeName = leafPropertyDesc.getDataTypeName();
			if (operation == CompareOperation.ContainsElement) {
				valueTypeName = leafPropertyDesc.getElementDataTypeName();
			}

			Optional<G3Serializable> value = PropertyInstantiator.getPropertyDefaultValue(leafPropertyDesc.getName(), valueTypeName);
			if (value.isPresent()) {
				// Newly selected property has a different value type than the previous
				if (property == null || !valueTypeName.equals(property.getType())) {
					property = new ClassProperty<>(leafPropertyDesc.getName(), leafPropertyDesc.getDataTypeName(), value.get());
					field.setProperties(new Property[] {new G3Property(property)});
				}
			} else {
				resetPropertyInstance.run();
				TaskDialogs.error(null, "",
						"Es konnte keine Instanz des Typs '" + leafPropertyDesc.getDataTypeName() + "' erstellt werden.");
			}
		});

		comp = new JPanel(new MigLayout("ins 0, fillx, hidemode 2"));
		comp.add(cbPropertySet);
		comp.add(cbProperty);
		comp.add(cbSubProperty);
		comp.add(cbOperation);
		int fieldHeight = (int) cbPropertySet.getPreferredSize().getHeight();
		field.getTable().setRowHeight(fieldHeight);
		comp.add(field.getTable(), "height " + fieldHeight + "!, pushx, growx");
	}

	private JComboBoxExt<PropertyDescriptor<?>> createPropertyComboBox(EventList<PropertyDescriptor<?>> properties) {
		JComboBoxExt<PropertyDescriptor<?>> cbProperty = new JComboBoxExt<>(
				GlazedListsSwing.eventComboBoxModel(new SortedList<>(properties, Comparator.comparing(PropertyDescriptor<?>::getName))));
		cbProperty.setRenderer(new FunctionalListCellRenderer<>(PropertyDescriptor<G3Serializable>::getName,
				PropertyDescriptor<G3Serializable>::getDataTypeName));
		new AutoCompletion(cbProperty, new FunctionalComboBoxSearchable<>(cbProperty, PropertyDescriptor::getName));
		return cbProperty;
	}

	private void fillProperties(Class<? extends ClassDescriptor> propertySet, List<PropertyDescriptor<?>> properties) {
		properties.clear();

		ClassDescriptor.getAllProperties(propertySet).filter(p -> !PropertyInstantiator.isPropertyObject(p.getDataTypeName()))
				.forEach(properties::add);

		SubClassDescriptorRegistery.getInstance().lookupSubClasses(propertySet).stream()
				.map(PropertySearchFilterBuilder::createSubClassProxy).forEach(properties::add);
	}

	@Override
	public JComponent getComponent() {
		return comp;
	}

	@Override
	public void initFocus() {
		cbPropertySet.requestFocusInWindow();
	}

	@Override
	public SearchFilter<T> buildFilter() {
		TableUtil.stopEditing(field.getTable());
		if (propertyDesc == null) {
			return new PropertySetSearchFilter<>(cbPropertySet.getSelectedItem());
		}
		return new PropertySearchFilter<>(getSubClassDesc(), getLeafPropertyDesc(), property, comparator, operation);
	}

	@Override
	public boolean loadFilter(SearchFilter<T> filter) {
		if (filter instanceof PropertySearchFilter<T> typedFilter) {
			PropertyDescriptor<?> propertyDesc = typedFilter.getPropertyDesc();
			if (propertyDesc != null) {
				cbPropertySet.setSelectedItem(propertyDesc.getPropertySet());
				if (typedFilter.getSubClassDescriptor() != null) {
					Optional<PropertyDescriptor<?>> subClassDesc = properties.stream()
							.filter(p -> p.getDataType() == SubClassDescriptorProxy.class)
							.filter(p -> ((SubClassDescriptorProxy) p.getDefaultValue()).descriptor == typedFilter.getSubClassDescriptor())
							.findFirst();

					if (!subClassDesc.isPresent()) {
						return true;
					}

					cbProperty.setSelectedItem(subClassDesc);
					cbSubProperty.setSelectedItem(propertyDesc);
				} else {
					cbProperty.setSelectedItem(propertyDesc);
				}

				cbOperation.setSelectedItem(typedFilter.getOperation());
				if (typedFilter.getProperty() != null) {
					property = PropertyUtil.clone(typedFilter.getProperty());
					field.setProperties(new Property[] {new G3Property(property)});
				}
			}
			return true;
		} else {
			return false;
		}

	}

	private static class SubClassDescriptorProxy implements G3Serializable {
		public final SubClassDescriptor descriptor;

		public SubClassDescriptorProxy(SubClassDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		public void read(G3FileReader reader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(G3FileWriter writer) {
			throw new UnsupportedOperationException();
		}
	}

	private static PropertyDescriptor<SubClassDescriptorProxy> createSubClassProxy(SubClassDescriptor subClassDesc) {
		return new PropertyDescriptor<>(subClassDesc.getName(), SubClassDescriptorProxy.class,
				ClassDescriptor.getName(subClassDesc.getSubPropertySet()), null, subClassDesc.getPropertySet(),
				new SubClassDescriptorProxy(subClassDesc));
	}

	private PropertyDescriptor<?> getLeafPropertyDesc() {
		if (propertyDesc == null) {
			return null;
		}

		return propertyDesc.getDataType() == SubClassDescriptorProxy.class ? subPropertyDesc : propertyDesc;
	}

	private SubClassDescriptor getSubClassDesc() {
		if (propertyDesc == null || propertyDesc.getDataType() != SubClassDescriptorProxy.class) {
			return null;
		}

		return ((SubClassDescriptorProxy) propertyDesc.getDefaultValue()).descriptor;
	}
}
