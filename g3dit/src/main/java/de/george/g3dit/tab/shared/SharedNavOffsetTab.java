package de.george.g3dit.tab.shared;

import java.awt.Container;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMotion;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.g3utils.validation.ValidationGroupWrapper;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEDirection;
import de.george.lrentnode.properties.gInt;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class SharedNavOffsetTab extends AbstractSharedTab {
	private JCheckBox cbOffsetCircle;
	private NavOffsetsPanel navOffsetsPanel;

	public SharedNavOffsetTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	public void afterScrollPaneCreation(JScrollPane scrollPane) {
		container.setLayout(new MigLayout("fillx"));

		container.add(SwingUtils.createBoldLabel("Eigenschaften"), "gaptop 7, wrap");

		cbOffsetCircle = new JCheckBox("OffsetCircle");
		cbOffsetCircle.setToolTipText(SwingUtils.getMultilineText("Animation kann auf einem Kreis um dieses Objekt ausgeführt werden.",
				"Der Radius des Kreises wird durch den ersten NavOffset definiert."));
		container.add(cbOffsetCircle, "gapleft 7, wrap");

		container.add(SwingUtils.createBoldLabel("NavOffsets"), "gaptop 7, wrap");

		navOffsetsPanel = new NavOffsetsPanel(scrollPane);
		container.add(navOffsetsPanel, "grow, wrap");
	}

	@Override
	public String getTabTitle() {
		return "NavOffset";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.gCNavOffset_PS.class);
	}

	@Override
	public void initValidation(ValidationPanel validationPanel) {
		navOffsetsPanel.initValidation(validationPanel);
	}

	@Override
	public void loadValues(G3ClassContainer entity) {
		G3Class navOffset = entity.getClass(CD.gCNavOffset_PS.class);
		PropertySync.wrap(navOffset).readBool(cbOffsetCircle, CD.gCNavOffset_PS.OffsetCircle);
		navOffsetsPanel.loadValues(navOffset);
	}

	@Override
	public void saveValues(G3ClassContainer entity) {
		G3Class navOffset = entity.getClass(CD.gCNavOffset_PS.class);
		PropertySync.wrap(navOffset).writeBool(cbOffsetCircle, CD.gCNavOffset_PS.OffsetCircle);
		navOffsetsPanel.saveValues(navOffset);
	}

	private static class NavOffset {
		private bCMotion motion;
		private int direction;

		private NavOffset(bCMotion motion, int direction) {
			this.motion = motion.clone();
			this.direction = direction;
		}

	}

	private class NavOffsetsPanel extends AbstractElementsPanel<G3Class> {
		public NavOffsetsPanel(JScrollPane navScroll) {
			super("NavOffset", navScroll, true);

			setLayout(new MigLayout("fillx, insets 0 5 0 0", "[]"));
		}

		private void clearNavOffsets(G3Class navOffsetPs) {
			navOffsetPs.property(CD.gCNavOffset_PS.OffsetPose).clear();
			navOffsetPs.property(CD.gCNavOffset_PS.AniDirection).clear();
		}

		@Override
		public void loadValuesInternal(G3Class navOffsetPs) {
			List<bCMotion> poses = navOffsetPs.property(CD.gCNavOffset_PS.OffsetPose).getEntries();
			List<gInt> directions = navOffsetPs.property(CD.gCNavOffset_PS.AniDirection).getEntries();

			if (poses.size() != directions.size()) {
				throw new IllegalArgumentException("gCNavOffset_PS hat unterschiedlich viele OffsetPoses und AniDirections.");
			}

			for (NavOffset offset : StreamEx.zip(poses, directions, (p, d) -> new NavOffset(p, d.getInt()))) {
				NavOffsetPanel stackPanel = new NavOffsetPanel(offset);
				insertElementRelative(stackPanel, null, InsertPosition.After);
			}
		}

		@Override
		public void saveValuesInternal(G3Class navOffsetPs) {
			clearNavOffsets(navOffsetPs);
			for (int i = 0; i < getComponentCount(); i++) {
				NavOffset navOffset = ((NavOffsetPanel) getComponent(i)).save();
				navOffsetPs.property(CD.gCNavOffset_PS.OffsetPose).getEntries().add(navOffset.motion.clone());
				navOffsetPs.property(CD.gCNavOffset_PS.AniDirection).getEntries().add(new gInt(navOffset.direction));
			}
		}

		@Override
		protected void removeValuesInternal(G3Class navOffsetPs) {
			clearNavOffsets(navOffsetPs);
		}

		@Override
		protected AbstractElementPanel getNewElement() {
			return new NavOffsetPanel(
					new NavOffset(new bCMotion(bCVector.nullVector(), new bCQuaternion(0, 0, 0, 1)), gEDirection.gEDirection_None));
		}
	}

	private class NavOffsetPanel extends AbstractElementPanel {
		private JEnumComboBox<gEDirection> cbAniDirection;
		private String title;

		private NavOffset navOffset;
		private UndoableTextField tfPositon, tfPitch, tfYaw, tfRoll;

		public NavOffsetPanel(NavOffset navOffset) {
			super("NavOffset", navOffsetsPanel);
			this.navOffset = navOffset;
			setLayout(new MigLayout("fillx", "[]10[]10[]"));

			add(new JLabel("Position"), "wrap");
			tfPositon = SwingUtils.createUndoTF();
			add(tfPositon, "spanx 3, growx, wrap");
			add(new JLabel("Pitch"), "");
			add(new JLabel("Yaw"), "");
			add(new JLabel("Roll"), "wrap");
			tfPitch = SwingUtils.createUndoTF();
			tfYaw = SwingUtils.createUndoTF();
			tfRoll = SwingUtils.createUndoTF();
			add(tfPitch, "sgx rot, width 100:125:150, growx");
			add(tfYaw, "sgx rot, growx");
			add(tfRoll, "sgx rot, growx, wrap");

			add(new JLabel("AniDirection"), "wrap");

			cbAniDirection = new JEnumComboBox<>(gEDirection.class);
			add(cbAniDirection, "sgx rot, growx, wrap");

			load();

			JPanel operationPanel = getOperationPanel();
			add(operationPanel, "cell 3 0, spanx 2, spany");
		}

		@Override
		public void initValidation(ValidationGroup group) {

		}

		@Override
		public void removeValidation(ValidationGroupWrapper group) {

		}

		@Override
		protected String getBorderTitle() {
			return title;
		}

		private void load() {
			cbAniDirection.setSelectedValue(navOffset.direction);
			tfPositon.setText(navOffset.motion.getPosition().toString());
			bCEulerAngles rotation = new bCEulerAngles(navOffset.motion.getRotation());
			tfPitch.setText(Misc.formatFloat(rotation.getPitchDeg(), 2));
			tfYaw.setText(Misc.formatFloat(rotation.getYawDeg(), 2));
			tfRoll.setText(Misc.formatFloat(rotation.getRollDeg(), 2));
		}

		public NavOffset save() {
			int direction = cbAniDirection.getSelectedValue();
			if (direction != navOffset.direction) {
				navOffset.direction = direction;
			}

			if (tfPositon.hasChanged()) {
				navOffset.motion.setPosition(bCVector.fromString(tfPositon.getText()));
			}

			if (tfPitch.hasChanged() || tfYaw.hasChanged() || tfRoll.hasChanged()) {
				bCEulerAngles rotation = bCEulerAngles.fromDegree(Float.parseFloat(tfYaw.getText()), Float.parseFloat(tfPitch.getText()),
						Float.parseFloat(tfRoll.getText()));
				navOffset.motion.setRotation(new bCQuaternion(rotation));
			}

			return navOffset;
		}
	}
}
