package de.george.g3dit.tab.archive.views;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.LightCache;
import de.george.g3dit.entitytree.EntityTree;
import de.george.g3dit.entitytree.TreeRenderer;
import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.LightCalc;
import de.george.g3dit.util.LightCalc.StaticLightedResult;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCIlluminated_PS;
import de.george.lrentnode.classes.desc.CD;
import net.miginfocom.swing.MigLayout;

public class IlluminationView extends JPanel implements ArchiveView {
	private static final Logger logger = LoggerFactory.getLogger(IlluminationView.class);

	private EditorArchiveTab ctx;

	private JLabel lblStatusCache, lblLightCount;
	private JButton btnDoLighting;
	private JLabel lblEntitiesSel;
	private JTextArea taLog;
	private JCheckBox cbPrintResult;
	private JCheckBox cbPrintError;
	private JButton btnShowLighting;

	public IlluminationView(EditorArchiveTab inEditor) {
		ctx = inEditor;

		LightCache lightCache = Caches.light(ctx);
		lightCache.load();
		if (lightCache.isValid()) {
			logger.info("Fehler beim Laden des Light-Caches.");
		}

		lightCache.addUpdateListener(this, l -> updateLightCacheStatus(l));

		setLayout(new MigLayout("fill", "[]30[][grow]push[]", "[][][][]10[grow]"));

		JLabel lblLightCache = SwingUtils.createBoldLabel("Light-Cache");
		add(lblLightCache, "cell 0 0");

		lblStatusCache = new JLabel();
		add(lblStatusCache, "cell 0 1, gapleft 7");

		lblLightCount = new JLabel();
		add(lblLightCount, "cell 0 2, gapleft 7");

		final JButton btnCreateCache = new JButton("Cache erstellen");
		add(btnCreateCache, "cell 0 3, gapleft 7");

		JLabel lblEntityIllum = SwingUtils.createBoldLabel("Entities beleuchten");
		add(lblEntityIllum, "cell 1 0");

		lblEntitiesSel = new JLabel();
		this.add(lblEntitiesSel, "cell 1 1, gapleft 7");

		btnShowLighting = new JButton("Aktuelle Beleuchtung");
		btnShowLighting.setEnabled(false);
		this.add(btnShowLighting, "cell 1 2, gapleft 7, height 18:18:18");

		btnDoLighting = new JButton("Beleuchten");
		btnDoLighting.setEnabled(false);
		this.add(btnDoLighting, "cell 1 3, gapleft 7");

		cbPrintResult = new JCheckBox("Ergebnisse(+)", true);
		this.add(cbPrintResult, "cell 4 3, split 2");

		cbPrintError = new JCheckBox("Fehler(-)", true);
		this.add(cbPrintError, "cell 4 3");

		taLog = new JTextAreaExt(true);
		taLog.setEditable(false);
		JScrollPane scrollLog = new JScrollPane(taLog);
		scrollLog.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		this.add(scrollLog, "cell 0 4, span, grow");

		btnCreateCache.addActionListener(e -> handleCreateCache());

		btnDoLighting.addActionListener(e -> handleDoLighting());

		btnShowLighting.addActionListener(e -> handleShowLighting());
	}

	private void logResult(String message) {
		if (cbPrintResult.isSelected()) {
			log("+" + message);
		}
	}

	private void logError(String message) {
		if (cbPrintError.isSelected()) {
			log("-" + message);
		}
	}

	private void log(String message) {
		taLog.append(message + "\n");
	}

	private void updateLightCacheStatus(LightCache lightCache) {
		if (lightCache.isValid()) {
			lblStatusCache.setText("Status: Vorhanden (" + lightCache.printCreationTimestamp() + ")");
			lblLightCount.setText("Lichtquellen: " + lightCache.getEntries().size());
		} else {
			lblStatusCache.setText("Status: Existiert nicht!");
			lblLightCount.setText("Lichtquellen: -");
		}

	}

	@Override
	public void load(eCEntity entity) {
		taLog.setText(null);
		entitySelectionChanged(null);
	}

	@Override
	public void save(eCEntity entity) {}

	@Override
	public void entitySelectionChanged(TreeSelectionEvent e) {
		EntityTree entityTree = ctx.getEntityTree();
		btnDoLighting.setEnabled(entityTree.getSelectedEntityCount() > 0);
		btnShowLighting.setEnabled(entityTree.getSelectedEntityCount() > 0);
		lblEntitiesSel.setText("Ausgewählt: " + entityTree.getSelectedEntityCount());
	}

	@Override
	public Component getContent() {
		return this;
	}

	@Override
	public ITreeExtension getTreeExtension() {
		return new IlluminationTreeExtension();
	}

	@Override
	public void onEnter() {
		ctx.getEntityTree().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	@Override
	public void cleanUp() {
		Caches.light(ctx).removeUpdateListeners(this);
	}

	private void handleCreateCache() {
		ctx.getCacheManager().createCache(LightCache.class);
	}

	private void handleShowLighting() {
		taLog.setText(null);
		for (eCEntity entity : ctx.getEntityTree().getSelectedEntities()) {
			if (!entity.hasClass(CD.eCIlluminated_PS.class)) {
				logError(entity.getName() + "(" + entity.getGuid() + ") ist nicht beleuchtet, keine eCIlluminated_PS Klasse.");
				continue;
			}

			eCIlluminated_PS illuminated = entity.getClass(CD.eCIlluminated_PS.class);

			// Beleuchtungstyp auf eEStaticLighingType_Instance umstellen
			StaticLightedResult result = LightCalc.isStaticLighted(entity);

			if (result == StaticLightedResult.MeshNotStatic) {
				logError(entity.getName() + "(" + entity.getGuid() + ") ist nicht beleuchtet, kein statisches Mesh.");
				return;
			}

			if (result == StaticLightedResult.WrongLightingType) {
				logResult(entity.getName() + "(" + entity.getGuid() + ") ist mit einer Lightmap beleuchtet.");
				return;
			}

			if (result == StaticLightedResult.Valid) {
				if (illuminated.lights.getLights().size() > 0) {
					logResult(entity.getName() + "(" + entity.getGuid() + ") ist mit " + illuminated.lights.getLights().size()
							+ " Lichtquellen beleuchtet.");
				} else {
					logResult(entity.getName() + "(" + entity.getGuid() + ") ist mit keiner Lichtquelle beleuchtet.");
				}
			}
		}
	}

	private void handleDoLighting() {
		LightCache lightCache = Caches.light(ctx);
		if (!lightCache.isValid()) {
			TaskDialogs.inform(ctx.getParentWindow(), "Light-Cache existiert nicht",
					"Bevor Entities beleuchtet werden können, muss der Light-Cache erstellt werden.");
			return;
		}
		taLog.setText(null);
		for (eCEntity entity : ctx.getEntityTree().getSelectedEntities()) {
			int result = LightCalc.calcLighting(entity, lightCache);
			switch (result) {
				case -3:
					logError(entity.getName() + "(" + entity.getGuid()
							+ ") wurde nicht beleuchtet, StaticLightingType ungleich eEStaticLightingType_Instance.");
					break;
				case -2:
					logError(entity.getName() + "(" + entity.getGuid() + ") wurde nicht beleuchtet, keine statisches Mesh.");
					break;
				case -1:
					logError(entity.getName() + "(" + entity.getGuid() + ") wurde nicht beleuchtet, keine eCIlluminated_PS Klasse.");
					break;
				case 0:
					logError(entity.getName() + "(" + entity.getGuid() + ") hatte keine Lichtquelle in der Nähe.");
					break;
				default:
					if (result > 0) {
						logResult(entity.getName() + "(" + entity.getGuid() + ") wurde mit " + result + " Lichtquellen beleuchtet.");
					}
					break;
			}
		}
	}

	static class IlluminationTreeExtension implements ITreeExtension {

		private JCheckBox cbOnlyIlluminatable;

		@Override
		public void guiInit(JPanel extensionPanel, ActionListener extActionListener) {
			cbOnlyIlluminatable = new JCheckBox("Nur beleuchtbare Entities");
			cbOnlyIlluminatable.addActionListener(extActionListener);
			extensionPanel.add(cbOnlyIlluminatable);
		}

		@Override
		public boolean filterLeave(eCEntity entity) {
			if (cbOnlyIlluminatable.isSelected()) {
				return entity.hasClass(CD.eCIlluminated_PS.class) && LightCalc.isStaticLighted(entity) == StaticLightedResult.Valid;
			}

			return true;
		}

		@Override
		public boolean isFilterActive() {
			return cbOnlyIlluminatable.isSelected();
		}

		@Override
		public void renderElement(TreeRenderer element, eCEntity entity, ArchiveFile file) {

		}
	}
}
