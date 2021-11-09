package de.george.g3dit.gui.dialogs;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXLabel;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;

public class TemplateNameSearchDialog extends ExtStandardDialog {
	protected TemplateSearchListener callback;
	protected TemplateFile tpleFile;

	protected JXLabel lblStatus;
	protected JTextField tfTemplateName;
	protected Action saveAction;

	protected EditorContext ctx;

	protected String defaultTpleName;

	private JButton btnLoadTple;

	public TemplateNameSearchDialog(TemplateSearchListener callback, EditorContext ctx) {
		this(callback, ctx, I.tr("Load template"), "");
	}

	public TemplateNameSearchDialog(TemplateSearchListener callback, EditorContext ctx, String defaultTpleName) {
		this(callback, ctx, I.tr("Load template"), defaultTpleName);
	}

	public TemplateNameSearchDialog(TemplateSearchListener callback, EditorContext ctx, String title, String defaultTpleName) {
		super(ctx.getParentWindow(), title, true);
		this.callback = callback;
		this.ctx = ctx;
		this.defaultTpleName = defaultTpleName;

		setType(Type.UTILITY);
		setResizable(false);
		setSize(280, 140);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fillx", "[][][]"));

		JLabel lblNewLabel = new JLabel(I.tr("Template name"));
		mainPanel.add(lblNewLabel, "wrap");

		tfTemplateName = SwingUtils.createUndoTF(defaultTpleName);
		mainPanel.add(tfTemplateName, "width 200!");

		TemplateCache tpleCache = Caches.template(ctx);
		if (tpleCache.isValid()) {
			new TemplateIntelliHints(tfTemplateName, tpleCache);
		}

		btnLoadTple = new JButton(I.tr("Search"));
		mainPanel.add(btnLoadTple, "wrap");

		lblStatus = new JXLabel();
		lblStatus.setLineWrap(true);
		mainPanel.add(lblStatus, "spanx 3, grow");

		btnLoadTple.addActionListener(e -> {
			if (tfTemplateName.getText().isEmpty()) {
				lblStatus.setText(I.tr("Please enter template name!"));
				return;
			}
			lblStatus.setText(I.tr("Search..."));
			search();
		});

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		saveAction = SwingUtils.createAction(I.tr("Apply"), this::callListener);
		saveAction.setEnabled(false);

		addButton(buttonPanel, saveAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	@Override
	public Component getInitFocusedComponent() {
		if (defaultTpleName.isEmpty()) {
			return tfTemplateName;
		} else {
			return btnLoadTple;
		}
	}

	protected void search() {
		tpleFile = FileUtil.openTemplateByName(ctx.getFileManager().listTemplateFiles(), tfTemplateName.getText());
		if (tpleFile != null) {
			saveAction.setEnabled(true);
			lblStatus.setText(I.trf("Template ''{0}'' found.", tfTemplateName.getText()));
		} else {
			saveAction.setEnabled(false);
			lblStatus.setText(I.trf("Template ''{0}'' NOT found.", tfTemplateName.getText()));
		}
	}

	protected void callListener() {
		if (tpleFile != null) {
			if (callback.templateSearchCallback(tpleFile)) {
				dispose();
			} else {
				lblStatus.setText(I.tr("Template does not fit."));
			}
		}
	}

	@FunctionalInterface
	public static interface TemplateSearchListener {
		public boolean templateSearchCallback(TemplateFile tple);
	}

}
