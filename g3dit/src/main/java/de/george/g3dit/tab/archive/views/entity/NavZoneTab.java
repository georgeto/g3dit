package de.george.g3dit.tab.archive.views.entity;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.navmap.data.NavZone;
import de.george.navmap.data.Zone;
import net.miginfocom.swing.MigLayout;

public class NavZoneTab extends AbstractNavTab {
	private JTextField tfRadius, tfRadiusOffset;
	private JCheckBox cbZoneIsCCW;

	public NavZoneTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fill", "[][]push"));

		JLabel lblRadius = new JLabel(I.tr("Radius"));
		tfRadius = SwingUtils.createUndoTF();
		tfRadius.setEditable(false);
		tfRadius.setName("Radius");
		addValidators(tfRadius, StringValidators.REQUIRE_VALID_NUMBER);

		JLabel lblRadiusOffset = new JLabel(I.tr("RadiusOffset (x/y/z//)"));
		tfRadiusOffset = SwingUtils.createUndoTF();
		tfRadiusOffset.setEditable(false);

		cbZoneIsCCW = new JCheckBox();
		cbZoneIsCCW.setToolTipText(I.tr("Sticks sind entgegen dem Uhrzeigersinn ausgerichtet. (wird automatisch berechnet)"));

		add(lblRadius, "");
		add(lblRadiusOffset, "wrap");
		add(tfRadius, "width 50:100:100");
		add(tfRadiusOffset, "width 200:250:300, wrap");
		add(new JLabel(I.tr("ZoneIsCCW")), "wrap");
		add(cbZoneIsCCW, "wrap");

		setupComponents(I.tr("NavZone-Koordinaten beibehalten"));

		JButton btnCalc = new JButton(I.tr("Errechne NavZone aus Sticks"));
		add(btnCalc, "split 2, spanx 4");
		btnCalc.addActionListener(l -> handleCalcZone());

		add(cbManualCoords, "gapleft 15, wrap");
	}

	@Override
	public String getTabTitle() {
		return "NavZone";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCNavZone_PS.class);
	}

	@Override
	public void loadValues(eCEntity entity) {
		G3Class navZone = entity.getClass(CD.gCNavZone_PS.class);
		tfRadius.setText(Misc.formatFloat(navZone.property(CD.gCNavZone_PS.Radius).getFloat()));
		tfRadiusOffset.setText(navZone.property(CD.gCNavZone_PS.RadiusOffset).toString());
		cbZoneIsCCW.setSelected(navZone.property(CD.gCNavZone_PS.ZoneIsCCW).isBool());

		loadSticks(entity, navZone.property(CD.gCNavZone_PS.Point).getEntries());
	}

	@Override
	public void saveValues(eCEntity entity) {
		G3Class navZone = entity.getClass(CD.gCNavZone_PS.class);
		navZone.property(CD.gCNavZone_PS.ZoneIsCCW).setBool(cbZoneIsCCW.isSelected());
	}

	private void handleCalcZone() {
		List<bCVector> sticks = null;
		try {
			sticks = Misc.parseVectorList(taSticks.getText());
		} catch (IllegalArgumentException e) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("Mindestens für einen Stick wurden fehlerhafte Koordinaten eingegeben."));
			return;
		}
		if (sticks.size() < 3) {
			TaskDialogs.inform(ctx.getParentWindow(), "", I.tr("Eine NavZone muss aus mindestens 3 Sticks bestehen."));
			return;
		}

		eCEntity entity = ctx.getCurrentEntity();

		bCMatrix worldMatrix = entity.getWorldMatrix();

		// Koordinaten sind absolut angegeben
		if (rbAbsolut.isSelected()) {
			// Aktuelle Zonen Koordinaten behalten
			if (!cbManualCoords.isSelected()) {
				worldMatrix.setToTranslation(bCVector.averageVector(sticks));
			}

			bCMatrix invWorldMatrix = worldMatrix.getInverted();
			sticks.forEach(p -> p.transform(invWorldMatrix));
		}

		// RadiusOffset und Radius berechnen
		bCVector radiusOffset = bCVector.averageVector(sticks);
		float radius = sticks.stream().map(radiusOffset::getInvTranslated).map(bCVector::length).max(Float::compareTo).get();

		G3Class navZone = entity.getClass(CD.gCNavZone_PS.class);
		navZone.property(CD.gCNavZone_PS.Radius).setFloat(radius);
		navZone.setPropertyData(CD.gCNavZone_PS.RadiusOffset, radiusOffset);
		navZone.property(CD.gCNavZone_PS.Point).setEntries(sticks);

		Zone.arePointsCcw(sticks).ifPresent(cbZoneIsCCW::setSelected);
		saveValues(entity);

		// Position of NavZone has been changed.
		if (!worldMatrix.equals(entity.getWorldMatrix())) {
			ctx.modifyEntityMatrix(entity, e -> e.setToWorldMatrix(worldMatrix));
		}

		// TODO: Gothic 3 initialisiert die LocalNodeBoundary mit min = max = Null-Vektor - WARUM?
		bCBox localNodeBoundary = new bCBox();
		sticks.forEach(localNodeBoundary::merge);
		entity.updateLocalNodeBoundary(localNodeBoundary);

		// Update NavCache
		Caches.nav(ctx).update(NavZone.fromArchiveEntity(entity));

		ctx.fileChanged();
		ctx.refreshView();
	}
}
