package de.george.g3dit.tab.archive.views.entity;

import java.awt.Window;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.gui.components.FloatSpinner;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.NavCalcFromNavCache;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.g3utils.util.Misc;
import de.george.g3utils.util.Pair;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPath.ZonePathIntersection;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc.IntersectZone;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class NavPathTab extends AbstractNavTab {
	private JCheckBox cbUnlimitedHeight;
	private JTextField tfAISM1, tfAISM2, tfAISC, tfBISM1, tfBISM2, tfBISC;
	private JTextAreaExt taRadius;
	private JGuidField tfZoneA, tfZoneB;
	private JButton btnResizePopup;

	public NavPathTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fillx", "[][][][]push"));

		add(new JLabel(I.tr("UnlimitedHeight")), "wrap");
		cbUnlimitedHeight = new JCheckBox();
		cbUnlimitedHeight.setToolTipText(I.tr("NavPath can be used by large NPCs, such as trolls and wyverns."));
		add(cbUnlimitedHeight, "wrap");

		// TODO: Warnung anzeigen, wenn NavPath nicht in NavZone liegt.

		JLabel lblZoneA = new JLabel(I.tr("ZoneA Guid"));
		tfZoneA = new JEntityGuidField(ctx);
		tfZoneA.setEditable(false);
		tfZoneA.initValidation(validation(), "ZoneA", GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx));
		add(lblZoneA, "wrap");
		add(tfZoneA, "width 100:300:300, spanx 3, wrap");

		JLabel lblZoneB = new JLabel(I.tr("ZoneB Guid"));
		tfZoneB = new JEntityGuidField(ctx);
		tfZoneB.setEditable(false);
		tfZoneB.initValidation(validation(), "ZoneB", GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx));
		add(lblZoneB, "wrap");
		add(tfZoneB, "width 100:300:300, spanx 3, wrap");

		add(new JLabel(I.tr("ZoneAIntersectionCenter")), "gaptop 5");
		add(new JLabel(I.tr("ZoneBIntersectionCenter")), "gaptop 5, wrap");
		tfAISC = SwingUtils.createUndoTF();
		tfBISC = SwingUtils.createUndoTF();
		tfAISC.setEditable(false);
		tfBISC.setEditable(false);
		add(tfAISC, "width 150:210:250");
		add(tfBISC, "width 150:210:250, wrap");

		add(new JLabel(I.tr("ZoneAIntersectionMargin1")), "");
		add(new JLabel(I.tr("ZoneBIntersectionMargin1")), "wrap");
		tfAISM1 = SwingUtils.createUndoTF();
		tfBISM1 = SwingUtils.createUndoTF();
		tfAISM1.setEditable(false);
		tfBISM1.setEditable(false);
		add(tfAISM1, "width 150:210:250");
		add(tfBISM1, "width 150:210:250, wrap");

		add(new JLabel(I.tr("ZoneAIntersectionMargin2")), "");
		add(new JLabel(I.tr("ZoneBIntersectionMargin2")), "wrap");
		tfAISM2 = SwingUtils.createUndoTF();
		tfBISM2 = SwingUtils.createUndoTF();
		tfAISM2.setEditable(false);
		tfBISM2.setEditable(false);
		add(tfAISM2, "width 150:210:250");
		add(tfBISM2, "width 150:210:250, wrap");

		setupComponents(I.tr("Preserve NavPath coordinates"));

		taRadius = new JTextAreaExt(true);
		taRadius.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		TextLineNumber tln = new TextLineNumber(taRadius);
		taRadius.getScrollPane().setRowHeaderView(tln);

		add(new JLabel(I.tr("Radius")), "cell 2 12");

		add(taRadius.getScrollPane(), "cell 2 14, push, grow, width 50:120:150, wrap");

		JButton btnCalc = new JButton(I.tr("Calculate NavPath"));
		add(btnCalc, "split 2, spanx 2");
		btnCalc.addActionListener(a -> processNavPath());
		add(cbManualCoords, "gapleft 15");

		btnResizePopup = new JButton(I.tr("Resize NavPath"));
		add(btnResizePopup, "wrap");
		JNavPathResizePopup resizePopup = new JNavPathResizePopup(ctx.getParentWindow(), () -> {
			// Update NavCache
			NavCache cache = Caches.nav(ctx);
			cache.update(NavPath.fromArchiveEntity(ctx.getCurrentEntity()));

			ctx.fileChanged();
			ctx.refreshView();
		});
		btnResizePopup.addActionListener(a -> resizePopup.showForEntity(ctx.getCurrentEntity(), btnResizePopup));

		JButton btnLoadFromNavMap = new JButton(I.tr("Loading Intersections from NavMap"));
		add(btnLoadFromNavMap, "spanx 3");
		btnLoadFromNavMap.addActionListener(a -> loadFromNavMap());
	}

	@Override
	public String getTabTitle() {
		return "NavPath";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCNavPath_PS.class);
	}

	private void processNavPath() {
		NavCache cache = Caches.nav(ctx);
		NavCalcFromNavCache navCalc = new NavCalcFromNavCache(ctx.getNavMapManager().getNavMap(true), cache);

		Pair<List<bCVector>, List<Float>> sticks = parseNavPathSticks();
		if (sticks == null) {
			return;
		}

		eCEntity entity = ctx.getCurrentEntity();
		bCMatrix worldMatrix = entity.getWorldMatrix();
		if (rbAbsolut.isSelected()) {
			// NavPath umpositionieren, wenn behalten Option nicht gewählt
			if (!cbManualCoords.isSelected()) {
				worldMatrix.setToTranslation(bCVector.averageVector(sticks.el0()));
			}

			bCMatrix inv = worldMatrix.getInverted();
			sticks.el0().forEach(p -> p.transform(inv));
		}

		NavPath navPath = new NavPath(entity.getGuid(), sticks.el0(), sticks.el1(), worldMatrix);

		// Detect zones
		String zoneA = navCalc.detectZoneForNavPath(navPath, IntersectZone.ZoneA).orElse(null);
		String zoneB = navCalc.detectZoneForNavPath(navPath, IntersectZone.ZoneB).orElse(null);

		// Detect zones intersections
		ZonePathIntersection calcZoneAIntersection = navCalc.calcZonePathIntersection(navPath.getPoints(), navPath.getRadius(),
				navPath.getWorldMatrix(), zoneA, IntersectZone.ZoneA);
		ZonePathIntersection calcZoneBIntersection = navCalc.calcZonePathIntersection(navPath.getPoints(), navPath.getRadius(),
				navPath.getWorldMatrix(), zoneB, IntersectZone.ZoneB);

		// Position of NavPath has been changed.
		if (!worldMatrix.equals(entity.getWorldMatrix())) {
			ctx.modifyEntityMatrix(entity, e -> e.setToWorldMatrix(worldMatrix));
		}

		// TODO: Gothic 3 initialisiert die LocalNodeBoundary mit min = max = Null-Vektor - WARUM?
		bCBox localNodeBoundary = StreamEx.zip(sticks.el0(), sticks.el1(), bCBox::new).reduce(new bCBox(), bCBox::getMerged);
		entity.updateLocalNodeBoundary(localNodeBoundary);

		G3Class clazz = entity.getClass(CD.gCNavPath_PS.class);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin1, calcZoneAIntersection.zoneIntersectionMargin1);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin2, calcZoneAIntersection.zoneIntersectionMargin2);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionCenter, calcZoneAIntersection.zoneIntersectionCenter);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin1, calcZoneBIntersection.zoneIntersectionMargin1);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin2, calcZoneBIntersection.zoneIntersectionMargin2);
		clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionCenter, calcZoneBIntersection.zoneIntersectionCenter);

		clazz.property(CD.gCNavPath_PS.Point).setEntries(sticks.el0());
		clazz.property(CD.gCNavPath_PS.Radius).setNativeEntries(sticks.el1());

		clazz.property(CD.gCNavPath_PS.ZoneAEntityID).setGuid(zoneA);
		clazz.property(CD.gCNavPath_PS.ZoneBEntityID).setGuid(zoneB);

		// Update NavCache
		cache.update(NavPath.fromArchiveEntity(entity));

		ctx.fileChanged();
		ctx.refreshView();
	}

	private Pair<List<bCVector>, List<Float>> parseNavPathSticks() {
		List<bCVector> points;
		List<Float> radius;
		try {
			points = Misc.parseVectorList(taSticks.getText());
			radius = Misc.parseFloatList(taRadius.getText());
		} catch (NumberFormatException e1) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("The radius data is malformed."));
			return null;
		} catch (IllegalArgumentException e1) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("The stick data is malformed."));
			return null;
		}

		if (points.size() != radius.size()) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("The number of sticks and radii do not match."));
			return null;
		}

		if (points.size() < 2) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("A NavPath must consist of at least 2 sticks."));
			return null;
		}

		return Pair.of(points, radius);
	}

	@Override
	public void loadValues(eCEntity entity) {
		G3Class navPath = entity.getClass(CD.gCNavPath_PS.class);

		cbUnlimitedHeight.setSelected(navPath.property(CD.gCNavPath_PS.UnlimitedHeight).isBool());

		tfZoneA.setText(navPath.property(CD.gCNavPath_PS.ZoneAEntityID).getGuid());
		tfZoneB.setText(navPath.property(CD.gCNavPath_PS.ZoneBEntityID).getGuid());

		tfAISM1.setText(navPath.property(CD.gCNavPath_PS.ZoneAIntersectionMargin1).toString());
		tfAISM2.setText(navPath.property(CD.gCNavPath_PS.ZoneAIntersectionMargin2).toString());
		tfAISC.setText(navPath.property(CD.gCNavPath_PS.ZoneAIntersectionCenter).toString());
		tfBISM1.setText(navPath.property(CD.gCNavPath_PS.ZoneBIntersectionMargin1).toString());
		tfBISM2.setText(navPath.property(CD.gCNavPath_PS.ZoneBIntersectionMargin2).toString());
		tfBISC.setText(navPath.property(CD.gCNavPath_PS.ZoneBIntersectionCenter).toString());

		List<bCVector> points = navPath.property(CD.gCNavPath_PS.Point).getEntries();
		loadSticks(entity, points);

		List<Float> radius = navPath.property(CD.gCNavPath_PS.Radius).getNativeEntries();
		taRadius.setText(null);
		Iterator<Float> radiusIterator = radius.iterator();
		while (radiusIterator.hasNext()) {
			taRadius.append(Misc.formatFloat(radiusIterator.next()));
			if (radiusIterator.hasNext()) {
				taRadius.append("\n");
			}
		}

		btnResizePopup.setEnabled(points.size() == 2 && radius.size() == 2);
	}

	@Override
	public void saveValues(eCEntity entity) {
		entity.getProperty(CD.gCNavPath_PS.UnlimitedHeight).setBool(cbUnlimitedHeight.isSelected());
	}

	private void loadFromNavMap() {
		NavMap navMap = ctx.getNavMapManager().getNavMap(true);
		if (navMap == null) {
			return;
		}

		eCEntity entity = ctx.getCurrentEntity();

		if (!navMap.hasNavPath(entity.getGuid())) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("NavPath is not in the NavMap."));
			return;
		}

		Optional<NavPath> optNavPath = navMap.getNavPath(entity.getGuid());
		if (optNavPath.isPresent()) {
			NavPath navPath = optNavPath.get();
			G3Class clazz = entity.getClass(CD.gCNavPath_PS.class);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin1, navPath.zoneAIntersection.zoneIntersectionMargin1);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin2, navPath.zoneAIntersection.zoneIntersectionMargin2);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionCenter, navPath.zoneAIntersection.zoneIntersectionCenter);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin1, navPath.zoneBIntersection.zoneIntersectionMargin1);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin2, navPath.zoneBIntersection.zoneIntersectionMargin2);
			clazz.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionCenter, navPath.zoneBIntersection.zoneIntersectionCenter);

			clazz.property(CD.gCNavPath_PS.ZoneAEntityID).setGuid(navPath.zoneAGuid);
			clazz.property(CD.gCNavPath_PS.ZoneBEntityID).setGuid(navPath.zoneBGuid);

			ctx.fileChanged();
			ctx.refreshView();
		} else {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("Entry in the NavMap is faulty."));
		}
	}

	private static class JNavPathResizePopup extends JPopupMenu {
		private FloatSpinner spLengthA;
		private FloatSpinner spWidthLeftA;
		private FloatSpinner spWidthRightA;
		private FloatSpinner spLengthB;
		private FloatSpinner spWidthLeftB;
		private FloatSpinner spWidthRightB;
		private JCheckBox cbLockWidthLeft;
		private JCheckBox cbLockWidthRight;
		private G3Class navPath;

		public JNavPathResizePopup(Window parent, Runnable resizedCallback) {
			spLengthA = new FloatSpinner();
			spWidthLeftA = new FloatSpinner();
			spWidthRightA = new FloatSpinner();
			spLengthB = new FloatSpinner();
			spWidthLeftB = new FloatSpinner();
			spWidthRightB = new FloatSpinner();
			cbLockWidthLeft = new JCheckBox("", true);
			cbLockWidthRight = new JCheckBox("", true);

			JButton btnResize = new JButton(I.trc("Resize NavPath", "Resize"));
			btnResize.setFocusable(false);

			setLayout(new MigLayout("fillx, ins 0"));
			add(spWidthLeftA, "width 100!");
			add(spLengthA, "width 100!");
			add(spWidthRightA, "width 100!, wrap");
			add(SwingUtils.monospaceFont(new JLabel(I.trc("Resize NavPath", "Locked") + " |")), "alignx left, sgy lock, split 2");
			add(cbLockWidthLeft, "sgy lock");
			add(btnResize, "alignx center");
			add(cbLockWidthRight, "alignx right, sgy lock, split 2");
			add(SwingUtils.monospaceFont(new JLabel("| " + I.trc("Resize NavPath", "Locked"))), "sgy lock, wrap");
			add(spWidthLeftB, "width 100!");
			add(spLengthB, "width 100!");
			add(spWidthRightB, "width 100!");

			setupPropagation(spWidthLeftA, spWidthLeftB, cbLockWidthLeft);
			setupPropagation(spWidthLeftB, spWidthLeftA, cbLockWidthLeft);
			setupPropagation(spWidthRightA, spWidthRightB, cbLockWidthRight);
			setupPropagation(spWidthRightB, spWidthRightA, cbLockWidthRight);

			btnResize.addActionListener(a -> doResize(parent, resizedCallback));
		}

		private void setupPropagation(FloatSpinner source, FloatSpinner target, JCheckBox propagationEnabled) {
			source.addChangeListener(e -> {
				if (propagationEnabled.isSelected()) {
					target.setVal(source.getVal());
				}
			});
		}

		/**
		 * Set model value and afterwards commit the value displayed in the editor field, to bring
		 * the value stored in the model and the displayed value in sync (rounding, as the displayed
		 * values only has a precision of four digits). Otherwise the call to
		 * {@link FloatSpinner#commitEdit()} during resize could trip up the locked value
		 * propagation.
		 *
		 * @param spinner
		 * @param value
		 */
		private void setValue(FloatSpinner spinner, float value) {
			spinner.setVal(value);
			try {
				spinner.commitEdit();
			} catch (ParseException e) {
				// Should not happen...
			}
		}

		public void showForEntity(eCEntity entity, JComponent invoker) {
			navPath = entity.getClass(CD.gCNavPath_PS.class);
			List<bCVector> points = navPath.property(CD.gCNavPath_PS.Point).getEntries();
			List<Float> radius = navPath.property(CD.gCNavPath_PS.Radius).getNativeEntries();

			float pathLength = points.get(1).getInvTranslated(points.get(0)).length() / 2;

			setValue(spLengthA, pathLength);
			setValue(spLengthB, pathLength);
			cbLockWidthLeft.setSelected(radius.get(0).equals(radius.get(1)));
			cbLockWidthRight.setSelected(radius.get(0).equals(radius.get(1)));
			setValue(spWidthLeftA, radius.get(0));
			setValue(spWidthRightA, radius.get(0));
			setValue(spWidthLeftB, radius.get(1));
			setValue(spWidthRightB, radius.get(1));

			show(invoker, 0, invoker.getHeight());
		}

		private void doResize(Window parent, Runnable resizedCallback) {
			try {
				spLengthA.commitEdit();
				spLengthB.commitEdit();
				spWidthLeftA.commitEdit();
				spWidthRightA.commitEdit();
				spWidthLeftB.commitEdit();
				spWidthRightB.commitEdit();
			} catch (ParseException e) {
				TaskDialogs.inform(parent, "", I.trf("Invalid input: {0}", e.getMessage()));
			}

			List<bCVector> points = navPath.property(CD.gCNavPath_PS.Point).getEntries(bCVector::clone);
			List<Float> radius = navPath.property(CD.gCNavPath_PS.Radius).getNativeEntries();

			float pathLength = points.get(1).getInvTranslated(points.get(0)).length() / 2;
			bCVector direction = points.get(1).getInvTranslated(points.get(0)).normalize();
			bCVector pointA = points.get(0).getInvTranslated(direction.getScaled(spLengthA.getVal() - pathLength));
			bCVector pointB = points.get(1).getTranslated(direction.getScaled(spLengthB.getVal() - pathLength));

			float widthA = spWidthLeftA.getVal() + spWidthRightA.getVal();
			float widthB = spWidthLeftB.getVal() + spWidthRightB.getVal();
			if (widthA <= 0.0f || widthB <= 0.0f) {
				return;
			}

			bCVector2 perpendicular = pointB.to2D().invTranslate(pointA.to2D()).perpendicular().normalize();
			float deltaA = (spWidthRightA.getVal() - spWidthLeftA.getVal()) / 2;
			float deltaB = (spWidthRightB.getVal() - spWidthLeftB.getVal()) / 2;

			bCVector deltaVectorA = perpendicular.getScaled(deltaA).to3D(0);
			pointA.translate(deltaVectorA);
			bCVector deltaVectorB = perpendicular.getScaled(deltaB).to3D(0);
			pointB.translate(deltaVectorB);

			points.set(0, pointA);
			points.set(1, pointB);
			navPath.property(CD.gCNavPath_PS.Point).setEntries(points);
			radius.set(0, widthA / 2);
			radius.set(1, widthB / 2);
			navPath.property(CD.gCNavPath_PS.Radius).setNativeEntries(radius);

			// Hide popup menu
			setVisible(false);

			// Notify callback
			resizedCallback.run();
		}
	}
}
