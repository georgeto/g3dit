package de.george.g3dit.check;

import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.rawHtml;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
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
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.HtmlEditorPane;
import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.gui.components.tab.JTypedTabbedPane;
import de.george.g3dit.gui.dialogs.CheckBoxListSelectDialog;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.ClasspathScanUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.SettingsHelper;
import de.george.g3dit.util.UriUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
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
	private CheckDialog checkDialog;

	public CheckManager(EditorContext ctx) {
		checks = new TreeSet<>(CheckManager::compareCheck);
		enabledChecks = new TreeSet<>(CheckManager::compareCheck);
		this.ctx = ctx;
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

	private String getFileHelperPath(Problem problem) {
		if (problem instanceof FileHelper) {
			File filePath = ((FileHelper) problem).getDescriptor().getPath().getParentFile();
			return SettingsHelper.applyAlias(ctx.getOptionStore(), filePath.getAbsolutePath());
		}
		return null;
	}

	private class CheckDialog extends ExtStandardDialog {
		private JProgressBar progressBar;
		private JButton btnToggleChecks;
		private JButton btnExecute;
		private EnableGroup egCheck;

		private EventList<Problem> problems;
		private EventList<Problem> filteredProblems;

		private Map<EntityDescriptor, EntityHelper> entityHelpers = new HashMap<>();
		private Map<FileDescriptor, FileHelper> fileHelpers = new HashMap<>();

		private CheckProblemConsumer problemConsumer = new CheckProblemConsumer();
		private ListenableFuture<Void> executeChecksFuture;
		private JTypedTabbedPane<ProblemTableTab> tabbedPane;

		public CheckDialog() {
			super(ctx.getParentWindow(), I.tr("Checks ausführen"));
			setSize(1000, 700);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (executeChecksFuture != null) {
						executeChecksFuture.cancel(true);
					}
				}
			});
		}

		private void toggleChecks() {
			CheckBoxListSelectDialog<Check> dialog = new CheckBoxListSelectDialog<>(this, I.tr("Checks auswählen"), checks,
					new BeanListCellRenderer("Title", "Description", 80));
			dialog.setSelectedEntries(enabledChecks);
			if (dialog.openAndWasSuccessful()) {
				setEnabledChecks(dialog.getSelectedEntries(), true);
			}
		}

		private void executeChecks() {
			if (checks.isEmpty()) {
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
				updateProgressBar(String.format(I.tr("Ermittele zu überprüfende Dateien...")));

				List<File> worldFiles = archivePasses != 0 ? ctx.getFileManager().listWorldFiles() : null;
				List<File> templateFiles = templatePasses != 0 ? ctx.getFileManager().listTemplateFiles() : null;

				for (int pass = 0; pass < passes; pass++) {
					if (pass < templatePasses) {
						// log("Template-Pass " + pass + "...");
						updateProgressBar(I.trf("Template-Pass {0, number}/{1, number}", pass + 1, templatePasses));
						TemplateFileIterator iter = new TemplateFileIterator(templateFiles);
						while (iter.hasNext()) {
							// Check execution got cancelled
							if (Thread.interrupted()) {
								return;
							}

							TemplateFile tple = iter.next();
							File tpleFile = iter.nextFile();
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
						// log("Template-Pass " + pass + " beendet");
					}

					if (pass < archivePasses) {
						// log("Archive-Pass " + pass + "...");
						updateProgressBar(I.trf("Archive-Pass {0, number}/{1, number}", pass + 1, archivePasses));
						ArchiveFileIterator iter = new ArchiveFileIterator(worldFiles);
						while (iter.hasNext()) {
							// Check execution got cancelled
							if (Thread.interrupted()) {
								return;
							}

							ArchiveFile archive = iter.next();
							File archiveFile = iter.nextFile();
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
						// log("Archive-Pass " + pass + " beendet");
					}
				}

				updateProgressBar(I.tr("Sammle Probleme..."));
				for (Check check : enabledChecks) {
					if (Thread.interrupted()) {
						return;
					}
					check.reportProblems(problemConsumer);
				}

				// TODO: Also execute when check execution gets cancelled
				enabledChecks.forEach(Check::reset);
			}, new FutureCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					onCompletion();
				}

				@Override
				public void onFailure(Throwable t) {
					logger.warn("Failure during check execution.", t);
					onCompletion();
				}

				private void onCompletion() {
					progressBar.setIndeterminate(false);
					progressBar.setString(I.tr("Ausführung abgeschlossen"));
					egCheck.setEnabled(true);
					ctx.runGC();
					executeChecksFuture = null;
				}
			}, ctx.getExecutorService());
		}

		private void updateProgressBar(String message) {
			SwingUtilities.invokeLater(() -> progressBar.setString(message));
		}

		@Override
		public JComponent createContentPanel() {
			JPanel mainPanel = new JPanel(new MigLayout("fill"));
			egCheck = new EnableGroup();

			progressBar = SwingUtils.createProgressBar();
			mainPanel.add(progressBar, "grow");

			btnToggleChecks = new JButton(I.tr("Checks auswählen"));
			btnToggleChecks.addActionListener(a -> toggleChecks());
			egCheck.add(btnToggleChecks);
			mainPanel.add(btnToggleChecks, "alignx right");

			btnExecute = new JButton(I.tr("Checks ausführen"));
			btnExecute.addActionListener(a -> executeChecks());
			egCheck.add(btnExecute);
			mainPanel.add(btnExecute, "alignx right, wrap");

			UndoableTextField tfSearch = SwingUtils.createUndoTF();
			mainPanel.add(tfSearch, "spanx, growx, wrap");
			TextComponentMatcherEditor<Problem> filteredProblemsMatcherEditor = new TextComponentMatcherEditor<>(tfSearch,
					GlazedLists.textFilterator("Message", "Details"));

			problems = new BasicEventList<>();
			filteredProblems = new FilterList<>(problems, filteredProblemsMatcherEditor);

			tabbedPane = new JTypedTabbedPane<>();
			tabbedPane.setTabPlacement(SwingConstants.BOTTOM);

			for (Category category : Category.values()) {
				if (category == Category.Helper) {
					continue;
				}

				tabbedPane.addTab(new ProblemTableTab(category));
			}

			mainPanel.add(tabbedPane.getComponent(), "gaptop 7, push, span, grow, wrap");
			return mainPanel;
		}

		private class ProblemTableTab implements ITypedTab {
			private Category category;
			private FilterList<Problem> filteredList;
			private JPanel tabContent;
			private TreeList<Problem> treeList;
			private AdvancedTableModel<Problem> problemTableModel;
			private HtmlEditorPane epDetails;

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
						treeFormat = new EntityProblemTreeFormat();
						tableFormat = new EntityProblemTableFormat();
						tableColumnFactory = new EntityProblemTableColumnFactory();
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
				JXTable problemTable = new JXTable();
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
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), () -> TableUtil.withSelectedRow(problemTable, this::onNavigate));
				SwingUtils.addKeyStroke(problemTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "Delete",
						KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), () -> onDelete(TableUtil.getSelectedRows(problemTable)));
				problemTable.getSelectionModel()
						.addListSelectionListener(e -> TableUtil.withSelectedRowOrInvalid(problemTable, this::onSelect));

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
			}

			@Override
			public String getTabTitle() {
				return category.toString() + " (" + filteredList.size() + ")";
			}

			@Override
			public Color getTitleColor() {
				Severity severest = filteredList.stream().map(Problem::getSeverity).max(Severity::compareTo).orElse(Severity.Info);
				if (severest == Severity.Warning) {
					return Color.ORANGE.darker();
				}
				if (severest == Severity.Fatal) {
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

			private void onNavigate(Integer index) {
				Problem problem = problemTableModel.getElementAt(index);
				if (problem.canNavigate()) {
					problem.navigate(ctx);
				}
			}

			private void onDelete(StreamEx<Integer> indices) {
				Set<Problem> problemsToDelete = new HashSet<>();
				Queue<Node<Problem>> openNodes = new LinkedList<>();
				indices.map(treeList::getTreeNode).forEach(openNodes::add);
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

			private void onSelect(Integer index) {
				if (index == -1) {
					epDetails.setHtml();
					return;
				}

				Problem problem = problemTableModel.getElementAt(index);
				if (problem instanceof FileHelper || problem instanceof EntityHelper) {
					epDetails.setHtml(rawHtml(problem.getDetails()));
				} else {
					epDetails.setHtml(b(rawHtml(problem.getMessage())), p(),
							rawHtml(problem.getDetails() != null ? problem.getDetails() : ""));
				}
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

	private static final ImmutableBiMap<String, String> COLUMN_MAPPING = ImmutableBiMap.of("Name", I.tr("Name"), "Guid", I.tr("Guid"),
			"Index", I.tr("Index"), "Path", I.tr("Pfad"));

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
					String icon = null;
					if (problem.getSeverity() != null) {
						icon = switch (problem.getSeverity()) {
							case Info -> Icons.Signal.INFO;
							case Warning -> Icons.Signal.WARN;
							case Fatal -> Icons.Signal.ERROR;
						};
					}

					// Strip all html tags
					String message = problem.getMessage().replaceAll("<[^>]+>", "");
					if (icon != null) {
						return new ImageIcon(Icons.getImageIcon(icon).getImage(), message);
					} else {
						return message;
					}
				}
				default -> throw new IllegalArgumentException();
			}
		}
	}

	private static class EntityProblemTreeFormat extends ProblemTreeFormat {
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
				case 2 -> (p1, p2) -> compareProblem(p1, p2, ProblemTreeFormat::compareFileHelper);
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
