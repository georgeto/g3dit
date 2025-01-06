package de.george.g3dit.tab.template.views.header;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;

import com.teamunify.i18n.I;

import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JFocusNameField;
import de.george.g3dit.gui.components.JSearchGuidField;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.dialogs.TemplateSearchDialog;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.tab.shared.BoundingBoxPanel;
import de.george.g3dit.tab.shared.PositionPanel;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.Tooltips;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.g3utils.validation.IsALongValidator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import net.miginfocom.swing.MigLayout;

public class AllgemeinTab extends AbstractTemplateTab {
	private JFocusNameField tfName;
	private UndoableTextField tfFilePrefix, tfChangeTime;

	private JGuidField tfItemGuid;
	private JSearchGuidField tfRefGuid;

	private PositionPanel plWorldPosition;
	private BoundingBoxPanel plLocalNodeBoundary;

	public AllgemeinTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("", "[][]push[grow]"));

		DocumentListener documentListener = SwingUtils.createDocumentListener(this::handleUpdateDataFile);

		add(new JLabel(I.tr("Name")), "wrap");
		tfName = new JFocusNameField(ctx);
		tfName.initValidation(validation(), I.tr("Name"), EmtpyWarnValidator.INSTANCE);
		tfName.getDocument().addDocumentListener(documentListener);
		add(tfName, "width 100:300:300, wrap");

		add(new JLabel(I.tr("File prefix")), "wrap");
		tfFilePrefix = SwingUtils.createUndoTF();
		tfFilePrefix.getDocument().addDocumentListener(documentListener);
		add(tfFilePrefix, "width 100:300:300, wrap");

		add(new JLabel(I.tr("Item Guid")), "wrap");
		tfItemGuid = new JGuidField();
		tfItemGuid.initValidation(validation(), I.tr("Item Guid"), GuidValidator.INSTANCE);
		add(tfItemGuid, "width 100:300:300");
		JButton btnRandomItemGuid = new JButton(Icons.getImageIcon(Icons.Data.COUNTER));
		btnRandomItemGuid.setToolTipText(I.tr("Generate random guid"));
		btnRandomItemGuid.addActionListener(a -> tfItemGuid.setText(GuidUtil.randomGUID(), true));
		add(btnRandomItemGuid, LayoutUtils.sqrBtn());

		JButton btnCopyEntry = new JButton(Icons.getImageIcon(Icons.Action.COPY));
		btnCopyEntry.setToolTipText(I.tr("Copy .lrtpldatasc entry to clipboard"));
		btnCopyEntry.addActionListener(a -> IOUtils.copyToClipboard(
				GuidUtil.hexToLrtpldatasc(tfItemGuid.getText()) + "=" + tfFilePrefix.getText() + "_" + tfName.getText() + ".tple"));
		add(btnCopyEntry, LayoutUtils.sqrBtn("wrap"));

		add(new JLabel(I.tr("Reference Guid")), "wrap");
		tfRefGuid = new JSearchGuidField(ctx);
		tfRefGuid.initValidation(validation(), I.tr("Reference Guid"), GuidValidator.INSTANCE);
		add(tfRefGuid, "width 100:300:300");

		tfRefGuid.addMenuItem(I.tr("List all references in entities to this template"), Icons.getImageIcon(Icons.Misc.GLOBE),
				(ctx, g) -> EntitySearchDialog.openEntitySearchGuid(ctx, MatchMode.Template, g));
		tfRefGuid.addMenuItem(I.tr("List all references in templates to this template"), Icons.getImageIcon(Icons.Misc.GLOBE),
				(ctx, g) -> TemplateSearchDialog.openTemplateSearchGuid(ctx, MatchMode.Template, g));

		JButton btnRandomRefGuid = new JButton(Icons.getImageIcon(Icons.Data.COUNTER));
		btnRandomRefGuid.setToolTipText(I.tr("Generate random guid"));
		btnRandomRefGuid.addActionListener(a -> tfRefGuid.setText(GuidUtil.randomGUID(), true));
		add(btnRandomRefGuid, LayoutUtils.sqrBtn());

		JButton btnCopyGuid = new JButton(Icons.getImageIcon(Icons.Action.COPY));
		btnCopyGuid.setToolTipText(I.tr("Copy Guid to Clipboard"));
		btnCopyGuid.addActionListener(a -> IOUtils.copyToClipboard(tfRefGuid.getText()));
		add(btnCopyGuid, LayoutUtils.sqrBtn("wrap"));

		JLabel lblChangeTime = new JLabel(I.tr("ChangeTime"));
		lblChangeTime.setToolTipText(Tooltips.changeTime());
		add(lblChangeTime, "wrap");
		tfChangeTime = SwingUtils.createUndoTF();
		tfChangeTime.setName(I.tr("ChangeTime"));
		tfChangeTime.setToolTipText(Tooltips.changeTime());
		addValidators(tfChangeTime, IsALongValidator.INSTANCE);
		add(tfChangeTime, "width 50:100:100, wrap");

		plWorldPosition = new PositionPanel(I.tr("Position"), ctx.getParentWindow(), this::changeWorldPosition);
		add(plWorldPosition, "width 100:300:400, spanx 3, grow, wrap");

		plLocalNodeBoundary = new BoundingBoxPanel(I.tr("BoundingBox"), ctx, this::changeLocalNodeBoundary, () -> {
			ctx.saveView(); // Mesh wurde eventuell im Mesh Tab bearbeitet
			return EntityUtil.getMesh(ctx.getCurrentTemplate().getReferenceHeader()).orElse(null);
		}, () -> tfName.getText());
		add(plLocalNodeBoundary, "width 100:300:400, spanx 3, grow, wrap");
	}

	@Override
	public String getTabTitle() {
		return I.tr("General");
	}

	@Override
	public boolean isActive(TemplateFile entity) {
		return true;
	}

	@Override
	public void loadValues(TemplateFile tple) {
		tfName.setText(tple.getEntityName());
		tfFilePrefix.setText(tple.getTemplateContext());
		tfItemGuid.setText(tple.getItemHeader().getGuid());
		tfRefGuid.setText(tple.getReferenceHeader().getGuid());
		tfChangeTime.setText(String.valueOf(tple.getReferenceHeader().getDataChangedTimeStamp()));

		TemplateEntity refEntity = tple.getReferenceHeader();
		plWorldPosition.setPositionMatrix(refEntity.getWorldMatrix());
		plLocalNodeBoundary.setBoundingBox(refEntity.getLocalNodeBoundary());
	}

	@Override
	public void saveValues(TemplateFile tple) {
		tple.setEntityName(tfName.getText());
		tple.setFileName(tfFilePrefix.getText() + "_" + tfName.getText() + ".tple");
		tple.getReferenceHeader().setName(tfName.getText());
		tple.getItemHeader().setGuid(GuidUtil.parseGuid(tfItemGuid.getText()));
		tple.getReferenceHeader().setGuid(GuidUtil.parseGuid(tfRefGuid.getText()));
		tple.getReferenceHeader().setDataChangedTimeStamp(Long.valueOf(tfChangeTime.getText()));
	}

	private void changeWorldPosition(bCMatrix worldMatrix) {
		ctx.getCurrentTemplate().getReferenceHeader().setToWorldMatrix(worldMatrix);
		ctx.fileChanged();
		ctx.refreshView();
	}

	private void changeLocalNodeBoundary(bCBox box) {
		ctx.getCurrentTemplate().getReferenceHeader().updateLocalNodeBoundary(box);
		ctx.fileChanged();
		ctx.refreshView();
	}

	private void handleUpdateDataFile() {
		if (ctx.getDataFile().isPresent()) {
			ctx.setDataFile(ctx.getDataFile().get().resolveSibling(tfFilePrefix.getText() + "_" + tfName.getText() + ".tple"));
		}
	}

	@Override
	public void registerKeyStrokes(JComponent container) {
		container.registerKeyboardAction(a -> {
			tfItemGuid.setText(GuidUtil.randomGUID(), true);
			tfRefGuid.setText(GuidUtil.randomGUID(), true);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		System.out.println("registerKeyStrokes");
	}

	@Override
	public void unregisterKeyStrokes(JComponent container) {
		container.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		System.out.println("unregisterKeyStrokes");
	}
}
