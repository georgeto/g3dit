package de.george.g3dit.tab.template;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;
import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.template.TemplateContentPane.TemplateViewType;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.StringtableHelper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.Saveable;
import de.george.g3utils.structure.Guid;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.template.Lrtpldatasc;
import de.george.lrentnode.template.Lrtpldatasc.DuplicateEntryException;
import de.george.lrentnode.template.Lrtpldatasc.InvalidEntryException;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;
import net.tomahawk.ExtensionsFilter;

public class EditorTemplateTab extends EditorAbstractFileTab {
	private static final Logger logger = LoggerFactory.getLogger(EditorTemplateTab.class);

	private TemplateContentPane contentPane;
	private JPanel statusBarExtension;
	private TemplateFile currentTemplate;
	private Color titleColor = Color.BLACK;
	private String tooltip = null;

	public EditorTemplateTab(EditorContext ctx) {
		super(ctx, EditorTabType.Template);
		contentPane = new TemplateContentPane(this);
		contentPane.initGUI();
		ctx.eventBus().register(this);
	}

	private void initStatusBarExtension() {
		statusBarExtension = new JPanel(new MigLayout("ins 0"));
		statusBarExtension.setOpaque(false);
		JToggleButton btnEntity = new JToggleButton("E", true);
		btnEntity.setToolTipText(I.tr("Edit template"));
		btnEntity.setMargin(new Insets(0, 0, 0, 0));
		btnEntity.addActionListener(l -> contentPane.showView(TemplateViewType.ENTITY));
		btnEntity.setMnemonic(KeyEvent.VK_1);
		JToggleButton btnPropertySheet = new JToggleButton("P", false);
		btnPropertySheet.setToolTipText("Property");
		btnPropertySheet.setMargin(new Insets(0, 0, 0, 0));
		btnPropertySheet.addActionListener(l -> contentPane.showView(TemplateViewType.PROPERTY));
		btnPropertySheet.setMnemonic(KeyEvent.VK_2);
		statusBarExtension.add(btnEntity, "width 20!, height 20!");
		statusBarExtension.add(btnPropertySheet, "width 20!, height 20!");
		SwingUtils.createButtonGroup(btnEntity, btnPropertySheet);
	}

	private void updateStatusBar() {
		if (statusBarExtension == null) {
			initStatusBarExtension();
		}
		getStatusBar().setExtensionPanel(statusBarExtension);

	}

	@Subscribe
	public void onTabSelected(EditorTab.SelectedEvent event) {
		if (event.getTab().isPresent() && event.getTab().get().equals(this)) {
			updateStatusBar();

			if (getDataFile().isPresent()) {
				displayTemplateContextStatus();
			}
		}

	}

	@Override
	public Icon getTabIcon() {
		return Icons.getImageIcon(Icons.Document.LETTER_T);
	}

	@Override
	public Color getTitleColor() {
		return titleColor;
	}

	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Liefert die aktuell geöffnete Datei zurück
	 *
	 * @return aktuelle geöffnete Datei
	 */
	public TemplateFile getCurrentTemplate() {
		return currentTemplate;
	}

	/**
	 * @param inTemplate geladene Secdat
	 * @param file File Objekt der Datei
	 */
	public void setCurrentFile(TemplateFile inTemplate, File file) {
		currentTemplate = null;
		setFileChanged(false);

		if (inTemplate != null) {
			setDataFile(file);
			currentTemplate = inTemplate;
			contentPane.loadView();
			contentPane.setVisible(true);
		} else {
			contentPane.setVisible(false);
		}

		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean openFile(File file) {
		try (G3FileReaderEx reader = new G3FileReaderEx(file)) {
			TemplateFile template = FileUtil.openTemplate(file);
			if (checkIntegrity(template)) {
				setCurrentFile(template, file);
				updateChecksum(reader.getBuffer());
				return true;
			}
		} catch (Exception e) {
			logger.warn("Failed to open template.", e);
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Template could not be opened"), e.getMessage());
		}
		return false;
	}

	@Override
	protected Saveable getSaveable() {
		contentPane.saveView();

		if (getOptionStore().get(EditorOptions.Misc.CLEAN_STRINGTABLE)) {
			StringtableHelper.clearStringtableSafe(getCurrentTemplate().getHeaders().toList(), getCurrentTemplate().getStringtable(), true,
					ctx.getParentWindow());
		}

		return getCurrentTemplate();
	}

	@Override
	public boolean saveFile(Optional<File> file) {
		boolean result = super.saveFile(file);
		if (result && file.isPresent()) {
			checkTemplateContext();
		}

		return result;
	}

	@Override
	public Component getTabContent() {
		return contentPane;
	}

	@Override
	public boolean onClose(boolean appExit) {
		if (super.onClose(appExit)) {
			contentPane.onClose();
			ctx.eventBus().unregister(this);
			setCurrentFile(null, null);
			return true;
		}
		return false;
	}

	@Override
	public String getDefaultFileExtension() {
		return "tple";
	}

	@Override
	public ExtensionsFilter getFileFilter() {
		return FileDialogWrapper.TEMPLATE_FILTER;
	}

	public FileDescriptor getFileDescriptor() {
		return getDataFile().map(f -> new FileDescriptor(f, FileType.Template)).orElseGet(() -> FileDescriptor.none(FileType.Template));
	}

	public void refreshView() {
		contentPane.refreshView();
	}

	public void saveView() {
		contentPane.saveView();
	}

	public void loadView() {
		contentPane.loadView();
	}

	private boolean checkIntegrity(TemplateFile tple) {
		if (tple.getItemHeader() == null) {
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Template could not be opened"), I.tr("Template has no item header."));
			return false;
		}

		if (tple.getReferenceHeader() == null) {
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Template could not be opened"), I.tr("Template has no reference header."));
			return false;
		}

		String itemName = tple.getItemHeader().getName();
		String refName = tple.getReferenceHeader().getName();

		if (!itemName.equals(refName)) {
			int result = TaskDialogs.choice(ctx.getParentWindow(), I.tr("Where is the guru?"),
					I.tr("Template has different names in item and reference header.\nWhich one should be kept?"), 1,
					new CommandLink("Item", itemName), new CommandLink("Reference", refName),
					new CommandLink(I.tr("Cancel"), I.tr("Cancel loading the template.")));

			switch (result) {
				case 0:
					tple.getReferenceHeader().setName(itemName);
					break;

				case 1:
					tple.getItemHeader().setName(refName);
					break;

				case -1:
				case 2:
					return false;
			}
		}

		return true;
	}

	private void displayTemplateContextStatus() {
		Guid tpleGuid = new Guid(getCurrentTemplate().getItemHeader().getGuid());
		String tpleName = getCurrentTemplate().getFileName();
		File primaryFileTpleContext = new File(getDataFile().get().getParentFile(),
				getCurrentTemplate().getTemplateContext() + ".lrtpldatasc");
		Optional<File> secondaryFileTpleContext = ctx.getFileManager().moveFromPrimaryToSecondary(primaryFileTpleContext);
		ConcurrencyUtil.executeAndInvokeLaterOnSuccess(() -> {
			File fileTpleContext = primaryFileTpleContext;
			if (!fileTpleContext.exists()) {
				if (secondaryFileTpleContext.isPresent() && secondaryFileTpleContext.get().exists()) {
					fileTpleContext = secondaryFileTpleContext.get();
				} else {
					return Pair.of(false, I.tr("No suitable TemplateContext could be found."));
				}
			}

			try {
				Lrtpldatasc tpleContext = new Lrtpldatasc(fileTpleContext);
				if (!tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
					return Pair.of(false, I.tr("There is no entry in the corresponding TemplateContext."));
				}

				if (!tpleContext.contains(tpleGuid) && tpleContext.contains(tpleName)) {
					return Pair.of(false,
							SwingUtils.getMultilineText(
									I.tr("For the name of the template there is already an entry in the corresponding TemplateContext,\n"
											+ "but under a different guid.")));
				}

				if (tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
					return Pair
							.of(false,
									SwingUtils.getMultilineText(I.trf(
											"For the guid of the template already exists an entry in the corresponding TemplateContext,\n"
													+ "but in combination with the name {0}.",
											tpleContext.get(tpleGuid).getTemplateName())));
				}

				return Pair.of(true, null);
			} catch (IOException e) {
				String message = SwingUtils.getMultilineText(I.trcf("Invalid template context",
						"Error opening the corresponding TemplateContext {0}", fileTpleContext.getName()) + ":", e.getMessage());
				return Pair.of(false, message);
			} catch (DuplicateEntryException | InvalidEntryException e) {
				String reason = e instanceof DuplicateEntryException
						? I.trc("Invalid template context", "as it contains multiple entries with the same guid.")
						: I.trc("Invalid template context", "as it contains at least one invalid guid.");

				String message = SwingUtils.getMultilineText(I.trcf("Invalid template context",
						"Error opening the corresponding TemplateContext {0}", fileTpleContext.getName()) + ",", reason);
				return Pair.of(false, message);
			}
		}, new Consumer<Pair<Boolean, String>>() {
			@Override
			public void accept(Pair<Boolean, String> result) {
				setTitleColorAndTooltip(result.el0() ? null : Color.RED, result.el1());
			}

			private void setTitleColorAndTooltip(Color titleColor, String tooltip) {
				EditorTemplateTab templateTab = EditorTemplateTab.this;
				if (!Objects.equals(titleColor, templateTab.titleColor) || !Objects.equals(tooltip, templateTab.tooltip)) {
					templateTab.titleColor = titleColor;
					templateTab.tooltip = tooltip;
					templateTab.eventBus().post(new StateChangedEvent(templateTab));
				}

			}

		}, ctx.getExecutorService());
	}

	public void checkTemplateContext() {
		checkAndRepairTemplateContext();
		displayTemplateContextStatus();
	}

	private void checkAndRepairTemplateContext() {
		TemplateFile template = getCurrentTemplate();
		File fileTpleContext = new File(getDataFile().get().getParentFile(), template.getTemplateContext() + ".lrtpldatasc");

		Guid tpleGuid = new Guid(template.getItemHeader().getGuid());
		String tpleName = template.getFileName();

		Lrtpldatasc tpleContext = null;
		boolean contextCreated = false;
		if (fileTpleContext.exists()) {
			try {
				tpleContext = new Lrtpldatasc(fileTpleContext);
			} catch (IOException e) {
				logger.warn("Error while opening TemplateContext {}: ", fileTpleContext.getName(), e);
				return;
			} catch (DuplicateEntryException | InvalidEntryException e) {
				logger.warn("Error while opening TemplateContext {}: {}", fileTpleContext.getName(), e.getMessage());
				String reason = e instanceof DuplicateEntryException
						? I.trc("Invalid lrtpldatasc", "as it contains multiple entries with the same guid")
						: I.trc("Invalid lrtpldatasc", "as it contains at least one invalid guid");
				if (TaskDialogs.isConfirmed(ctx.getParentWindow(), I.tr(".lrtpldatasc entry"), I.trcf("Invalid lrtpldatasc",
						"The .lrtpldatasc ''{1}'' responsible for ''{0}'' could not be opened,\n{2} ({3}).\nShould it be opened in the file manager?",
						template.getEntityName(), fileTpleContext.getName(), reason, e.getMessage()))) {
					ctx.getFileManager().explorePath(fileTpleContext);
				}
				return;
			}
		}

		if (!fileTpleContext.exists()) {
			Optional<File> fileAltTpleContext = ctx.getFileManager().moveFromPrimaryToSecondary(fileTpleContext);
			if (fileAltTpleContext.isPresent() && fileAltTpleContext.get().exists()) {
				try {
					tpleContext = new Lrtpldatasc(fileAltTpleContext.get());
				} catch (IOException | DuplicateEntryException e) {
					logger.warn("Error while opening TemplateContext {}: {}", fileAltTpleContext.get().getName(), e.getMessage());
				}
			}
		}

		if (tpleContext == null) {
			int result = TaskDialogs.choice(ctx.getParentWindow(), I.tr("Create .lrtpldatasc"),
					I.trf("No suitable .lrtpldatasc could be found for ''{0}''.", tpleName), 0,
					new CommandLink(I.tr("Create new .lrtpldatasc"), ""), new CommandLink(I.tr("Search for .lrtpldatasc"), ""),
					new CommandLink(I.tr("Ignore"), ""));

			if (result == 0) {
				tpleContext = new Lrtpldatasc();
				contextCreated = true;
			} else if (result == 1) {
				File file = FileDialogWrapper.openFile(I.tr("Search for .lrtpldatasc"), ctx.getParentWindow(),
						FileDialogWrapper.createFilter(".lrtpldatasc", "lrtpldatasc"));
				if (file != null) {
					try {
						tpleContext = new Lrtpldatasc(file);
						contextCreated = true;
					} catch (IOException e) {
						logger.warn("Error while opening TemplateContext {}.", file.getName(), e);
						return;
					}
				}
			}
		}

		if (tpleContext != null) {
			boolean contextChanged = false;
			if (!tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Create .lrtpldatasc entry"),
						I.trf("Should an entry be created for {0} in ''{1}''?", template.getEntityName(), fileTpleContext.getName()));

				if (confirmed) {
					tpleContext.add(tpleGuid, tpleName);
					contextChanged = true;
				}
			} else if (!tpleContext.contains(tpleGuid) && tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Update .lrtpldatasc entry"),
						I.trf("For {0} there is already an entry in ''{1}'',\nbut under a different guid.\n\nShould the entry be updated?",
								template.getEntityName(), fileTpleContext.getName()));

				if (confirmed) {
					tpleContext.remove(tpleName);
					tpleContext.add(tpleGuid, tpleName);
					contextChanged = true;
				}
			} else if (tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Update .lrtpldatasc entry"), I.trf(
						"For the guid of {0} there is already an entry in ''{1}'',\nbut in combination with the name {2}.\n\nShould the entry be updated?",
						template.getEntityName(), fileTpleContext.getName(), tpleContext.get(tpleGuid).getTemplateName()));

				if (confirmed) {
					tpleContext.remove(tpleGuid);
					tpleContext.add(tpleGuid, tpleName);
					contextChanged = true;
				}
			}

			try {
				if (contextChanged) {
					tpleContext.save(fileTpleContext);
					if (contextCreated) {
						FileUtil.createLrtpl(fileTpleContext);
						FileUtil.createLrtpldat(fileTpleContext);
					}
				}
			} catch (IOException e) {
				logger.warn("Error while saving TemplateContext {}.", fileTpleContext.getName(), e);
				TaskDialogs.error(ctx.getParentWindow(), "", I.trf("''{0}'' could not be saved.", fileTpleContext.getName()));
			}
		}
	}
}
