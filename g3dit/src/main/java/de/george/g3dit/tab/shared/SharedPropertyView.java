package de.george.g3dit.tab.shared;

import java.beans.BeanInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataListener;

import com.ezware.dialog.task.TaskDialogs;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.CreatePropertyDialog;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.rpc.proto.RemoteProperty;
import de.george.g3dit.tab.archive.views.property.AdditiveBeanBinder;
import de.george.g3dit.tab.archive.views.property.EntityBeanInfo;
import de.george.g3dit.tab.archive.views.property.G3ClassProperty;
import de.george.g3dit.tab.archive.views.property.G3Property;
import de.george.g3dit.tab.archive.views.property.ParticleBeanInfo;
import de.george.g3dit.tab.archive.views.property.TemplateEntityBeanInfo;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ListUtil;
import de.george.g3dit.util.PropertySheetUtil;
import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.TriFunction;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCParticle_PS;
import de.george.lrentnode.classes.desc.SubClassDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptorRegistery;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.template.TemplateEntity;
import net.miginfocom.swing.MigLayout;

public class SharedPropertyView extends JPanel {
	private EditorContext ctx;

	private G3ClassContainer container;
	private JList<G3Class> classList;
	private String selectedClassName;

	private PropertySheetPanel sheet;
	private List<AdditiveBeanBinder> beanBinders = new ArrayList<>();

	public SharedPropertyView(EditorContext ctx) {
		this.ctx = ctx;

		setLayout(new MigLayout("fill", "[]12px[grow, fill]", "[][][grow, fill]"));
		LookAndFeelTweaks.tweak();
		sheet = PropertySheetUtil.createPropertySheetPanel();

		classList = new JList<>();
		classList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		classList.addListSelectionListener(e -> classSelectionChanged());

		JButton btnGetRemotePropertySet = new JButton(Icons.getImageIcon(Icons.Data.TABLE_IMPORT));
		btnGetRemotePropertySet.setToolTipText(I.tr("Read Remote PropertySet"));
		add(btnGetRemotePropertySet, "split 2");
		btnGetRemotePropertySet.addActionListener(e -> getRemotePropertySet());
		enableOnSelectedPropertySetAndIpcAvailable(btnGetRemotePropertySet);

		JButton btnSetRemotePropertySet = new JButton(Icons.getImageIcon(Icons.Data.TABLE_EXPORT));
		btnSetRemotePropertySet.setToolTipText(I.tr("Write Remote PropertySet"));
		add(btnSetRemotePropertySet);
		btnSetRemotePropertySet.addActionListener(e -> setRemotePropertySet());
		enableOnSelectedPropertySetAndIpcAvailable(btnSetRemotePropertySet);

		this.add(sheet, "grow, spany, wrap");

		JButton btnGetRemoteProperty = new JButton(Icons.getImageIcon(Icons.Data.CARD_IMPORT));
		btnGetRemoteProperty.setToolTipText(I.tr("Read remote property"));
		add(btnGetRemoteProperty, "split 4");
		btnGetRemoteProperty.addActionListener(e -> getRemoteProperty());
		enableOnSelectedPropertyAndIpcAvailable(btnGetRemoteProperty);

		JButton btnSetRemoteProperty = new JButton(Icons.getImageIcon(Icons.Data.CARD_EXPORT));
		btnSetRemoteProperty.setToolTipText(I.tr("Write remote property"));
		add(btnSetRemoteProperty);
		btnSetRemoteProperty.addActionListener(e -> setRemoteProperty());
		enableOnSelectedPropertyAndIpcAvailable(btnSetRemoteProperty);

		JButton btnAddProperty = new JButton(Icons.getImageIcon(Icons.Data.CARD_PLUS));
		btnAddProperty.setToolTipText(I.tr("Add property"));
		add(btnAddProperty);
		btnAddProperty.addActionListener(e -> addProperty());

		JButton btnRemProperty = new JButton(Icons.getImageIcon(Icons.Data.CARD_MINUS));
		btnRemProperty.setToolTipText(I.tr("Remove property"));
		add(btnRemProperty, "wrap");
		btnRemProperty.addActionListener(e -> removeProperty());
		enableOnSelectedProperty(btnRemProperty);

		this.add(new JScrollPane(classList), "wmin 170, growy");
	}

	public void load(G3ClassContainer entity) {
		container = entity;
		String lastSelectedClassName = selectedClassName;
		classList.setModel(new ClassContainerClassListModel(entity));

		if (lastSelectedClassName != null) {
			G3Class equivalentClass = entity.getClass(lastSelectedClassName);
			if (equivalentClass != null) {
				classList.setSelectedValue(equivalentClass, true);
				return;
			}
		}
		classList.setSelectedIndex(0);
		revalidate();
	}

	public void save(G3ClassContainer entity) {
		TableUtil.stopEditing(sheet.getTable());
	}

	public void cleanUp() {
		beanBinders.forEach(AdditiveBeanBinder::unbind);
		beanBinders.clear();
		classList.setModel(new EmptyListModel<>());
		sheet.setProperties(new Property[0]);
		ctx.getIpcMonitor().removeListeners(this, true, true);
	}

	private void enableOnSelectedPropertySetAndIpcAvailable(JComponent component) {
		Runnable updateEnable = ListUtil.enableOnEqual(classList, component, 1,
				() -> getSelectedPropertySet().isPresent() && ctx.getIpcMonitor().isAvailable());
		ctx.getIpcMonitor().addListener(this, ipcMon -> updateEnable.run(), true, false, true);
	}

	private void enableOnSelectedProperty(JComponent component) {
		TableUtil.enableOnEqual(sheet.getTable(), component, 1, () -> getSelectedProperty().isPresent());
	}

	private void enableOnSelectedPropertyAndIpcAvailable(JComponent component) {
		Runnable updateEnable = TableUtil.enableOnEqual(sheet.getTable(), component, 1,
				() -> getSelectedProperty().isPresent() && ctx.getIpcMonitor().isAvailable());
		ctx.getIpcMonitor().addListener(this, ipcMon -> updateEnable.run(), true, false, true);
	}

	private void classSelectionChanged() {
		TableUtil.stopEditing(sheet.getTable());

		sheet.setProperties(new Property[0]);

		G3Class value = classList.getSelectedValue();
		if (value != null) {
			beanBinders.forEach(AdditiveBeanBinder::unbind);
			beanBinders.clear();

			if (value instanceof HeaderClass) {
				G3ClassContainer container = ((HeaderClass) value).getContainer();
				BeanInfo beanInfo = null;
				if (container instanceof TemplateEntity) {
					beanInfo = new TemplateEntityBeanInfo();
				} else if (container instanceof eCEntity) {
					beanInfo = new EntityBeanInfo();
				}

				if (beanInfo != null) {
					beanBinders.add(new AdditiveBeanBinder(container, sheet, beanInfo));
				}
			} else {
				value.properties().forEach(c -> sheet.addProperty(new G3Property(c, value.getClassName())));

				for (SubClassDescriptor subClassDesc : SubClassDescriptorRegistery.getInstance().lookupSubClasses(value.getClassName())) {
					if (subClassDesc.isList()) {
						addSubClasses(subClassDesc.getElementName(), subClassDesc.getList(value));
					} else {
						addSubClass(subClassDesc.getName(), subClassDesc.get(value));
					}
				}

				if (value instanceof eCParticle_PS) {
					beanBinders.add(new AdditiveBeanBinder(value, sheet, new ParticleBeanInfo()));
				}
			}
		}

		selectedClassName = value != null ? value.getClassName() : null;
	}

	private void addSubClasses(String baseName, List<? extends G3Class> classes) {
		int count = 0;
		for (G3Class clazz : classes) {
			addSubClass(baseName + " " + ++count, clazz);
		}
	}

	private void addSubClass(String name, G3Class clazz) {
		sheet.addProperty(new G3ClassProperty(name, clazz));
	}

	private Optional<G3Class> getSelectedPropertySet() {
		return Optional.ofNullable(classList.getSelectedValue()).filter(propertySet -> !(propertySet instanceof HeaderClass));
	}

	private Optional<G3Property> getSelectedProperty() {
		int row = TableUtil.getSelectedRow(sheet.getTable());
		if (row != -1) {
			Item sheetElement = sheet.getTable().getSheetModel().getPropertySheetElement(row);
			if (sheetElement.isProperty() && sheetElement.getProperty() instanceof G3Property) {
				return Optional.of((G3Property) sheetElement.getProperty());
			}
		}
		return Optional.empty();
	}

	private void addProperty() {
		G3Class selectedClass = getSelectedPropertySet().orElse(null);
		if (selectedClass == null) {
			return;
		}

		CreatePropertyDialog dialog = new CreatePropertyDialog(ctx.getParentWindow(), selectedClass.getClassName());
		if (dialog.openAndWasSuccessful()) {
			if (!selectedClass.hasProperty(dialog.getPropertyName())) {
				Optional<G3Serializable> value = PropertyInstantiator.getPropertyDefaultValue(dialog.getPropertyName(),
						dialog.getPropertyType());
				if (value.isPresent()) {
					ClassProperty<G3Serializable> property = new ClassProperty<>(dialog.getPropertyName(), dialog.getPropertyType(),
							value.get());
					selectedClass.addProperty(property);
					classSelectionChanged();
				} else {
					TaskDialogs.error(dialog, "", I.trf("Failed to create instance of type ''{0}''.", dialog.getPropertyType()));
				}
			} else {
				TaskDialogs.error(dialog, "", I.trf("PropertySet already has a property named ''{0}''.", dialog.getPropertyName()));
			}
		}
	}

	private void removeProperty() {
		Optional<G3Property> selectedProperty = getSelectedProperty();
		if (selectedProperty.isPresent()) {
			ClassProperty<?> property = selectedProperty.get().getClassProperty();
			classList.getSelectedValue().properties().remove(property);
			classSelectionChanged();
		}
	}

	private void getRemotePropertySet() {
		updatePropertySet((entity, propertySet) -> RemoteProperty.getPropertySet(entity, propertySet.getClassName()));
	}

	private void setRemotePropertySet() {
		updatePropertySet(RemoteProperty::setPropertySet);
	}

	private void updatePropertySet(BiFunction<String, G3Class, CompletableFuture<G3Class>> remotePropertyProvider) {
		G3Class propertySet = getSelectedPropertySet().orElse(null);
		if (propertySet != null && container instanceof eCEntity entity) {
			try {
				G3Class result = remotePropertyProvider.apply(entity.getGuid(), propertySet).get(2, TimeUnit.SECONDS);
				entity.replaceClass(result);
				classSelectionChanged();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				TaskDialogs.showException(e);
			}
		}
	}

	private void getRemoteProperty() {
		updateProperty((entity, propertySet, property) -> RemoteProperty.getProperty(entity, propertySet, property.getName()));
	}

	private void setRemoteProperty() {
		updateProperty(RemoteProperty::setProperty);
	}

	private void updateProperty(
			TriFunction<String, String, ClassProperty<G3Serializable>, CompletableFuture<ClassProperty<G3Serializable>>> remotePropertyProvider) {
		G3Class propertySet = getSelectedPropertySet().orElse(null);
		ClassProperty<G3Serializable> property = getSelectedProperty().map(G3Property::getClassProperty).orElse(null);
		if (propertySet != null && property != null && container instanceof eCEntity entity) {
			try {
				ClassProperty<G3Serializable> result = remotePropertyProvider.apply(entity.getGuid(), propertySet.getClassName(), property)
						.get(2, TimeUnit.SECONDS);
				property.setValue(result.getValue());
				classSelectionChanged();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				TaskDialogs.showException(e);
			}
		}
	}

	private class ClassContainerClassListModel extends AbstractListModel<G3Class> {
		private G3ClassContainer container;
		private HeaderClass header;

		public ClassContainerClassListModel(G3ClassContainer container) {
			this.container = container;
			header = new HeaderClass(container);
		}

		@Override
		public int getSize() {
			return container.getClassCount() + 1;
		}

		@Override
		public G3Class getElementAt(int index) {
			if (index == 0) {
				return header;
			} else {
				return container.getClasses().get(index - 1);
			}
		}

	}

	private static class HeaderClass extends G3Class {
		private G3ClassContainer container;

		public HeaderClass(G3ClassContainer container) {
			super("Header", -1);
			this.container = container;
		}

		public G3ClassContainer getContainer() {
			return container;
		}

		@Override
		protected void readPostClassVersion(G3FileReader reader) {

		}

		@Override
		protected void writePostClassVersion(G3FileWriter writer) {

		}

	}

	// An empty ListMode, this is used when the UI changes to allow
	// the JList to be gc'ed.
	private static class EmptyListModel<T> implements ListModel<T>, Serializable {
		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public T getElementAt(int index) {
			return null;
		}

		@Override
		public void addListDataListener(ListDataListener l) {}

		@Override
		public void removeListDataListener(ListDataListener l) {}
	}
}
