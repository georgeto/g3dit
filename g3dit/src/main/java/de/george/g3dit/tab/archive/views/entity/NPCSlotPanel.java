package de.george.g3dit.tab.archive.views.entity;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.AbstractSelectDialog;
import de.george.g3dit.gui.dialogs.ListSelectDialog;
import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog.TemplateSearchListener;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.archive.views.entity.dialogs.SlotDialog;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.Tooltips;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Pair;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.NPCUtil;
import net.miginfocom.swing.MigLayout;

public class NPCSlotPanel extends JPanel implements TemplateSearchListener {
	private int slotType;
	private eCEntity ownerEntity;

	private eCEntity slotEntity;

	private JTextField tfRefGuid, tfMaterialSwitch, tfMesh, tfName, tfChangeTime;
	private JButton btnEdit, btnTple, btnAdd, btnDelete;
	private JLabel lblChangeTime, lblRefGuid, lblMaterialSwitch, lblName, lblMesh;
	private JTextComponent lblError;

	private boolean error;
	private EditorArchiveTab ctx;

	public NPCSlotPanel(int inSlotType, EditorArchiveTab inEditor) {
		super(new MigLayout("", "[]10px[]", "25"));

		ctx = inEditor;

		slotType = inSlotType;

		lblName = new JLabel(I.tr("Name"));
		tfName = SwingUtils.createUndoTF();
		tfName.setEditable(false);

		lblMesh = new JLabel(I.tr("Mesh"));
		tfMesh = SwingUtils.createUndoTF();
		tfMesh.setEditable(false);

		lblMaterialSwitch = new JLabel(I.tr("MaterialSwitch"));
		tfMaterialSwitch = SwingUtils.createUndoTF();
		tfMaterialSwitch.setEditable(false);

		lblRefGuid = new JLabel(I.tr("Reference Guid"));
		tfRefGuid = SwingUtils.createUndoTF();
		tfRefGuid.setEditable(false);

		lblChangeTime = new JLabel(I.tr("ChangeTime"));
		lblChangeTime.setToolTipText(Tooltips.changeTime());
		tfChangeTime = SwingUtils.createUndoTF();
		tfChangeTime.setEditable(false);
		tfChangeTime.setToolTipText(Tooltips.changeTime());

		lblError = SwingUtils.createSelectableLabel();

		addComponents();

		btnEdit = new JButton(Icons.getImageIcon(Icons.Action.EDIT));
		btnEdit.setToolTipText(I.tr("Edit values"));
		add(btnEdit, LayoutUtils.sqrBtn("cell 2 0"));
		btnEdit.addActionListener(e -> handleEditManual());

		btnTple = new JButton(Icons.getImageIcon(Icons.Action.BOOK));
		btnTple.setToolTipText(I.tr("Load template"));
		add(btnTple, LayoutUtils.sqrBtn("cell 2 1"));
		btnTple.addActionListener(e -> handleLoadTemplate());

		// Platzhalter
		add(new JLabel(), LayoutUtils.sqrBtn("cell 2 2"));

		btnAdd = new JButton(Icons.getImageIcon(Icons.Action.ADD));
		btnAdd.setToolTipText(I.tr("Select entity for this slot"));
		add(btnAdd, LayoutUtils.sqrBtn("cell 2 3"));
		btnAdd.addActionListener(e -> handleAddSlot());

		btnDelete = new JButton(Icons.getImageIcon(Icons.Action.DELETE));
		btnDelete.setToolTipText(I.tr("Delete slot"));
		add(btnDelete, LayoutUtils.sqrBtn("cell 2 4"));
		btnDelete.addActionListener(e -> handleRemoveSlot());
	}

	private void addComponents() {
		remove(lblError);
		add(lblName, "cell 0 0");
		add(tfName, "cell 1 0, width 100:300:300");
		add(lblMesh, "cell 0 1");
		add(tfMesh, "cell 1 1, width 100:300:300");
		add(lblMaterialSwitch, "cell 0 2");
		add(tfMaterialSwitch, "cell 1 2, width 100:300:300");
		add(lblRefGuid, "cell 0 3");
		add(tfRefGuid, "cell 1 3, width 100:300:300");
		add(lblChangeTime, "cell 0 4");
		add(tfChangeTime, "cell 1 4, width 100:300:300");
		repaint(50);
	}

	private void removeComponents() {
		remove(lblName);
		remove(tfName);
		remove(lblMesh);
		remove(tfMesh);
		remove(lblMaterialSwitch);
		remove(tfMaterialSwitch);
		remove(lblRefGuid);
		remove(tfRefGuid);
		remove(lblChangeTime);
		remove(tfChangeTime);
		add(lblError, "cell 0 0, aligny center, spany, width 100:400:400");
		repaint(50);
	}

	@SuppressWarnings("unchecked")
	public void initValidation(ValidationGroup group) {
		tfName.setName(I.tr("Name"));
		group.add(tfName, EmtpyWarnValidator.INSTANCE);
		tfMesh.setName(I.tr("Mesh"));
		group.add(tfMesh, EmtpyWarnValidator.INSTANCE);
		tfMaterialSwitch.setName(I.tr("MaterialSwitch"));
		group.add(tfMaterialSwitch, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
		tfRefGuid.setName(I.tr("Reference Guid"));
		group.add(tfRefGuid, GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(group, ctx));
	}

	/**
	 * Slot der Entity <code>aEntity</code> lesen und anzeigen
	 *
	 * @param aEntity
	 */
	public void loadSlot(eCEntity aEntity) {
		ownerEntity = aEntity;
		gCInventory_PS inv = ownerEntity.getClass(CD.gCInventory_PS.class);
		G3Class slot = inv.getValidSlot(slotType);

		btnEdit.setEnabled(false);
		btnTple.setEnabled(false);
		btnAdd.setEnabled(true);
		btnDelete.setEnabled(false);
		if (error) {
			addComponents();
		}
		error = false;

		if (slot == null) {
			slot = inv.getErrorSlot(slotType);
			if (slot != null) {
				int innerSlotType = slot.property(CD.gCInventorySlot.Slot).getEnumValue();
				String message = I.trf("Inner slot type ''{0}'' differs from outer slot type ''{1}''.",
						G3Enums.asString(gESlot.class, innerSlotType), G3Enums.asString(gESlot.class, slotType));
				slotError(message);
				btnDelete.setEnabled(true);
			} else {
				slotError(I.tr("Slot not present"));
			}
			return;
		}

		btnDelete.setEnabled(true);

		tfRefGuid.setText(slot.property(CD.gCInventorySlot.Template).getGuid());
		String itemGuid = slot.property(CD.gCInventorySlot.Item).getGuid();
		slotEntity = ctx.getCurrentFile().getEntityByGuid(itemGuid).orElse(null);
		if (slotEntity != null) {
			if (ownerEntity != slotEntity.getParent()) {
				slotError(I.trf("Slot entity ''{0}'' ({1}) is not a child of this NPC, but a child of ''{2}'' ({3})", slotEntity.getName(),
						slotEntity.getGuid(), slotEntity.getParent().getName(), slotEntity.getParent().getGuid()));
				return;
			}

			Pair<String, Integer> meshAndMaterialSwitch = EntityUtil.getMeshAndMaterialSwitch(slotEntity).orElse(null);
			if (meshAndMaterialSwitch != null) {
				tfName.setText(slotEntity.getName());
				tfChangeTime.setText(String.valueOf(slotEntity.getDataChangedTimeStamp()));
				tfMesh.setText(meshAndMaterialSwitch.el0().replace(".FXA", ".fxa"));
				tfMaterialSwitch.setText(meshAndMaterialSwitch.el1().toString());
				btnEdit.setEnabled(true);
				btnTple.setEnabled(true);
				btnAdd.setEnabled(false);
			} else {
				slotError(I.trf("Slot entity ''{0}'' has no mesh class", slotEntity.getName()));
			}

		} else {
			slotError(I.trf("Slot entity with guid ''{0}'' was not found", itemGuid));
		}
	}

	/**
	 * Neue Slotdaten speichern
	 */
	private void saveSlot() {
		if (error) {
			return;
		}

		String name = tfName.getText();
		String mesh = tfMesh.getText();
		int materialSwitch = tfMaterialSwitch.getText().isEmpty() ? 0 : Integer.valueOf(tfMaterialSwitch.getText());
		String guid = GuidUtil.parseGuid(tfRefGuid.getText());
		int changeTime = tfChangeTime.getText().isEmpty() ? 0 : Integer.valueOf(tfChangeTime.getText());

		if (slotType == gESlot.gESlot_Head || slotType == gESlot.gESlot_Body) {
			String meshAnimated = null;

			if (slotType == gESlot.gESlot_Head) {
				if (mesh.contains("_") && mesh.substring(mesh.lastIndexOf("_")).matches("_\\d+\\.fxa")) {
					meshAnimated = mesh.substring(0, mesh.lastIndexOf("_")) + "_Animated" + mesh.substring(mesh.lastIndexOf("_"));
				} else {
					meshAnimated = mesh.replace(".fxa", "_Animated.fxa");
				}
			}

			NPCUtil.setAnimatedProperties(slotEntity, name, mesh, meshAnimated, materialSwitch, guid, changeTime);
			NPCUtil.syncAnimatedProperties(ownerEntity, slotEntity);
		} else {
			NPCUtil.setWearableProperties(slotEntity, name, mesh, mesh, materialSwitch, guid, changeTime);
			NPCUtil.syncWearableProperties(ownerEntity, slotEntity);
		}
	}

	/**
	 * Fehler anzeigen
	 *
	 * @param errorMessage
	 */
	private void slotError(String errorMessage) {
		removeComponents();
		lblError.setText(errorMessage);
		tfName.setText(null);
		tfMesh.setText(null);
		tfMaterialSwitch.setText("0");
		tfRefGuid.setText(null);
		tfChangeTime.setText("0");
		error = true;
	}

	/**
	 * Zwischen 'Editieren' und 'Template Laden' oder 'Speichern' und 'Abbrechen' Buttons umschalten
	 *
	 * @param edit
	 * @param tooltip1
	 * @param icon1
	 * @param tooltip2
	 * @param icon2
	 */
	private void changeButtons(boolean edit, String tooltip1, Icon icon1, String tooltip2, Icon icon2) {
		tfName.setEditable(edit);
		tfMesh.setEditable(edit);
		tfMaterialSwitch.setEditable(edit);
		tfRefGuid.setEditable(edit);
		tfChangeTime.setEditable(edit);
		btnEdit.setIcon(icon1);
		btnEdit.setToolTipText(tooltip1);
		btnTple.setIcon(icon2);
		btnTple.setToolTipText(tooltip2);
	}

	@Override
	public boolean templateSearchCallback(TemplateFile tpleFile) {
		int useType = -1;
		if (tpleFile.getHeaderCount() == 2) {
			useType = EntityUtil.getUseType(tpleFile.getReferenceHeader());
		}

		int tpleSlotType = gESlot.fromUseType(useType);
		if (tpleSlotType == slotType) {
			if (slotType == gESlot.gESlot_Head || slotType == gESlot.gESlot_Body) {
				NPCUtil.setAnimatedProperties(slotEntity, tpleFile);
				NPCUtil.syncAnimatedProperties(ownerEntity, slotEntity);
			} else {
				NPCUtil.setWearableProperties(slotEntity, tpleFile);
				NPCUtil.syncWearableProperties(ownerEntity, slotEntity);
			}
		} else {
			return false;
		}

		ctx.refreshTree(false);

		loadSlot(ownerEntity);

		return true;
	}

	private void handleEditManual() {
		// Editieren Modus aktivieren
		if (btnEdit.getIcon().equals(Icons.getImageIcon(Icons.Action.EDIT))) {
			changeButtons(true, I.tr("Apply changes"), Icons.getImageIcon(Icons.Select.TICK), I.tr("Discard changes"),
					Icons.getImageIcon(Icons.Select.CANCEL));
		} else {
			changeButtons(false, I.tr("Edit values"), Icons.getImageIcon(Icons.Action.EDIT), I.tr("Load template"),
					Icons.getImageIcon(Icons.Action.BOOK));
			saveSlot();
			ctx.refreshTree(false);
			loadSlot(ownerEntity);
		}
	}

	private void handleLoadTemplate() {
		// Editieren Modus aktivieren
		if (btnTple.getIcon().equals(Icons.getImageIcon(Icons.Action.BOOK))) {
			new SlotDialog(NPCSlotPanel.this, ctx).open();
		} else {
			changeButtons(false, I.tr("Edit values"), Icons.getImageIcon(Icons.Action.EDIT), I.tr("Load template"),
					Icons.getImageIcon(Icons.Action.BOOK));
			loadSlot(ownerEntity);
		}
	}

	private void handleAddSlot() {
		ListSelectDialog<eCEntity> dialog = new ListSelectDialog<>(ctx.getParentWindow(), I.tr("Select entity"),
				AbstractSelectDialog.SELECTION_SINGLE, ownerEntity.getChilds());

		if (dialog.openAndWasSuccessful()) {
			eCEntity entity = dialog.getSelectedEntries().get(0);
			if (slotType != gESlot.fromUseType(EntityUtil.getUseType(entity))) {
				slotError(I.tr("Selected entity is not suitable for this slot type"));
				return;
			}

			slotEntity = entity;
			NPCUtil.removeSlot(ownerEntity, slotEntity, slotType);
			NPCUtil.addSlot(ownerEntity, slotEntity);
			if (slotType == gESlot.gESlot_Head || slotType == gESlot.gESlot_Body) {
				NPCUtil.syncAnimatedProperties(ownerEntity, slotEntity);
			} else {
				NPCUtil.syncWearableProperties(ownerEntity, slotEntity);
			}
			loadSlot(ownerEntity);
		}
	}

	private void handleRemoveSlot() {
		NPCUtil.removeSlot(ownerEntity, slotEntity, slotType);
		loadSlot(ownerEntity);
	}
}
