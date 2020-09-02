package de.george.g3dit.tab.negzone;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.dialogs.EnterGuidDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.gui.validation.PointDistanceValidator;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.navmap.NavMapObjectContentPane;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.navmap.data.NegZone;
import net.miginfocom.swing.MigLayout;

public class NegZoneContentPane extends NavMapObjectContentPane {
	private NegZoneEditingPanel editingPanel;

	public NegZoneContentPane(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public ListManageAndEdit<String> createContent() {
		editingPanel = new NegZoneEditingPanel();
		EventList<String> zoneEventList = GlazedLists.eventList(navMap.getNegZones().map(NegZone::getGuid).toList());
		return ListManageAndEdit.create(zoneEventList, this::addNegZone, editingPanel).onSelect(editingPanel::loadNegZone)
				.onDelete(this::deleteNegZone).cellRenderer(new BeanListCellRenderer("Guid") {
					@Override
					public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

						Optional<NegZone> zone = navMap.getNegZone((String) value);
						if (zone.isPresent()) {
							SwingUtils.generateTooltipErrorList(this, zone.get().generateErrors(), Collections.emptyList());
						}

						return this;
					}

				}).searchTooltip(getSearchTooltip()).matcherEditor(tfFilter -> new NavMapObjectMatcherEditor(tfFilter,
						negZone -> navMap.getNegZone(negZone).map(NegZone::getRadiusOffset)))
				.build();
	}

	private Optional<String> addNegZone() {
		EnterGuidDialog dialog = new EnterGuidDialog(ctx.getParentWindow(), I.tr("NegZone erstellen"), I.tr("Erstellen"),
				GuidUtil.randomGUID());

		if (dialog.openAndWasSuccessful()) {
			NegZone negZone = new NegZone(dialog.getEnteredGuid(), null, Collections.emptyList(), 0, bCVector.nullVector(), false);
			navMap.addNegZone(negZone);
			return Optional.of(negZone.getGuid());
		} else {
			return Optional.empty();
		}
	}

	private boolean deleteNegZone(String negZone) {
		navMap.removeNegZone(negZone);
		return true;
	}

	@Override
	protected void onGoto(String guid) {
		navMap.getNegZone(guid).map(NegZone::getWorldRadiusOffset).ifPresent(IpcUtil::gotoPosition);
	}

	@Override
	protected void onShowOnMap(List<String> guid) {
		EntityMap map = EntityMap.getInstance(ctx);
		guid.stream().map(navMap::getNegZone).forEach(o -> o.ifPresent(map::addNegZone));
	}

	private class NegZoneEditingPanel extends JPanel {
		private Optional<String> negZoneGuid;

		private JTextAreaExt taSticks;
		private JTextField tfRadius, tfRadiusOffset;
		private JGuidField gfGuid, gfZoneGuid;
		private JCheckBox cbZoneIsCCW;

		private EnableGroup enableGroup;

		public NegZoneEditingPanel() {
			setupComponents();
			loadNegZone(null);
		}

		private void setupComponents() {
			setLayout(new MigLayout());

			gfGuid = new JGuidField();
			gfGuid.setEditable(false);

			tfRadius = SwingUtils.createUndoTF();
			tfRadius.setEditable(false);

			tfRadiusOffset = SwingUtils.createUndoTF();
			tfRadiusOffset.setEditable(false);

			gfZoneGuid = new JEntityGuidField(ctx);
			gfZoneGuid.setEditable(false);

			cbZoneIsCCW = new JCheckBox("ZoneIsCCW");
			cbZoneIsCCW.addActionListener(e -> negZoneGuid.flatMap(navMap::getNegZone).map(zone -> {
				if (zone.isCcw() != cbZoneIsCCW.isSelected()) {
					zone.setCcw(cbZoneIsCCW.isSelected());
					return zone;
				} else {
					return null;
				}
			}).ifPresent(navMap::updateNegZone));

			add(new JLabel("NegZone Guid"), "wrap");
			add(gfGuid, "width 100:300:300, wrap");
			add(new JLabel("RadiusOffset (x/y/z//)"), "");
			add(new JLabel("Radius"), "wrap");
			add(tfRadiusOffset, "width 200:250:300");
			add(tfRadius, "width 50:100:100, wrap");
			add(new JLabel("NavZone Guid"), "wrap");
			add(gfZoneGuid, "width 100:300:300");
			add(cbZoneIsCCW, "wrap");

			taSticks = new JTextAreaExt(true);
			taSticks.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			TextLineNumber tln = new TextLineNumber(taSticks);
			taSticks.getScrollPane().setRowHeaderView(tln);
			taSticks.setName("Sticks");
			addValidators(taSticks, new PointDistanceValidator());

			JLabel lblStickList = new JLabel("Sticks (x-Pos/y-Pos/z-Pos//)");

			this.add(lblStickList, "gaptop 7, wrap");
			this.add(taSticks.getScrollPane(), "spanx 3, width 100:500:500, pushy, grow, wrap");

			JButton btnCalc = new JButton(I.tr("Errechne NegZone aus Sticks"));
			this.add(btnCalc, "split 2, spanx 3");
			btnCalc.addActionListener(l -> handleCalcNegZone());

			enableGroup = EnableGroup.create(gfGuid, tfRadius, tfRadiusOffset, gfZoneGuid, cbZoneIsCCW, taSticks, btnCalc);
		}

		public void loadNegZone(String newNegZoneGuid) {
			negZoneGuid = Optional.ofNullable(newNegZoneGuid);

			enableGroup.setEnabled(negZoneGuid.isPresent());
			if (negZoneGuid.isPresent()) {
				NegZone negZone = navMap.getNegZone(newNegZoneGuid).get();
				gfGuid.setText(negZone.getGuid());
				tfRadius.setText(Misc.formatFloat(negZone.getRadius()));
				tfRadiusOffset.setText(negZone.getRadiusOffset().toString());
				gfZoneGuid.setText(negZone.getZoneGuid());
				cbZoneIsCCW.setSelected(negZone.isCcw());
				taSticks.setText(Misc.formatVectorList(negZone.getPoints()));
			}
		}

		private void handleCalcNegZone() {
			List<bCVector> sticks = null;
			try {
				sticks = Misc.parseVectorList(taSticks.getText());
			} catch (IllegalArgumentException e) {
				TaskDialogs.inform(ctx.getParentWindow(), "",
						I.tr("Mindestens für einen Stick wurden fehlerhafte Koordinaten eingegeben."));
				return;
			}

			if (sticks.size() < 3) {
				TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("Eine NegZone muss aus mindestens 3 Sticks bestehen."));
				return;
			}

			bCVector radiusOffset = bCVector.averageVector(sticks);
			float radius = sticks.stream().map(stick -> radiusOffset.getRelative(stick).length()).reduce(Float::max).get();

			NegZone negZone = navMap.getNegZone(negZoneGuid.get()).get();
			negZone.setPoints(sticks);
			negZone.setRadiusOffset(radiusOffset);
			negZone.setRadius(radius);
			negZone.arePointsCcw().ifPresent(negZone::setCcw);
			negZone.setZoneGuid(navCalc.getNavZoneGuid(negZone));
			navMap.updateNegZone(negZone);
			loadNegZone(negZone.getGuid());
		}
	}
}
