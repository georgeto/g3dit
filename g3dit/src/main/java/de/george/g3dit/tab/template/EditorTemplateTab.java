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
		btnEntity.setToolTipText("Template bearbeiten");
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
			logger.warn("Template konnte nicht geöffnet werden:", e);
			TaskDialogs.error(ctx.getParentWindow(), "Template konnte nicht geöffnet werden", e.getMessage());
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
			TaskDialogs.error(ctx.getParentWindow(), "Template konnte nicht geöffnet werden", "Template hat keinen Item-Header.");
			return false;
		}

		if (tple.getReferenceHeader() == null) {
			TaskDialogs.error(ctx.getParentWindow(), "Template konnte nicht geöffnet werden", "Template hat keinen Reference-Header.");
			return false;
		}

		String itemName = tple.getItemHeader().getName();
		String refName = tple.getReferenceHeader().getName();

		if (!itemName.equals(refName)) {
			int result = TaskDialogs.choice(ctx.getParentWindow(), "Where is the guru?",
					"Template hat unterschiedliche Namen im Item und Reference Header.\nWelcher soll übernommen werden?", 1,
					new CommandLink("Item", itemName), new CommandLink("Reference", refName),
					new CommandLink("Abbrechen", "Ladevorgang der Template abbrechen."));

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
					return Pair.of(false, "Es konnte kein passender TemplateContext gefunden werden.");
				}
			}

			try {
				Lrtpldatasc tpleContext = new Lrtpldatasc(fileTpleContext);
				if (!tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
					return Pair.of(false, "Es existiert kein Eintrag im zugehörigen TemplateContext.");
				}

				if (!tpleContext.contains(tpleGuid) && tpleContext.contains(tpleName)) {
					return Pair.of(false,
							SwingUtils.getMultilineText(
									"Für den Namen der Template existiert bereits ein Eintrag im zugehörigen TemplateContext,",
									"allerdings unter einer anderen Guid."));
				}

				if (tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
					return Pair.of(false,
							SwingUtils.getMultilineText(
									"Für die Guid der Template existiert bereits ein Eintrag im zugehörigen TemplateContext,",
									"allerdings in Kombination mit dem Namen " + tpleContext.get(tpleGuid).getTemplateName()));
				}

				return Pair.of(true, null);
			} catch (IOException e) {
				String message = SwingUtils.getMultilineText(
						"Fehler beim Öffnen des zugehörigen TemplateContexts " + fileTpleContext.getName() + ":", e.getMessage());
				return Pair.of(false, message);
			} catch (DuplicateEntryException | InvalidEntryException e) {
				String reason = e instanceof DuplicateEntryException ? "da dieser für eine Guid mehrere Einträge enthält."
						: "da dieser mindestens eine ungültige Guid enthält.";

				String message = SwingUtils.getMultilineText(
						"Fehler beim Öffnen des zugehörigen TemplateContexts " + fileTpleContext.getName() + ", ", reason);
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
				logger.warn("Fehler beim Öffnen des TemplateContexts {}: ", fileTpleContext.getName(), e);
				return;
			} catch (DuplicateEntryException | InvalidEntryException e) {
				logger.warn("Fehler beim Öffnen des TemplateContexts {}: {}", fileTpleContext.getName(), e.getMessage());
				String reason = e instanceof DuplicateEntryException ? "da sie für eine Guid mehrere Einträge enthält"
						: "da sie mindestens eine ungültige Guid enthält";
				if (TaskDialogs.isConfirmed(ctx.getParentWindow(), ".lrtpldatasc Eintrag",
						"Die für '" + template.getEntityName() + "' zuständige .lrtpldatasc '" + fileTpleContext.getName()
								+ "' konnte nicht geöffnet werden,\n" + reason + " (" + e.getMessage()
								+ ").\nSoll sie im Dateimanager geöffnet werden?")) {
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
					logger.warn("Fehler beim Öffnen des TemplateContexts {}: {}", fileAltTpleContext.get().getName(), e.getMessage());
				}
			}
		}

		if (tpleContext == null) {
			int result = TaskDialogs.choice(ctx.getParentWindow(), ".lrtpldatasc erstellen",
					"Für '" + tpleName + "' konnte keine passende .lrtpldatasc gefunden werden.", 0,
					new CommandLink("Neue .lrtpldatasc erstellen", ""), new CommandLink("Nach .lrtpldatasc suchen", ""),
					new CommandLink("Ignorieren", ""));

			if (result == 0) {
				tpleContext = new Lrtpldatasc();
				contextCreated = true;
			} else if (result == 1) {
				File file = FileDialogWrapper.openFile("Nach .lrtpldatasc suchen", ctx.getParentWindow(),
						FileDialogWrapper.createFilter(".lrtpldatasc", "lrtpldatasc"));
				if (file != null) {
					try {
						tpleContext = new Lrtpldatasc(file);
						contextCreated = true;
					} catch (IOException e) {
						logger.warn("Fehler beim Öffnen des TemplateContexts {}: ", file.getName(), e);
						return;
					}
				}
			}
		}

		if (tpleContext != null) {
			boolean contextChanged = false;
			if (!tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), ".lrtpldatasc Eintrag erstellen",
						"Soll für " + template.getEntityName() + " ein Eintrag in '" + fileTpleContext.getName() + "' erstellt werden?");

				if (confirmed) {
					tpleContext.add(tpleGuid, tpleName);
					contextChanged = true;
				}
			} else if (!tpleContext.contains(tpleGuid) && tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), ".lrtpldatasc Eintrag aktualisieren",
						"Für " + template.getEntityName() + " existiert bereits ein Eintrag in '" + fileTpleContext.getName()
								+ "',\nallerdings unter einer anderen Guid.\n\nSoll der Eintrag aktualisiert werden?");

				if (confirmed) {
					tpleContext.remove(tpleName);
					tpleContext.add(tpleGuid, tpleName);
					contextChanged = true;
				}
			} else if (tpleContext.contains(tpleGuid) && !tpleContext.contains(tpleName)) {
				boolean confirmed = TaskDialogs.ask(ctx.getParentWindow(), ".lrtpldatasc Eintrag aktualisieren",
						"Für die Guid von " + template.getEntityName() + " existiert bereits ein Eintrag in '" + fileTpleContext.getName()
								+ "',\nallerdings in Kombination mit dem Namen " + tpleContext.get(tpleGuid).getTemplateName()
								+ ".\n\nSoll der Eintrag aktualisiert werden?");

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
				logger.warn("Fehler beim Speichern des TemplateContexts {}: ", fileTpleContext.getName(), e);
				TaskDialogs.error(ctx.getParentWindow(), "", "'" + fileTpleContext.getName() + "' konnte nicht gespeichert werden.");
			}
		}
	}
}
