package de.george.g3dit.check;

import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.rawHtml;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeList.Format;
import ca.odell.glazedlists.TreeList.Node;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.glazedlists.swing.TreeTableSupport;
import de.george.g3dit.EditorContext;
import de.george.g3dit.check.Check.PassStatus;
import de.george.g3dit.check.problem.Category;
import de.george.g3dit.check.problem.EntityHelper;
import de.george.g3dit.check.problem.FileHelper;
import de.george.g3dit.check.problem.Problem;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.config.ConfigFiles;
import de.george.g3dit.config.StringWithCommentConfigFile;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.HtmlEditorPane;
import de.george.g3dit.gui.components.JSeverityComboBox;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.gui.components.SeverityImageIcon;
import de.george.g3dit.gui.components.SeverityMatcherEditor;
import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.gui.components.tab.JTypedTabbedPane;
import de.george.g3dit.gui.components.tab.TabSelectedEvent;
import de.george.g3dit.gui.dialogs.CheckBoxListSelectDialog;
import de.george.g3dit.gui.dialogs.EditStringWithCommentConfigDialog;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.ClasspathScanUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.PathAliases;
import de.george.g3dit.util.StringWithComment;
import de.george.g3dit.util.UriUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class CheckManager {
	private static final Logger logger = LoggerFactory.getLogger(CheckManager.class);

	private Set<Check> checks;
	private Set<Check> enabledChecks;
	private EditorContext ctx;
	private StringWithCommentConfigFile ignoredFiles;
	private CheckDialog checkDialog;

	public CheckManager(EditorContext ctx) {
		checks = new TreeSet<>(CheckManager::compareCheck);
		enabledChecks = new TreeSet<>(CheckManager::compareCheck);
		this.ctx = ctx;
		ignoredFiles = ConfigFiles.checkIgnoredFiles(ctx);
	}

	private static int compareCheck(Check c1, Check c2) {
		return c1.getTitle().compareTo(c2.getTitle());
	}

	public void addDefaultChecks() {
		ConcurrencyUtil.executeAndInvokeLaterOnSuccess(() -> {
			List<Check> checks = new ArrayList<>();
			for (Class<? extends Check> check : ClasspathScanUtil.findSubtypesOf(Check.class, "de.george.g3dit.check.checks")) {
				try {
					if (!Modifier.isAbstract(check.getModifiers())) {
						try {
							Constructor<? extends Check> constructor = check.getConstructor(EditorContext.class);
							checks.add(constructor.newInstance(ctx));
						} catch (NoSuchMethodException e) {
							// Check is not interested in EditorContext
							checks.add(check.newInstance());
						}
					}
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warn("Unable to instantiate '{}'.", check.getSimpleName(), e);
				}
			}
			return checks;
		}, checks -> {
			checks.forEach(this::addCheck);
			loadEnabledChecks();
		}, ctx.getExecutorService());

	}

	public void addCheck(Check check) {
		checks.add(check);
		enabledChecks.add(check);
	}

	public void removeCheck(Check check) {
		checks.remove(check);
		enabledChecks.remove(check);
	}

	private void loadEnabledChecks() {
		Set<String> enabled = ctx.getOptionStore().get(EditorOptions.CheckManager.ENABLED_CHECKS);
		if (enabled != null) {
			List<Check> checksToEnable = checks.stream().filter(c -> enabled.contains(c.getClass().getSimpleName()))
					.collect(Collectors.toList());
			setEnabledChecks(checksToEnable, false);
		}
	}

	private void setEnabledChecks(Collection<Check> checksToEnable, boolean store) {
		enabledChecks.clear();
		enabledChecks.addAll(checksToEnable);
		if (store) {
			Set<String> enabled = enabledChecks.stream().map(Object::getClass).map(Class::getSimpleName).collect(Collectors.toSet());
			ctx.getOptionStore().put(EditorOptions.CheckManager.ENABLED_CHECKS, enabled);
		}
	}

	public void showCheckDialog() {
		if (checkDialog == null) {
			checkDialog = new CheckDialog();
			checkDialog.setLocationRelativeTo(ctx.getParentWindow());
		}
		checkDialog.setVisible(true);
	}

	private static Optional<FileHelper> findFileHelper(Problem problem) {
		while (problem != null) {
			if (problem instanceof FileHelper fileHelper)
				return Optional.of(fileHelper);
			problem = problem.getParent();
		}
		return Optional.empty();
	}

	private static Optional<Path> getFileHelperFile(Problem problem) {
		if (problem instanceof FileHelper fileHelper) {
			Path filePath = fileHelper.getDescriptor().getPath();
			return Optional.of(filePath);
		}
		return Optional.empty();
	}

	private String getFileHelperPath(Problem problem) {
		return getFileHelperFile(problem).map(Path::getParent).map(f -> PathAliases.from(ctx.getOptionStore()).apply(f)).orElse(null);
	}

	private class IgnoredFilesMatchedEditor extends AbstractMatcherEditor<Problem> {
		private ImmutableSet<String> ignoredFileNames;
		private boolean enabled = true;

		public IgnoredFilesMatchedEditor() {
			ignoredFiles.addContentChangedListener(CheckManager.this, c -> ignoredFilesChanged());
		}

		public void setEnabled(boolean enabled) {
			if (this.enabled != enabled) {
				this.enabled = enabled;
				ignoredFilesChanged();
			}
		}

		private void ignoredFilesChanged() {
			if (enabled) {
				ignoredFileNames = ignoredFiles.getValues();
				fireChanged(problem -> !findFileHelper(problem).flatMap(CheckManager::getFileHelperFile).map(FilesEx::getFileName)
						.map(ignoredFileNames::contains).orElse(false));
			} else {
				fireMatchAll();
			}
		}
	}

	private class CheckDialog extends ExtStandardDialog {
		private JProgressBar progressBar;
		private EnableGroup egCheck;
		private JLabel laFilteredCount;
		private EventList<Problem> problems;
		private FilterList<Problem> problemsWithoutIgnored, filteredProblems;

		private Map<EntityDescriptor, EntityHelper> entityHelpers = new HashMap<>();
		private Map<FileDescriptor, FileHelper> fileHelpers = new HashMap<>();

		private CheckProblemConsumer problemConsumer = new CheckProblemConsumer();
		private ListenableFuture<Void> executeChecksFuture;
		private JTypedTabbedPane<ProblemTableTab> tabbedPane;
		private IgnoredFilesMatchedEditor ignoredFilesMatchedEditor;

		public CheckDialog() {
			super(ctx.getParentWindow(), I.tr("Execute checks"));
			setSize(1000, 700);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					cancelChecks();
				}
			});
		}

		private void toggleChecks() {
			CheckBoxListSelectDialog<Check> dialog = new CheckBoxListSelectDialog<>(this, I.tr("Select checks"), checks,
					new BeanListCellRenderer("Title", "Description", 80));
			dialog.setSelectedEntries(enabledChecks);
			if (dialog.openAndWasSuccessful()) {
				setEnabledChecks(dialog.getSelectedEntries(), true);
			}
		}

		private void cancelChecks() {
			if (executeChecksFuture != null && !executeChecksFuture.isCancelled()) {
				executeChecksFuture.cancel(true);
			}
		}

		private void executeChecks() {
			if (executeChecksFuture != null || checks.isEmpty()) {
				return;
			}

			progressBar.setIndeterminate(true);
			egCheck.setEnabled(false);

			entityHelpers.clear();
			fileHelpers.clear();
			problems.clear();

			enabledChecks.forEach(Check::reset);
			int archivePasses = enabledChecks.stream().map(Check::getArchivePasses).max(Integer::compareTo).get();
			int templatePasses = enabledChecks.stream().map(Check::getTemplatePasses).max(Integer::compareTo).get();
			int passes = Math.max(archivePasses, templatePasses);

			executeChecksFuture = ConcurrencyUtil.executeAndInvokeLater(() -> {
				updateProgressBar(String.format(I.tr("Determine files to be checked...")));

				List<Path> worldFiles = archivePasses != 0 ? ctx.getFileManager().listWorldFiles() : null;
				List<Path> templateFiles = templatePasses != 0 ? ctx.getFileManager().listTemplateFiles() : null;

				for (int pass = 0; pass < passes; pass++) {
					if (pass < templatePasses) {
						updateProgressBar(I.trf("Template pass {0, number}/{1, number}", pass + 1, templatePasses));
						TemplateFileIterator iter = new TemplateFileIterator(templateFiles);
						while (iter.hasNext()) {
							// Check execution got cancelled
							if (Thread.interrupted()) {
								return;
							}

							TemplateFile tple = iter.next();
							Path tpleFile = iter.nextFile();
							boolean allDone = true;
							for (Check check : enabledChecks) {
								if (pass < check.getTemplatePasses()) {
									PassStatus status = check.processTemplate(tple, tpleFile, pass, problemConsumer);
									allDone &= status == PassStatus.Done;
								}
							}

							if (allDone) {
								break;
							}
						}
					}

					if (pass < archivePasses) {
						updateProgressBar(I.trf("Archive pass {0, number}/{1, number}", pass + 1, archivePasses));
						ArchiveFileIterator iter = new ArchiveFileIterator(worldFiles);
						while (iter.hasNext()) {
							// Check execution got cancelled
							if (Thread.interrupted()) {
								return;
							}

							ArchiveFile archive = iter.next();
							Path archiveFile = iter.nextFile();
							boolean allDone = true;
							for (Check check : enabledChecks) {
								if (pass < check.getArchivePasses()) {
									PassStatus status = check.processArchive(archive, archiveFile, pass, problemConsumer);
									allDone &= status == PassStatus.Done;
								}
							}

							if (allDone) {
								break;
							}
						}
					}
				}

				updateProgressBar(I.tr("Collect issues..."));
				for (Check check : enabledChecks) {
					if (Thread.interrupted()) {
						return;
					}
					check.reportProblems(problemConsumer);
				}
				enabledChecks.forEach(Check::reset);
			}, new FutureCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					onCompletion();
				}

				@Override
				public void onFailure(Throwable t) {
					if (!executeChecksFuture.isCancelled())
						logger.warn("Failure during check execution.", t);
					onCompletion();
				}

				private void onCompletion() {
					progressBar.setIndeterminate(false);
					progressBar.setString(executeChecksFuture.isCancelled() ? I.tr("Execution cancelled") : I.tr("Execution completed"));
					egCheck.setEnabled(true);
					ctx.runGC();
					executeChecksFuture = null;
				}
			}, ctx.getExecutorService());
		}

		private void updateProgressBar(String message) {
			SwingUtilities.invokeLater(() -> {
				if (executeChecksFuture != null)
					progressBar.setString(message);
			});
		}

		private void updateFilteredCount() {
			laFilteredCount
					.setText(I.trf("{0, number} / {1, number} issues displayed", filteredProblems.size(), problemsWithoutIgnored.size()));
		}

		@Override
		public JComponent createContentPanel() {
			JPanel mainPanel = new JPanel(new MigLayout("fill"));
			egCheck = new EnableGroup();

			progressBar = SwingUtils.createProgressBar();
			mainPanel.add(progressBar, "spanx 10, split 3, grow");

			JButton btnToggleChecks = SwingUtils.keyStrokeButton(I.tr("Select checks"), Icons.getImageIcon(Icons.Select.CHECK_BOX),
					KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK, this::toggleChecks);
			egCheck.add(btnToggleChecks);
			mainPanel.add(btnToggleChecks, "alignx right, sizegroup sgBtnTopRow");

			Action executeAction = SwingUtils.createAction(I.tr("Execute checks"), Icons.getImageIcon(Icons.Misc.MAGNIFIER),
					this::executeChecks);
			Action cancelAction = SwingUtils.createAction(I.tr("Cancel"), Icons.getImageIcon(Icons.Select.CANCEL), this::cancelChecks);
			setDefaultAction(executeAction);
			setDefaultCancelAction(cancelAction);

			JButton btnExecute = SwingUtils.keyStrokeButton(executeAction, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
			egCheck.addCallback(enabled -> btnExecute.setAction(enabled ? executeAction : cancelAction));
			mainPanel.add(btnExecute, "alignx right, sizegroup sgBtnTopRow, wrap");

			UndoableTextField tfSearch = SwingUtils.createUndoTF();
			JSeverityComboBox cbSeverity = new JSeverityComboBox();
			cbSeverity.setSelectedIndex(1);
			laFilteredCount = new JLabel();
			mainPanel.add(tfSearch, "gaptop 7, split 3, width 350!");
			mainPanel.add(cbSeverity, "gaptop 9");
			mainPanel.add(laFilteredCount, "wmax 200, gapleft 10");

			JPanel actionButtonPanel = new JPanel(new MigLayout("insets 0, fillx"));
			mainPanel.add(actionButtonPanel, "wrap");

			problems = new BasicEventList<>();
			ignoredFilesMatchedEditor = new IgnoredFilesMatchedEditor();
			problemsWithoutIgnored = new FilterList<>(problems, ignoredFilesMatchedEditor);
			filteredProblems = new FilterList<>(problemsWithoutIgnored,
					new TextComponentMatcherEditor<>(tfSearch, GlazedLists.textFilterator("Message", "Details")));
			filteredProblems = new FilterList<>(filteredProblems, new SeverityMatcherEditor<>(cbSeverity, Problem::getSeverity));

			// Update filter count if number of found or listed issues changes.
			problemsWithoutIgnored.addListEventListener(e -> updateFilteredCount());
			filteredProblems.addListEventListener(e -> updateFilteredCount());

			tabbedPane = new JTypedTabbedPane<>();
			tabbedPane.setTabPlacement(SwingConstants.BOTTOM);
			tabbedPane.eventBus().register(new Object() {
				@Subscribe
				public void onTabSelected(TabSelectedEvent<ProblemTableTab> event) {
					actionButtonPanel.removeAll();
					event.getTab().ifPresent(tab -> tab.actionButtons.stream().forEach(actionButtonPanel::add));
				}
			});

			for (Category category : Category.values()) {
				if (category == Category.Helper) {
					continue;
				}

				tabbedPane.addTab(new ProblemTableTab(category));
			}

			mainPanel.add(tabbedPane.getComponent(), "id tabs, push, span, grow, wrap");

			JCheckBox cbEnableIgnoreList = new JCheckBox("", true);
			cbEnableIgnoreList.setToolTipText(I.tr("Filter results by ignore list."));
			cbEnableIgnoreList.addItemListener(e -> ignoredFilesMatchedEditor.setEnabled(cbEnableIgnoreList.isSelected()));

			JButton btnEditIgnoreList = SwingUtils.keyStrokeButton(null, I.tr("Edit ignore list"),
					Icons.getImageIcon(Icons.Select.CANCEL_EDIT), KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK,
					() -> new EditStringWithCommentConfigDialog(ctx.getParentWindow(), I.tr("Edit ignore list"), ignoredFiles).open());

			JPanel ignorePanel = new JPanel(new MigLayout("insets 0, fill"));
			ignorePanel.add(cbEnableIgnoreList);
			ignorePanel.add(btnEditIgnoreList);
			mainPanel.add(ignorePanel, "id ignorelist, pos (tabs.x2 - ignorelist.w) (tabs.y2 - ignorelist.h)");
			// Make sure ignore panel is on top of the tabbed pane, otherwise it cannot be
			// interacted with.
			mainPanel.setComponentZOrder(ignorePanel, 0);

			return mainPanel;
		}

		private class ProblemTableTab implements ITypedTab {
			private Category category;
			private FilterList<Problem> filteredList;
			private JPanel tabContent;
			private TreeList<Problem> treeList;
			private AdvancedTableModel<Problem> problemTableModel;
			private JXTable problemTable;
			private HtmlEditorPane epDetails;
			private List<JButton> actionButtons = new ArrayList<>();

			public ProblemTableTab(Category category) {
				this.category = category;
				filteredList = new FilterList<>(filteredProblems, p -> p.getCategory() == category);
				filteredList.addListEventListener(c -> tabbedPane.updateTab(this));

				Format<Problem> treeFormat;
				TableFormat<Problem> tableFormat;
				ColumnFactory tableColumnFactory;
				switch (category) {
					case Entity -> {
						treeFormat = new EntityProblemTreeFormat();
						tableFormat = new EntityProblemTableFormat();
						tableColumnFactory = new EntityProblemTableColumnFactory();
					}
					case Template -> {
						treeFormat = new TemplateProblemTreeFormat();
						tableFormat = new TemplateProblemTableFormat();
						tableColumnFactory = new TemplateProblemTableColumnFactory();
					}
					case Misc -> {
						treeFormat = new TemplateProblemTreeFormat();
						tableFormat = new TemplateProblemTableFormat();
						tableColumnFactory = new TemplateProblemTableColumnFactory();
					}
					case Navigation -> {
						treeFormat = new EntityProblemTreeFormat();
						tableFormat = new EntityProblemTableFormat();
						tableColumnFactory = new EntityProblemTableColumnFactory();
					}
					default -> throw new IllegalArgumentException();
				}

				treeList = new TreeList<>(filteredList, treeFormat, TreeList.nodesStartExpanded());

				problemTableModel = GlazedListsSwing.eventTableModel(treeList, tableFormat);
				problemTable = new JXTable();
				problemTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				problemTable.setAutoCreateRowSorter(false);
				problemTable.setRowSorter(null);
				problemTable.setSortable(false);
				problemTable.setColumnFactory(tableColumnFactory);
				problemTable.setShowGrid(false);
				problemTable.setColumnControlVisible(true);
				problemTable.setModel(problemTableModel);
				problemTable.addMouseListener(TableUtil.createDoubleClickListener(this::onNavigate));
				SwingUtils.addKeyStroke(problemTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "Navigate",
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), () -> onNavigate(TableUtil.getSelectedRow(problemTable)));
				SwingUtils.addKeyStroke(problemTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "Delete",
						KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), this::onDelete);
				problemTable.getSelectionModel().addListSelectionListener(e -> onSelect());

				// make the 3rd table column a hierarchical column to create a TreeTable
				TreeTableSupport treeTableSupport = TreeTableSupport.install(problemTable, treeList, 0);
				treeTableSupport.setArrowKeyExpansionEnabled(true);
				treeTableSupport.setShowExpanderForEmptyParent(false);
				treeTableSupport.setSpaceKeyExpansionEnabled(true);
				treeTableSupport.setDelegateRenderer(new ProblemTableCellRenderer());

				tabContent = new JPanel(new MigLayout("insets 0, fill", "[fill]", "[fill][fill]"));

				tabContent.add(new JScrollPane(problemTable), "height 80%, wrap");

				epDetails = HtmlEditorPane.withCssTemplateFile("/css/issue-box.css");
				epDetails.editorPane.addHyperlinkListener(this::onHyperlink);
				tabContent.add(epDetails.scrollPane, "height 20%");

				createActionButtons();
			}

			private void createActionButtons() {
				JButton btnBlacklist = SwingUtils.keyStrokeButton(I.tr("Blacklist"), I.tr("Add the selected file(s) to the ignore list."),
						Icons.getImageIcon(Icons.Select.CANCEL), KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK, this::onBlacklist);
				TableUtil.enableOnGreaterEqual(problemTable, btnBlacklist, 1,
						() -> getSelectedProblems().mapPartial(CheckManager::getFileHelperFile).findAny().isPresent());
				actionButtons.add(btnBlacklist);
			}

			@Override
			public String getTabTitle() {
				return category.toString() + " (" + filteredList.size() + ")";
			}

			@Override
			public Color getTitleColor() {
				Severity severest = filteredList.stream().map(Problem::getSeverity).max(Severity::compareTo).orElse(Severity.Info);
				if (severest == Severity.Warn) {
					return Color.ORANGE.darker();
				}
				if (severest == Severity.Error) {
					return Color.RED;
				}
				return null;
			}

			@Override
			public Icon getTabIcon() {
				return null;
			}

			@Override
			public Component getTabContent() {
				return tabContent;
			}

			protected Optional<Problem> getProblem(int index) {
				return Optional.of(index).filter(i -> i >= 0).map(problemTableModel::getElementAt);
			}

			protected Optional<Problem> getSelectedProblem() {
				return getProblem(TableUtil.getSelectedRow(problemTable));
			}

			protected StreamEx<Problem> getSelectedProblems() {
				return TableUtil.getSelectedRows(problemTable).map(problemTableModel::getElementAt);
			}

			private void onNavigate(int index) {
				getProblem(index).ifPresent(problem -> {
					if (problem.canNavigate()) {
						problem.navigate(ctx);
					}
				});
			}

			private void onDelete() {
				Set<Problem> problemsToDelete = new HashSet<>();
				Queue<Node<Problem>> openNodes = new LinkedList<>();
				TableUtil.getSelectedRows(problemTable).map(treeList::getTreeNode).forEach(openNodes::add);
				while (!openNodes.isEmpty()) {
					Node<Problem> node = openNodes.poll();
					if (!node.isLeaf()) {
						openNodes.addAll(node.getChildren());
					} else {
						problemsToDelete.add(node.getElement());
					}
				}

				if (!problems.isEmpty()) {
					problems.removeAll(problemsToDelete);
				}
			}

			private void onBlacklist() {
				var filesToBlacklist = getSelectedProblems().mapPartial(CheckManager::getFileHelperFile).map(FilesEx::getFileName)
						.map(name -> new StringWithComment(name, ""));

				if (ignoredFiles.acquireEditLock()) {
					ignoredFiles.updateContent(ImmutableSet.copyOf(Iterables.concat(ignoredFiles.getContent(), filesToBlacklist)));
					ignoredFiles.releaseEditLock();
				} else {
					TaskDialogs.error(ctx.getParentWindow(), I.tr("Ignore list"), I.tr("Already opened for editing."));
				}
			}

			private void onSelect() {
				getSelectedProblem().ifPresent(problem -> {
					if (problem instanceof FileHelper || problem instanceof EntityHelper) {
						epDetails.setHtml(rawHtml(problem.getDetails()));
					} else {
						epDetails.setHtml(b(rawHtml(problem.getMessage())), p(),
								rawHtml(problem.getDetails() != null ? problem.getDetails() : ""));
					}
				});
			}

			private void onHyperlink(HyperlinkEvent e) {
				if (e.getEventType() != EventType.ACTIVATED) {
					return;
				}

				String uri = e.getDescription();
				Optional<String> scheme = UriUtil.getScheme(uri);
				if (scheme.isPresent()) {
					switch (scheme.get()) {
						case "file" -> {
							FileDescriptor file = UriUtil.decodeUriAsJson(uri, FileDescriptor.class).el1();
							ctx.getEditor().openOrSelectFile(file.getPath());
						}
						case "entity" -> {
							EntityDescriptor entity = UriUtil.decodeUriAsJson(uri, EntityDescriptor.class).el1();
							ctx.getEditor().openEntity(entity);
						}
					}
				}
			}
		}

		private class CheckProblemConsumer implements ProblemConsumer {
			@Override
			public void post(Problem problem) {
				if (!Thread.interrupted()) {
					SwingUtilities.invokeLater(() -> problems.add(problem));
				}
			}

			@Override
			public EntityHelper getEntityHelper(EntityDescriptor entity) {
				return entityHelpers.computeIfAbsent(entity, d -> new EntityHelper(d, getFileHelper(d.getFile())));
			}

			@Override
			public FileHelper getFileHelper(FileDescriptor file) {
				return fileHelpers.computeIfAbsent(file, f -> new FileHelper(f, CheckManager.this::getFileHelperPath));
			}

		}
	}

	private static abstract class ProblemTreeFormat implements Format<Problem> {
		@Override
		public void getPath(List<Problem> path, Problem element) {
			List<Problem> reversePath = new ArrayList<>();
			Problem curElement = element;
			do {
				reversePath.add(curElement);
				curElement = curElement.getParent();
			} while (curElement != null);
			path.addAll(Lists.reverse(reversePath));
		}

		@Override
		public boolean allowsChildren(Problem element) {
			return element.getCategory() == Category.Helper;
		}

		protected static int compareFileHelper(Problem p1, Problem p2) {
			return ((FileHelper) p1).getDescriptor().compareTo(((FileHelper) p2).getDescriptor());
		}

		protected static int compareEntityHelper(Problem p1, Problem p2) {
			if (p1.getParent() == p2.getParent()) {
				return Integer.compare(((EntityHelper) p1).getDescriptor().getIndex(), ((EntityHelper) p2).getDescriptor().getIndex());
			}

			return compareFileHelper(p1.getParent(), p2.getParent());
		}

		protected static int compareProblem(Problem p1, Problem p2, BiFunction<Problem, Problem, Integer> compareParents) {
			if (p1.getParent() == p2.getParent()) {
				int result = p1.getSeverity().compareTo(p2.getSeverity());
				if (result == 0) {
					result = p1.getMessage().compareTo(p2.getMessage());
				}
				return result;
			}

			return compareParents.apply(p1.getParent(), p2.getParent());
		}
	}

	// TODO: Make certain columns monospace...
	private static final ImmutableBiMap<String, String> COLUMN_MAPPING = ImmutableBiMap.of("Name", I.tr("Name"), "Guid", I.tr("Guid"),
			"Index", I.tr("Index"), "Path", I.tr("Path"));

	private abstract static class ProblemTableFormat implements TableFormat<Problem> {
		@Override
		public String getColumnName(int column) {
			return switch (column) {
				case 0 -> COLUMN_MAPPING.get("Name");
				default -> throw new IllegalArgumentException();
			};
		}

		@Override
		public Object getColumnValue(Problem problem, int column) {
			switch (column) {
				case 0 -> {
					// Strip all html tags
					String message = problem.getMessage().replaceAll("<[^>]+>", "");
					if (problem.getSeverity() != null)
						return new SeverityImageIcon(problem.getSeverity(), message);
					else
						return message;
				}
				default -> throw new IllegalArgumentException();
			}
		}
	}

	private class EntityProblemTreeFormat extends ProblemTreeFormat {
		@Override
		public Comparator<? super Problem> getComparator(int depth) {
			return switch (depth) {
				case 0 -> ProblemTreeFormat::compareFileHelper;
				case 1 -> ProblemTreeFormat::compareEntityHelper;
				case 2 -> (p1, p2) -> compareProblem(p1, p2, ProblemTreeFormat::compareEntityHelper);
				default -> null;
			};
		}
	}

	private class EntityProblemTableFormat extends ProblemTableFormat {
		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			return switch (column) {
				case 0 -> super.getColumnName(column);
				case 1 -> COLUMN_MAPPING.get("Guid");
				case 2 -> COLUMN_MAPPING.get("Index");
				case 3 -> COLUMN_MAPPING.get("Path");
				default -> throw new IllegalArgumentException();
			};
		}

		@Override
		public Object getColumnValue(Problem problem, int column) {
			switch (column) {
				case 0:
					return super.getColumnValue(problem, column);
				case 1:
					if (problem instanceof EntityHelper) {
						return ((EntityHelper) problem).getDescriptor().getGuid();
					}
					return null;
				case 2:
					if (problem instanceof EntityHelper) {
						return ((EntityHelper) problem).getDescriptor().getIndex();
					}
					return null;
				case 3:
					return getFileHelperPath(problem);
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	private static class EntityProblemTableColumnFactory extends ColumnFactory {
		@Override
		public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
			super.configureTableColumn(model, columnExt);
			if (columnExt.getTitle().equals("Index")) {
				columnExt.setComparator(Comparator.naturalOrder());
			}
		}

		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			switch (COLUMN_MAPPING.inverse().get(columnExt.getTitle())) {
				case "Name" -> {
					columnExt.setPreferredWidth(500);
					columnExt.setMaxWidth(1000);
					columnExt.setHideable(false);
				}
				case "Guid" -> {
					columnExt.setPreferredWidth(300);
					columnExt.setMaxWidth(300);
				}
				case "Index" -> {
					columnExt.setMinWidth(40);
					columnExt.setPreferredWidth(45);
					columnExt.setMaxWidth(45);
				}
				case "Path" -> {
					columnExt.setPreferredWidth(75);
					columnExt.setMaxWidth(1000);
				}
			}
		}
	}

	private static class TemplateProblemTreeFormat extends ProblemTreeFormat {
		@Override
		public Comparator<? super Problem> getComparator(int depth) {
			return switch (depth) {
				case 0 -> ProblemTreeFormat::compareFileHelper;
				case 1 -> (p1, p2) -> compareProblem(p1, p2, ProblemTreeFormat::compareFileHelper);
				default -> null;
			};
		}
	}

	private class TemplateProblemTableFormat extends ProblemTableFormat {
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return switch (column) {
				case 0 -> super.getColumnName(column);
				case 1 -> COLUMN_MAPPING.get("Path");
				default -> throw new IllegalArgumentException();
			};
		}

		@Override
		public Object getColumnValue(Problem problem, int column) {
			return switch (column) {
				case 0 -> super.getColumnValue(problem, column);
				case 1 -> getFileHelperPath(problem);
				default -> throw new IllegalArgumentException();
			};
		}
	}

	private static class TemplateProblemTableColumnFactory extends ColumnFactory {
		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			switch (COLUMN_MAPPING.inverse().get(columnExt.getTitle())) {
				case "Name" -> {
					columnExt.setPreferredWidth(500);
					columnExt.setMaxWidth(1000);
					columnExt.setHideable(false);
				}
				case "Path" -> {
					columnExt.setPreferredWidth(75);
					columnExt.setMaxWidth(1000);
				}
			}
		}
	}

	private static class ProblemTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			setIcon(value instanceof Icon ? (Icon) value : null);

			return this;
		}
	}
}
