package de.george.g3dit.tab.shared;

import java.awt.Container;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.edit.handler.LambdaPropertyHandler;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMotion;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.g3utils.validation.ValidationGroupWrapper;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEDirection;
import de.george.lrentnode.properties.gInt;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class SharedNavOffsetTab extends AbstractPropertySharedTab {
	private NavOffsetsPanel navOffsetsPanel;

	public SharedNavOffsetTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel, ValidationGroup validation, JScrollPane scrollPane) {
		navOffsetsPanel = new NavOffsetsPanel(scrollPane);
		navOffsetsPanel.initValidation(validation);

		// @foff
		propertyPanel
			.addHeadline(I.tr("Properties"))
			.add(CD.gCNavOffset_PS.OffsetCircle)
				.tooltip(SwingUtils.getMultilineText(I.tr("Animation can be performed on a circle around this object."),
						 I.tr("The radius of the circle is defined by the first NavOffset.")))
			.addHeadline("NavOffsets")
			.add(new LambdaPropertyHandler(navOffsetsPanel, navOffsetsPanel::loadValues, navOffsetsPanel::saveValues))
				.grow().constraints("width 100:300:450, wrap")
			.done();
		// @fon
	}

	@Override
	public String getTabTitle() {
		return "NavOffset";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.gCNavOffset_PS.class);
	}

	private static class NavOffset {
		private bCMotion motion;
		private int direction;

		private NavOffset(bCMotion motion, int direction) {
			this.motion = motion.clone();
			this.direction = direction;
		}

	}

	private class NavOffsetsPanel extends AbstractElementsPanel<G3ClassContainer> {
		public NavOffsetsPanel(JScrollPane navScroll) {
			super("NavOffset", navScroll, true);

			setLayout(new MigLayout("fillx, insets 0 5 0 0", "[]"));
		}

		private void clearNavOffsets(G3ClassContainer entity) {
			entity.getProperty(CD.gCNavOffset_PS.OffsetPose).clear();
			entity.getProperty(CD.gCNavOffset_PS.AniDirection).clear();
		}

		@Override
		public void loadValuesInternal(G3ClassContainer entity) {
			List<bCMotion> poses = entity.getProperty(CD.gCNavOffset_PS.OffsetPose).getEntries();
			List<gInt> directions = entity.getProperty(CD.gCNavOffset_PS.AniDirection).getEntries();

			if (poses.size() != directions.size()) {
				throw new IllegalArgumentException(I.tr("gCNavOffset_PS has different numbers of OffsetPoses and AniDirections."));
			}

			for (NavOffset offset : StreamEx.zip(poses, directions, (p, d) -> new NavOffset(p, d.getInt()))) {
				NavOffsetPanel stackPanel = new NavOffsetPanel(offset);
				insertElementRelative(stackPanel, null, InsertPosition.After);
			}
		}

		@Override
		public void saveValuesInternal(G3ClassContainer entity) {
			clearNavOffsets(entity);
			for (int i = 0; i < getComponentCount(); i++) {
				NavOffset navOffset = ((NavOffsetPanel) getComponent(i)).save();
				entity.getProperty(CD.gCNavOffset_PS.OffsetPose).getEntries().add(navOffset.motion.clone());
				entity.getProperty(CD.gCNavOffset_PS.AniDirection).getEntries().add(new gInt(navOffset.direction));
			}
		}

		@Override
		protected void removeValuesInternal(G3ClassContainer entity) {
			clearNavOffsets(entity);
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
