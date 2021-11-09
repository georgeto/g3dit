package de.george.g3dit.tab.archive.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;

import com.teamunify.i18n.I;

import de.george.g3dit.config.ConfigFiles;
import de.george.g3dit.config.NegCirclePrototypeConfigFile;
import de.george.g3dit.entitytree.EntityTree;
import de.george.g3dit.entitytree.TreeRenderer;
import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.navmap.data.NegCircle;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import de.george.navmap.util.NegCircleCalc;
import net.miginfocom.swing.MigLayout;

public class NegCircleView extends JPanel implements ArchiveView {
	private final NegCirclePrototypeConfigFile negCirclePrototypes;

	private NegCircleCalc calc;
	private NegCircleTreeExtension extension;

	private JLabel lblEntitiesSel;
	private JButton btnCreateCircles, btnRemoveCircles;

	private JTextArea taLog;

	private JCheckBox cbPrintGuid, cbPrintPosition;

	private EditorArchiveTab ctx;

	public NegCircleView(EditorArchiveTab inEditor) {
		ctx = inEditor;
		negCirclePrototypes = ConfigFiles.negCirclePrototypes(ctx);

		calc = new NegCircleCalc();
		negCirclePrototypes.addContentChangedListener(this, calc::setNegCirclePrototypes);

		setLayout(new MigLayout("fill", "[]30[][grow]push[]", "[][][][]10[grow]"));

		JLabel lblModify = SwingUtils.createBoldLabel(I.tr("Create & Remove"));
		add(lblModify, "cell 0 0");

		lblEntitiesSel = new JLabel();
		this.add(lblEntitiesSel, "cell 0 1, gapleft 7");

		btnCreateCircles = new JButton(I.tr("Create NegCircles"));
		btnCreateCircles.setEnabled(false);
		this.add(btnCreateCircles, "cell 0 2, gapleft 7");

		btnRemoveCircles = new JButton(I.tr("Remove NegCircles"));
		btnRemoveCircles.setEnabled(false);
		this.add(btnRemoveCircles, "cell 0 3, gapleft 7");

		JButton btnRemoveCirclesManual = new JButton(I.tr("Remove manually (specify guids)"));
		btnRemoveCirclesManual.setEnabled(true);
		this.add(btnRemoveCirclesManual, "cell 1 3, gapleft 7");

		cbPrintPosition = new JCheckBox(I.tr("Position"), true);
		this.add(cbPrintPosition, "cell 4 3, split 2");

		cbPrintGuid = new JCheckBox(I.tr("Guid"), false);
		this.add(cbPrintGuid, "cell 4 3");

		taLog = new JTextAreaExt(true);
		taLog.setEditable(false);
		JScrollPane scrollLog = new JScrollPane(taLog);
		scrollLog.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		this.add(scrollLog, "cell 0 4, span, grow");

		btnCreateCircles.addActionListener((e) -> handleAddNegCircle());

		btnRemoveCircles.addActionListener((e) -> handleRemoveNegCircle());

		btnRemoveCirclesManual.addActionListener((e) -> handleRemoveCirclesManual());
	}

	private void log(String message) {
		taLog.append(message + "\n");
	}

	private void logEntity(eCEntity entity) {
		String message = entity.toString();
		if (cbPrintGuid.isSelected()) {
			message += "(" + entity.getGuid() + ")";
		}
		if (cbPrintPosition.isSelected()) {
			message += ": " + entity.getWorldPosition().toString().replaceAll("\\.\\d+/", "/");
		}
		log(message);
	}

	@Override
	public void load(eCEntity entity) {
		taLog.setText(null);
		extension.cbHasPrototpye.doClick();

		entitySelectionChanged(null);
	}

	@Override
	public void save(eCEntity entity) {}

	@Override
	public void entitySelectionChanged(TreeSelectionEvent e) {
		EntityTree entityTree = ctx.getEntityTree();
		btnRemoveCircles.setEnabled(entityTree.getSelectedEntityCount() > 0);
		btnCreateCircles.setEnabled(entityTree.getSelectedEntityCount() > 0);
		lblEntitiesSel.setText(I.trf("Selected: {0}", entityTree.getSelectedEntityCount()));
	}

	@Override
	public Component getContent() {
		return this;
	}

	@Override
	public ITreeExtension getTreeExtension() {
		extension = new NegCircleTreeExtension();
		return extension;
	}

	@Override
	public void onEnter() {
		ctx.getEntityTree().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	private void handleAddNegCircle() {
		taLog.setText(null);
		NavMap navMap = getNavMap();
		if (navMap == null) {
			return;
		}

		NavCalc navCalc = ctx.getNavMapManager().getNavCalc(false);
		if (navCalc == null) {
			log(I.tr("NavCalc could not be created."));
			return;
		}

		for (eCEntity entity : ctx.getEntityTree().getSelectedEntities()) {
			logEntity(entity);
			if (navMap.hasNegCircle(entity.getGuid())) {
				log(I.tr("NegCircle entry already exists.") + "\n");
				continue;
			}

			if (!calc.hasNegCirclePrototype(entity)) {
				log(I.tr("No NegCircle prototype found for this entity.") + "\n");
				continue;
			}

			NegCircle circle = calc.createNegCircleFromEntity(entity, navCalc);
			try {
				navMap.addNegCircle(circle);
			} catch (Exception e) {
				log(I.trf("Critical Error! Saving the NavMap is NOT recommended: {0}", e.getMessage()) + "\n");
				continue;
			}

			for (String guid : circle.zoneGuids) {
				log(guid);
			}

			if (circle.zoneGuids.size() == 0) {
				log(I.tr("Created without NavZone!"));
			}

			log("");
		}
	}

	private void handleRemoveNegCircle() {
		taLog.setText(null);
		NavMap navMap = getNavMap();
		if (navMap == null) {
			return;
		}

		for (eCEntity entity : ctx.getEntityTree().getSelectedEntities()) {
			logEntity(entity);
			removeNegCircle(navMap, entity.getGuid());
		}
	}

	private void removeNegCircle(NavMap navMap, String guid) {
		if (!navMap.hasNegCircle(guid)) {
			log(I.tr("No NegCircle entry exists.") + "\n");
			return;
		}

		try {
			navMap.removeNegCircle(guid);
		} catch (IllegalStateException e) {
			log(I.trf("Critical Error! Saving the NavMap is NOT recommended: {0}", e.getMessage()) + "\n");
			return;
		}
		log(I.tr("Removed!") + "\n");
	}

	private void handleRemoveCirclesManual() {
		taLog.setText(null);
		NavMap navMap = getNavMap();
		if (navMap == null) {
			return;
		}

		JTextArea textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 500));

		int result = JOptionPane.showConfirmDialog(null, scrollPane, I.tr("Enter the guids of the NegCircles"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String[] lines = textArea.getText().split("\n");
			for (String line : lines) {
				String guid = GuidUtil.parseGuid(line);
				if (guid != null) {
					log(guid);
					removeNegCircle(navMap, guid);
				}
			}
		}
	}

	private NavMap getNavMap() {
		NavMap navMap = ctx.getNavMapManager().getNavMap(false);
		if (navMap == null) {
			log(I.tr("NavMap could not be loaded."));
			return null;
		}
		return navMap;
	}

	class NegCircleTreeExtension implements ITreeExtension {

		private JCheckBox cbHasPrototpye, cbNoNegCircrle, cbHasNegCircrle;

		@Override
		public void guiInit(JPanel extensionPanel, ActionListener extActionListener) {
			cbHasPrototpye = new JCheckBox(I.tr("Prototype available"));
			cbHasPrototpye.addActionListener(extActionListener);
			extensionPanel.add(cbHasPrototpye);

			cbNoNegCircrle = new JCheckBox(I.tr("NegCircle missing"));
			cbNoNegCircrle.addActionListener(extActionListener);
			extensionPanel.add(cbNoNegCircrle);

			cbHasNegCircrle = new JCheckBox(I.tr("NegCircle exists"));
			cbHasNegCircrle.addActionListener(extActionListener);
			extensionPanel.add(cbHasNegCircrle);
		}

		@Override
		public boolean filterLeave(eCEntity entity) {
			boolean show = true;
			if (cbNoNegCircrle.isSelected() || cbHasNegCircrle.isSelected()) {
				NavMap navMap = ctx.getNavMapManager().getNavMap(false);
				if (navMap == null) {
					log(I.tr("NavMap could not be loaded."));
				}
				if (cbNoNegCircrle.isSelected()) {
					show = navMap != null && !navMap.hasNegCircle(entity.getGuid());
				}
				if (cbHasNegCircrle.isSelected() && show) {
					show = navMap != null && navMap.hasNegCircle(entity.getGuid());
				}
			}
			if (cbHasPrototpye.isSelected() && show) {
				show = calc.hasNegCirclePrototype(entity);
			}
			return show;
		}

		@Override
		public boolean isFilterActive() {
			return cbHasPrototpye.isSelected() || cbNoNegCircrle.isSelected() || cbHasNegCircrle.isSelected();
		}

		@Override
		public void renderElement(TreeRenderer element, eCEntity entity, ArchiveFile file) {

		}
	}

	@Override
	public void cleanUp() {}
}
