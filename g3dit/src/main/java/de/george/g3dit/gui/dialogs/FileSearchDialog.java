package de.george.g3dit.gui.dialogs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;

import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.gui.table.renderer.FileTableCellRenderer;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.AbstractFileWorker;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ImportHelper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;
import net.miginfocom.swing.MigLayout;

public class FileSearchDialog extends AbstractTableProgressDialog {
	protected JButton btnSearch;

	protected UndoableTextField tfSearchField;
	protected JCheckBox cbRegex;

	private JCheckBox cbLrentdat, cbNode, cbTple, cbMesh;

	private JButton btnImport;

	private EventList<Result> results;
	private SortableEventTable<Result> table;

	private static final TableColumnDef COLUMN_NAME = TableColumnDef.withName("Name").displayName(I.tr("Name")).size(400).b();

	private final TableColumnDef COLUMN_PATH = TableColumnDef.withName("Path").displayName(I.tr("Path")).size(600)
			.cellRenderer(new FileTableCellRenderer(() -> ctx.getOptionStore())).b();

	public FileSearchDialog(EditorContext ctx, String title) {
		super(ctx, title);
		setSize(1000, 700);
	}

	@Override
	public JComponent createContentPanel() {
		btnSearch = registerAction(I.tr("Search"), Icons.getImageIcon(Icons.Action.FIND), this::doWork, true);

		JPanel mainPanel = new JPanel(new MigLayout("fill", "[][][]"));

		tfSearchField = SwingUtils.createUndoTF();
		tfSearchField.setName(I.tr("Filter"));

		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText(I.tr("Clear search"));
		btnErase.addActionListener(e -> tfSearchField.setText(null));

		mainPanel.add(tfSearchField, "split 4, width 100%, spanx 4");
		cbRegex = new JCheckBox(I.tr("Regex"), false);
		mainPanel.add(cbRegex);

		SwingValidationGroup group = SwingValidationGroup.create();
		group.add(tfSearchField, new AbstractValidator<String>(String.class) {
			@Override
			public void validate(Problems problems, String compName, String model) {
				if (!cbRegex.isSelected()) {
					return;
				}

				try {
					Pattern.compile(model);
				} catch (Exception e) {
					problems.append(I.trf("{0} contains invalid regular expression.", compName));
				}
			}
		});

		cbRegex.addActionListener(a -> group.performValidation());
		mainPanel.add(btnSearch, "height 23!");
		mainPanel.add(btnErase, "width 23!, height 23!, wrap");

		cbLrentdat = new JCheckBox(I.tr("Lrentdat"), Icons.getDisabledImageIcon(Icons.Document.LETTER_L), true);
		cbLrentdat.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_L));
		cbNode = new JCheckBox(I.tr("Node"), Icons.getDisabledImageIcon(Icons.Document.LETTER_N), true);
		cbNode.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_N));
		cbTple = new JCheckBox(I.tr("Template"), Icons.getDisabledImageIcon(Icons.Document.LETTER_T), true);
		cbTple.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_T));
		cbMesh = new JCheckBox(I.tr("Mesh"), Icons.getDisabledImageIcon(Icons.Document.LETTER_M), false);
		cbMesh.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_M));
		btnImport = new JButton(I.tr("Import entities from selected file"), Icons.getImageIcon(Icons.IO.IMPORT));
		btnImport.setEnabled(false);
		mainPanel.add(cbLrentdat, "split 5, spanx 4");
		mainPanel.add(cbNode, "gapleft 7");
		mainPanel.add(cbTple, "gapleft 7");
		mainPanel.add(cbMesh, "gapleft 7");
		mainPanel.add(btnImport, "gapx push, wrap");

		results = new BasicEventList<>();
		SortedList<Result> sortedResults = new SortedList<>(results, Comparator.comparing(Result::getPath));
		table = TableUtil.createSortableTable(ctx, sortedResults, Result.class, COLUMN_NAME, COLUMN_PATH);
		appendBarAndTable(mainPanel, table.table);

		// Open in editor
		setEntryActivationListener(i -> table.getRowAt(i).open());

		// Import entities from file
		table.addSelectionListener(e -> assessEnableImport(ctx.getEditor().getSelectedTab()));
		btnImport.addActionListener(a -> {
			EditorArchiveTab archiveTab = ctx.getEditor().<EditorArchiveTab>getSelectedTab(EditorTabType.Archive).get();
			Result entry = table.getSelectedRow().get();
			if (ImportHelper.importFromFile(entry.file, archiveTab.getCurrentFile(), ctx)) {
				archiveTab.refreshTree(false);
			}
		});
		ctx.eventBus().register(this);
		return mainPanel;
	}

	@Override
	public void dispose() {
		ctx.eventBus().unregister(this);
		super.dispose();
	}

	@Subscribe
	public void onSelectTab(EditorTab.SelectedEvent event) {
		assessEnableImport(event.getTab());
	}

	private void assessEnableImport(Optional<EditorTab> tab) {
		Optional<Result> entry = table.getSelectedRow();
		btnImport.setEnabled(entry.isPresent() && tab.isPresent() && tab.get().type() == EditorTabType.Archive && Optional
				.of(entry.get().getName().toLowerCase()).filter(n -> n.contains(".lrentdat") || n.contains(".node")).isPresent());
	}

	public void doWork() {
		Predicate<Path> filter;
		String text = tfSearchField.getText();
		if (text.isEmpty() || !cbLrentdat.isSelected() && !cbNode.isSelected() && !cbTple.isSelected() && !cbMesh.isSelected()) {
			progressBar.setString(I.tr("Invalid filter settings"));
			return;
		}

		if (!cbRegex.isSelected()) {
			String matchText = text.toLowerCase();
			filter = f -> FilesEx.getFileNameLowerCase(f).contains(matchText);
		} else {
			try {
				Pattern pattern = Pattern.compile(text);
				filter = f -> pattern.matcher(ctx.getFileManager().getRelativePath(f).orElseGet(() -> FilesEx.getFileName(f))).find();
			} catch (PatternSyntaxException e) {
				progressBar.setString(I.tr("Invalid regular expression"));
				return;
			}
		}

		results.clear();

		FileManager fileManager = ctx.getFileManager();
		List<Callable<List<Path>>> fileProviders = new ArrayList<>();
		if (cbLrentdat.isSelected() && cbNode.isSelected()) {
			fileProviders.add(fileManager.worldFilesCallable());
		} else if (cbLrentdat.isSelected()) {
			fileProviders.add(() -> fileManager.listFiles(FileManager.RP_PROJECTS_COMPILED, IOUtils.lrentdatFileFilter));
		} else if (cbNode.isSelected()) {
			fileProviders.add(() -> fileManager.listFiles(FileManager.RP_PROJECTS_COMPILED, IOUtils.nodeFileFilter));
		}

		if (cbTple.isSelected()) {
			fileProviders.add(fileManager.templateFilesCallable());
		}

		if (cbMesh.isSelected()) {
			fileProviders.add(fileManager::listMeshes);
		}

		worker = new SearchFileWorker(IOUtils.joinFileCallables(fileProviders), filter);
		executeWorker();
	}

	protected class SearchFileWorker extends AbstractFileWorker<Void, Result> {
		private Predicate<Path> filter;

		protected SearchFileWorker(Callable<List<Path>> fileProvider, Predicate<Path> filter) {
			super(fileProvider, I.tr("Creating list of all files..."), I.tr("{0, number}/{1, number} files scanned"),
					I.tr("Search completed"));
			this.filter = filter;
			setProgressBar(progressBar);
			doneMessageSupplier = this::getDoneMessage;
		}

		@Override
		protected void process(List<Result> resultsToProcess) {
			super.process(resultsToProcess);

			if (!isCancelled()) {
				results.addAll(resultsToProcess);
			}
		}

		@Override
		protected Void doInBackground() throws Exception {
			List<Path> files = getFiles();
			publish();
			for (Path file : files) {
				if (isCancelled()) {
					return null;
				}

				if (!filter.test(file)) {
					filesDone.incrementAndGet();
					publish();
					continue;
				}

				filesDone.incrementAndGet();
				publish(new Result(file));
			}
			return null;
		}

		private String getDoneMessage() {
			return I.trf("Search completed ({0, number} files found)", results.size());
		}
	}

	public class Result {
		private Path file;

		public Result(Path file) {
			this.file = file;
		}

		public String getName() {
			return FilesEx.getFileName(file);
		}

		public Path getPath() {
			return file;
		}

		public void open() {
			ctx.getEditor().openFile(file);
		}
	}
}
