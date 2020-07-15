package de.george.g3dit.gui.dialogs;

import java.io.File;
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
import com.jidesoft.dialog.ButtonPanel;

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

	private static final TableColumnDef COLUMN_NAME = TableColumnDef.withName("Name").size(400).b();

	private final TableColumnDef COLUMN_PATH = TableColumnDef.withName("Path").size(600)
			.cellRenderer(new FileTableCellRenderer(() -> ctx.getOptionStore())).b();

	public FileSearchDialog(EditorContext ctx, String title) {
		super(ctx, title);
		setSize(1000, 700);
	}

	@Override
	public JComponent createContentPanel() {
		btnSearch = registerAction("Suchen", Icons.getImageIcon(Icons.Action.FIND), this::doWork, true);

		JPanel mainPanel = new JPanel(new MigLayout("fill", "[][][]"));

		tfSearchField = SwingUtils.createUndoTF();
		tfSearchField.setName("Filter");

		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText("Suche leeren");
		btnErase.addActionListener(e -> tfSearchField.setText(null));

		mainPanel.add(tfSearchField, "split 4, width 100%, spanx 4");
		cbRegex = new JCheckBox("Regex", false);
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
					problems.append(compName + " enthält ungültigen regulären Ausdruck.");
				}
			}
		});

		cbRegex.addActionListener(a -> group.performValidation());
		mainPanel.add(btnSearch, "height 23!");
		mainPanel.add(btnErase, "width 23!, height 23!, wrap");

		cbLrentdat = new JCheckBox("Lrentdat", Icons.getDisabledImageIcon(Icons.Document.LETTER_L), true);
		cbLrentdat.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_L));
		cbNode = new JCheckBox("Node", Icons.getDisabledImageIcon(Icons.Document.LETTER_N), true);
		cbNode.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_N));
		cbTple = new JCheckBox("Template", Icons.getDisabledImageIcon(Icons.Document.LETTER_T), true);
		cbTple.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_T));
		cbMesh = new JCheckBox("Mesh", Icons.getDisabledImageIcon(Icons.Document.LETTER_M), false);
		cbMesh.setSelectedIcon(Icons.getImageIcon(Icons.Document.LETTER_M));
		btnImport = new JButton("Entities aus ausgewählter Datei importieren", Icons.getImageIcon(Icons.IO.IMPORT));
		btnImport.setEnabled(false);
		mainPanel.add(cbLrentdat, "split 5, spanx 4");
		mainPanel.add(cbNode, "gapleft 7");
		mainPanel.add(cbTple, "gapleft 7");
		mainPanel.add(cbMesh, "gapleft 7");
		mainPanel.add(btnImport, "gapx push, wrap");

		results = new BasicEventList<>();
		SortedList<Result> sortedResults = new SortedList<>(results, Comparator.comparing(Result::getPath));
		table = TableUtil.createSortableTable(sortedResults, Result.class, COLUMN_NAME, COLUMN_PATH);
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
	public ButtonPanel createButtonPanel() {
		return null;
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
		Predicate<File> filter;
		String text = tfSearchField.getText();
		if (text.isEmpty() || !cbLrentdat.isSelected() && !cbNode.isSelected() && !cbTple.isSelected() && !cbMesh.isSelected()) {
			progressBar.setString("Ungültige Filtereinstellungen");
			return;
		}

		if (!cbRegex.isSelected()) {
			String matchText = text.toLowerCase();
			filter = f -> f.getName().toLowerCase().contains(matchText);
		} else {
			try {
				Pattern pattern = Pattern.compile(text);
				filter = f -> pattern.matcher(ctx.getFileManager().getRelativePath(f).orElseGet(() -> f.getName())).find();
			} catch (PatternSyntaxException e) {
				progressBar.setString("Ungültiger Regulärer Ausdruck");
				return;
			}
		}

		results.clear();

		FileManager fileManager = ctx.getFileManager();
		List<Callable<List<File>>> fileProviders = new ArrayList<>();
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
			fileProviders.add(() -> fileManager.listFiles(FileManager.RP_COMPILED_MESH, IOUtils.meshFilter));
		}

		worker = new SearchFileWorker(IOUtils.joinFileCallables(fileProviders), filter);
		executeWorker();
	}

	protected class SearchFileWorker extends AbstractFileWorker<Void, Result> {
		private Predicate<File> filter;

		protected SearchFileWorker(Callable<List<File>> fileProvider, Predicate<File> filter) {
			super(fileProvider, "Erstelle Liste aller Dateien...", "%d/%d Dateien betrachtet", "Suche abgeschlossen");
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
			List<File> files = getFiles();
			publish();
			for (File file : files) {
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
			return String.format("Suche abgeschlossen (%d Dateien gefunden)", results.size());
		}
	}

	public class Result {
		private File file;

		public Result(File file) {
			this.file = file;
		}

		public String getName() {
			return file.getName();
		}

		public File getPath() {
			return file;
		}

		public void open() {
			ctx.getEditor().openFile(file);
		}
	}
}
