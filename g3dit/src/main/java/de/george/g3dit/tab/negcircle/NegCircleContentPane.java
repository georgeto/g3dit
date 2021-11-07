package de.george.g3dit.tab.negcircle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.ezware.dialog.task.TaskDialogs;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.dialogs.EnterGuidDialog;
import de.george.g3dit.gui.validation.PointDistanceValidator;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.navmap.NavMapObjectContentPane;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.enums.G3Enums.gENavObstacleType;
import de.george.navmap.data.NegCircle;
import de.george.navmap.util.NegCircleCalc;
import net.miginfocom.swing.MigLayout;

public class NegCircleContentPane extends NavMapObjectContentPane {
	private NegCircleEditingPanel editingPanel;

	public NegCircleContentPane(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public ListManageAndEdit<String> createContent() {
		editingPanel = new NegCircleEditingPanel();
		EventList<String> circleEventList = GlazedLists.eventList(navMap.getNegCircles().map(NegCircle::getCircleGuid).toList());
		return ListManageAndEdit.create(circleEventList, this::addNegCircle, editingPanel).onSelect(editingPanel::loadNegCircle)
				.onDelete(this::deleteNegCircle).searchTooltip(getSearchTooltip())
				.matcherEditor(tfFilter -> new NavMapObjectMatcherEditor(tfFilter,
						negZone -> navMap.getNegCircle(negZone).map(NegCircle::getCenter)))
				.build();
	}

	private Optional<String> addNegCircle() {
		EnterGuidDialog dialog = new EnterGuidDialog(ctx.getParentWindow(), "NegCircle erstellen", "Erstellen", GuidUtil.randomGUID());

		if (dialog.openAndWasSuccessful()) {
			NegCircle negCircle = new NegCircle(dialog.getEnteredGuid());
			navMap.addNegCircle(negCircle);
			return Optional.of(negCircle.getCircleGuid());
		} else {
			return Optional.empty();
		}
	}

	private boolean deleteNegCircle(String negCircle) {
		navMap.removeNegCircle(negCircle);
		return true;
	}

	@Override
	protected void onGoto(String guid) {
		navMap.getNegCircle(guid).map(NegCircle::getCenter).ifPresent(IpcUtil::gotoPosition);
	}

	@Override
	protected void onShowOnMap(List<String> guid) {
		EntityMap map = EntityMap.getInstance(ctx);
		guid.stream().map(navMap::getNegCircle).forEach(o -> o.ifPresent(map::addNegCircle));
	}

	private class NegCircleEditingPanel extends JPanel {
		private Optional<String> negCircleGuid;

		private JGuidField gfCircleGuid;
		private JEnumComboBox<gENavObstacleType> ecbObstacleType;
		private JTextAreaExt taOffsets, taRadius;
		private JTextField tfObjectY;
		private JTextAreaExt taZonesGuids;

		private EnableGroup enableGroup;

		public NegCircleEditingPanel() {
			setupComponents();
			loadNegCircle(null);
		}

		private void setupComponents() {
			setLayout(new MigLayout());

			gfCircleGuid = new JGuidField();
			gfCircleGuid.setEditable(false);

			ecbObstacleType = new JEnumComboBox<>(gENavObstacleType.class);
			ecbObstacleType.addActionListener(e -> negCircleGuid.flatMap(navMap::getNegCircle).map(circle -> {
				if (circle.obstacleType != ecbObstacleType.getSelectedValue()) {
					circle.obstacleType = ecbObstacleType.getSelectedValue();
					return circle;
				} else {
					return null;
				}
			}).ifPresent(navMap::updateNegCircle));

			taOffsets = new JTextAreaExt(true);
			taOffsets.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			taOffsets.getScrollPane().setRowHeaderView(new TextLineNumber(taOffsets));
			taOffsets.setName("Offsets");
			addValidators(taOffsets, new PointDistanceValidator());

			taRadius = new JTextAreaExt(true);
			taRadius.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			taRadius.getScrollPane().setRowHeaderView(new TextLineNumber(taRadius));

			tfObjectY = new JTextField();
			tfObjectY.setEditable(false);

			taZonesGuids = new JTextAreaExt(true);
			taZonesGuids.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			taZonesGuids.setEditable(false);

			add(new JLabel("NegCircle Guid"), "wrap");
			add(gfCircleGuid, "width 100:300:300, wrap");
			add(new JLabel("ObstaceType"), "wrap");
			add(ecbObstacleType, "wrap");
			add(new JLabel("Offsets (x-Pos/y-Pos/z-Pos//)"), "gaptop 7");
			add(new JLabel("Radius"), "gaptop 7, wrap");
			add(taOffsets.getScrollPane(), "width 100:500:500, pushy, grow");
			add(taRadius.getScrollPane(), "width 100:125:150, pushy, grow, wrap");
			add(new JLabel("ObjectY"), "wrap");
			add(tfObjectY, "wrap");
			add(new JLabel("NavZones"), "gaptop 7, spanx, wrap");
			add(taZonesGuids.getScrollPane(), "spanx, flowy, aligny top, split 2, hmax 200, wmax 500, pushy, grow");

			JButton btnCalc = new JButton("Errechne NegCircle aus Offsets");
			this.add(btnCalc, "");
			btnCalc.addActionListener(l -> handleCalcNegCircle());

			enableGroup = EnableGroup.create(gfCircleGuid, ecbObstacleType, taOffsets, taRadius, tfObjectY, taZonesGuids, btnCalc);

			// TODO: Show/modify NegCircles without object...
		}

		public void loadNegCircle(String newNegCircleGuid) {
			negCircleGuid = Optional.ofNullable(newNegCircleGuid);
			enableGroup.setEnabled(negCircleGuid.isPresent());

			if (negCircleGuid.isPresent()) {
				NegCircle negCircle = navMap.getNegCircle(newNegCircleGuid).get();
				gfCircleGuid.setText(negCircle.circleGuid);
				ecbObstacleType.setSelectedValue(negCircle.obstacleType);
				taOffsets.setText(Misc.formatVectorList(negCircle.circleOffsets));
				taRadius.setText(Misc.formatFloatList(negCircle.circleRadius));
				tfObjectY.setText(Misc.formatFloat(negCircle.objectY));
				taZonesGuids.setText(Misc.formatList(negCircle.zoneGuids));
			}
		}

		private void handleCalcNegCircle() {
			List<bCVector> offsets = null;
			List<Float> radius = null;
			try {
				offsets = Misc.parseVectorList(taOffsets.getText());
				radius = Misc.parseFloatList(taRadius.getText());
			} catch (IllegalArgumentException e) {
				TaskDialogs.inform(ctx.getParentWindow(), "",
						"Mindestens für einen Offset oder Radius wurden ungültige Werte eingegeben.");
				return;
			}

			if (offsets.size() <= 0) {
				TaskDialogs.inform(ctx.getParentWindow(), "", "Eine NegCircle muss mindestens einen Offset haben.");
				return;
			}

			if (offsets.size() != radius.size()) {
				TaskDialogs.inform(ctx.getParentWindow(), "", "Anzahl von Offsets und Radius stimmt nicht überein.");
				return;
			}

			NegCircle negCircle = navMap.getNegCircle(negCircleGuid.get()).get();
			negCircle.circleOffsets = offsets;
			negCircle.circleRadius = radius;
			negCircle.zoneGuids = NegCircleCalc.calcAssignedAreas(negCircle, navCalc).stream().map(a -> a.areaId)
					.collect(Collectors.toList());
			negCircle.objectY = negCircle.getCenter().getY();
			navMap.updateNegCircle(negCircle);
			loadNegCircle(negCircle.getCircleGuid());
		}
	}
}
