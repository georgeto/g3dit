package de.george.g3dit.tab.prefpath;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.renderer.FunctionalListCellRenderer;
import de.george.g3dit.gui.validation.PointDistanceValidator;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.navmap.NavMapObjectContentPane;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.navmap.data.PrefPath;
import de.george.navmap.sections.NavMap;
import net.miginfocom.swing.MigLayout;

public class PrefPathContentPane extends NavMapObjectContentPane {
	private PrefPathEditingPanel editingPanel;

	public PrefPathContentPane(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public ListManageAndEdit<String> createContent() {
		editingPanel = new PrefPathEditingPanel();
		EventList<String> pathEventList = GlazedLists.eventList(navMap.getPrefPaths().map(PrefPath::getVirtualGuid).toList());
		return ListManageAndEdit.create(pathEventList, this::addPrefPath, editingPanel).onSelect(editingPanel::loadPrefPath)
				.onDelete(this::deletePrefPath)
				.cellRenderer(new FunctionalListCellRenderer<String>(
						g -> navMap.getPrefPath(g).map(p -> p.getRadiusOffset().toString()).orElse("<No longer in NavMap>")))
				.searchTooltip(getSearchTooltip()).matcherEditor(tfFilter -> new NavMapObjectMatcherEditor(tfFilter,
						negZone -> navMap.getPrefPath(negZone).map(PrefPath::getRadiusOffset)))
				.build();
	}

	private Optional<String> addPrefPath() {
		PrefPath prefPath = new PrefPath(Collections.emptyList(), Collections.emptyList(), null);
		navMap.addPrefPath(prefPath);
		return Optional.ofNullable(prefPath.getVirtualGuid());
	}

	private boolean deletePrefPath(String prefPath) {
		navMap.removePrefPath(prefPath);
		return true;
	}

	@Override
	protected void onGoto(String guid) {
		navMap.getPrefPath(guid).map(PrefPath::getRadiusOffset).ifPresent(IpcUtil::gotoPosition);
	}

	@Override
	protected void onShowOnMap(List<String> guid) {
		EntityMap map = EntityMap.getInstance(ctx);
		guid.stream().map(navMap::getPrefPath).forEach(o -> o.ifPresent(map::addPrefPath));
	}

	private class PrefPathEditingPanel extends JPanel {
		private Optional<String> prefPathGuid;

		private JTextAreaExt taSticks, taRadius;
		private JTextField tfRadius, tfRadiusOffset;
		private JGuidField gfZoneGuid;

		private EnableGroup enableGroup;

		public PrefPathEditingPanel() {
			setupComponents();
			loadPrefPath(null);
		}

		private void setupComponents() {
			setLayout(new MigLayout("fill", "[]10[][fill, grow]push[]", "[][][][][]push[fill, grow]push[]"));

			tfRadius = SwingUtils.createUndoTF();
			tfRadius.setEditable(false);

			tfRadiusOffset = SwingUtils.createUndoTF();
			tfRadiusOffset.setEditable(false);

			gfZoneGuid = new JEntityGuidField(ctx);
			gfZoneGuid.setEditable(false);

			add(new JLabel("RadiusOffset (x/y/z//)"), "");
			add(new JLabel("Radius"), "wrap");
			add(tfRadiusOffset, "width 200:250:300");
			add(tfRadius, "width 50:100:100, wrap");
			add(new JLabel("NavZone Guid"), "wrap");
			add(gfZoneGuid, "width 100:300:300, wrap");

			taSticks = new JTextAreaExt(true);
			taSticks.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			taSticks.getScrollPane().setRowHeaderView(new TextLineNumber(taSticks));
			taSticks.setName("Sticks");
			addValidators(taSticks, new PointDistanceValidator());

			taRadius = new JTextAreaExt(true);
			taRadius.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			taRadius.getScrollPane().setRowHeaderView(new TextLineNumber(taRadius));

			this.add(new JLabel("Sticks (x-Pos/y-Pos/z-Pos//)"), "gaptop 7, spanx 3");
			this.add(new JLabel("Radius"), "gapleft 10, gaptop 7, wrap");
			this.add(taSticks.getScrollPane(), "spanx 3, grow");
			this.add(taRadius.getScrollPane(), "gapleft 10, width 50:120:150, grow, wrap");

			JButton btnCalc = new JButton("Errechne PrefPath aus Sticks");
			this.add(btnCalc, "split 2, spanx 3");
			btnCalc.addActionListener(l -> handlePrefPath());

			enableGroup = EnableGroup.create(tfRadius, tfRadiusOffset, gfZoneGuid, taSticks, taRadius, btnCalc);
		}

		public void loadPrefPath(String newPrefPathGuid) {
			prefPathGuid = Optional.ofNullable(newPrefPathGuid);

			enableGroup.setEnabled(prefPathGuid.isPresent());
			if (prefPathGuid.isPresent()) {
				PrefPath prefPath = navMap.getPrefPath(prefPathGuid.get()).get();
				tfRadius.setText(Misc.formatFloat(prefPath.getRadius()));
				tfRadiusOffset.setText(prefPath.getRadiusOffset().toString());
				gfZoneGuid.setText(prefPath.getZoneGuid());
				taSticks.setText(Misc.formatVectorList(prefPath.getPoints()));
				taRadius.setText(Misc.formatFloatList(prefPath.getPointRadius()));
			}
		}

		private void handlePrefPath() {
			List<bCVector> sticks;
			try {
				sticks = Misc.parseVectorList(taSticks.getText());
			} catch (IllegalArgumentException e) {
				TaskDialogs.inform(ctx.getParentWindow(), "", "Mindestens für einen Stick wurden fehlerhafte Koordinaten eingegeben.");
				return;
			}

			List<Float> pointRadius;
			try {
				pointRadius = Misc.parseFloatList(taRadius.getText());
			} catch (IllegalArgumentException e) {
				TaskDialogs.inform(ctx.getParentWindow(), "", "Mindestens für einen Stick wurden fehlerhafte Koordinaten eingegeben.");
				return;
			}

			if (sticks.size() != pointRadius.size()) {
				TaskDialogs.inform(ctx.getParentWindow(), "", "Anzahl von Sticks und PointRadius unterscheidet sich.");
				return;
			}

			PrefPath prefPath = navMap.getPrefPath(prefPathGuid.get()).get();
			prefPath.setPoints(sticks, pointRadius);
			prefPath.setZoneGuid(getPrefPathZone(prefPath));

			navMap.updatePrefPath(prefPath);
			loadPrefPath(prefPath.getVirtualGuid());
		}

		private String getPrefPathZone(PrefPath prefPath) {
			String detecedZoneStart = navCalc.getZone(prefPath.getPoints().get(0), true, false, -1.0f).map(a -> a.areaId)
					.orElse(NavMap.INVALID_ZONE_ID);
			String detecedZoneEnd = navCalc.getZone(prefPath.getPoints().get(prefPath.getPoints().size() - 1), true, false, -1.0f)
					.map(a -> a.areaId).orElse(NavMap.INVALID_ZONE_ID);

			boolean startInZone = !detecedZoneStart.equals(NavMap.INVALID_ZONE_ID);
			boolean endInZone = !detecedZoneEnd.equals(NavMap.INVALID_ZONE_ID);

			if (startInZone && endInZone && !detecedZoneStart.equals(detecedZoneEnd)) {
				TaskDialogs.error(ctx.getParentWindow(), "Spans two NavZones",
						"The PrefPath spans two or more zones,\nbut a PrefPath can only be assigned to one zone.\nPlease consider to change the PrefPath.");
				return detecedZoneStart;
			} else {
				return startInZone ? detecedZoneStart : detecedZoneEnd;
			}
		}
	}
}
