package de.george.g3dit.nav;

import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.rawHtml;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;

import com.google.common.base.Predicates;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.util.concurrent.Lock;
import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.gui.components.ComboBoxMatcherEditor;
import de.george.g3dit.gui.components.HtmlEditorPane;
import de.george.g3dit.gui.components.JSeverityComboBox;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.gui.components.SeverityImageIcon;
import de.george.g3dit.gui.components.SeverityMatcherEditor;
import de.george.g3dit.gui.renderer.FileListCellRenderer;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.gui.table.renderer.BooleanTableCellRenderer;
import de.george.g3dit.gui.table.renderer.FileTableCellRenderer;
import de.george.g3dit.gui.table.renderer.IconTableCellRenderer;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public abstract class NavCalcStage {
	protected final EditorContext ctx;
	protected final NavMap navMap;
	protected final NavCache navCache;
	protected final NavCalc navCalc;

	private JComponent component;
	private Consumer<NavCalcState> gotoState;

	private EventList<Change> changes;
	private AdvancedTableModel<Change> changeTableModel;
	private JLabel laFilteredCount;
	private JXTable changeTable;
	private HtmlEditorPane epDetails;

	private List<Runnable> repaintListeners;

	public NavCalcStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		this.ctx = ctx;
		this.navMap = navMap;
		this.navCache = navCache;
		this.navCalc = navCalc;
		this.gotoState = gotoState;
	}

	public abstract String getTitle();

	protected void getExtraButtons(JXTable changeTable, Consumer<JButton> add, Consumer<Runnable> repaintListener) {}

	public JComponent getComponent() {
		if (component == null) {
			component = createComponent();
		}
		return component;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	/**
	 * This method is thread-safe.
	 */
	protected void clearChanges() {
		Lock writeLock = changes.getReadWriteLock().writeLock();
		try {
			writeLock.lock();
			changes.clear();
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * This method is thread-safe.
	 *
	 * @param change
	 * @return
	 */
	protected Change addChange(Change change) {
		Lock writeLock = changes.getReadWriteLock().writeLock();
		try {
			writeLock.lock();
			changes.add(change);
		} finally {
			writeLock.unlock();
		}
		return change;
	}

	/**
	 * This method is thread-safe.
	 *
	 * @param change
	 */
	protected int getChangeCount() {
		Lock readLock = changes.getReadWriteLock().readLock();
		try {
			readLock.lock();
			return changes.size();
		} finally {
			readLock.unlock();
		}
	}

	protected void gotoState(NavCalcState state) {
		gotoState.accept(state);
	}

	protected abstract JComponent createComponent();

	private void updateFilteredCount() {
		laFilteredCount.setText(I.trf("{0, number} / {1, number} issues displayed", changeTableModel.getRowCount(), getChangeCount()));
	}

	private void onExecute() {
		doExecute();
		updateFilteredCount();
		if (!changeTable.hasFocus()) {
			changeTable.requestFocusInWindow();
		}
	}

	protected abstract void doExecute();

	protected static final TableColumnDef COLUMN_GUID = TableColumnDef.withName("Guid").displayName(I.tr("Guid"))
			.sizeExample(GuidUtil.randomGUID()).b();
	protected static final TableColumnDef COLUMN_MESSAGE = TableColumnDef.withName("Message").displayName(I.tr("Beschreibung"))
			.sizeExample("Something is wrong, please fix it before it grows.")
			.<Change, String>cellValueTransformer((change, value) -> new SeverityImageIcon(change.getSeverity(), value))
			.cellRenderer(new IconTableCellRenderer()).b();

	protected final TableColumnDef COLUMN_FIXED = TableColumnDef.withName("Fixed").displayName(I.tr("Behoben")).sizeExample(false)
			.editable(true).cellRenderer(new BooleanTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
						int column) {
					Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					int modelRow = table.convertRowIndexToModel(row);
					if (changeTableModel != null && modelRow < changeTableModel.getRowCount()) {
						component.setEnabled(changeTableModel.getElementAt(table.convertRowIndexToModel(row)).canBeFixed());
					}
					return component;
				}
			}).cellEditor(new JXTable.BooleanEditor() {
				@Override
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
					Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
					int modelRow = table.convertRowIndexToModel(row);
					if (changeTableModel != null && modelRow < changeTableModel.getRowCount()) {
						component.setEnabled(changeTableModel.getElementAt(table.convertRowIndexToModel(row)).canBeFixed());
					}
					return component;
				}
			}).b();

	protected final TableColumnDef COLUMN_FILE = TableColumnDef.withName("File").displayName(I.tr("Datei"))
			.cellRenderer(new FileTableCellRenderer(() -> getContext().getOptionStore()))
			.sizeExample(new File("G3_Nordmar_01_Landscape_Dynamic_Objects_02_SHyb.node")).b();

	protected static final TableColumnDef COLUMN_NAME = TableColumnDef.withName("Name").displayName(I.tr("Name"))
			.sizeExample("G3_Object_Interact_Animated_Chest_01").b();

	protected void addBackButton(JPanel mainPanel, NavCalcState state) {
		JButton btnBack = new JButton(I.trf("Back ({0}) <-", state.name()));
		btnBack.setFocusable(false);
		btnBack.addActionListener(a -> gotoState(state));
		SwingUtils.addWindowKeyStroke(btnBack, "Back", KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, () -> gotoState(state));
		mainPanel.add(btnBack, "alignx left");
	}

	protected void addNextButton(JPanel mainPanel, NavCalcState state) {
		JButton btnNext = new JButton(I.trf("-> Next ({0})", state.name()));
		btnNext.setFocusable(false);
		btnNext.addActionListener(a -> gotoState(state));
		SwingUtils.addWindowKeyStroke(btnNext, "Next", KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, () -> gotoState(state));
		mainPanel.add(btnNext, "spanx, alignx right");
	}

	protected JPanel createMainPanel() {
		return createMainPanel(Change.class, COLUMN_GUID, COLUMN_MESSAGE, COLUMN_FIXED, COLUMN_FILE);
	}

	protected JPanel createMainPanelNamed() {
		return createMainPanel(BaseNamedChange.class, COLUMN_NAME, COLUMN_GUID, COLUMN_MESSAGE, COLUMN_FIXED, COLUMN_FILE);
	}

	protected <T extends Change> JPanel createMainPanel(Class<T> changeClass, TableColumnDef... columns) {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		JProgressBar progressBar = SwingUtils.createProgressBar();
		mainPanel.add(progressBar, "spanx 10, split 2, pushx, growx 100, id progressbar");

		mainPanel.add(SwingUtils.keyStrokeButton(I.tr("Execute Scan"), Icons.getImageIcon(Icons.Action.DIFF), KeyEvent.VK_E,
				InputEvent.CTRL_DOWN_MASK, this::onExecute), "wrap");

		changes = new BasicEventList<>();

		JSeverityComboBox cbSeverity = new JSeverityComboBox();
		cbSeverity.setSelectedIndex(1);

		FilterList<Change> filteredChanges = new FilterList<>(changes, new SeverityMatcherEditor<>(cbSeverity, Change::getSeverity));

		CompositeList<File> availablesFiles = new CompositeList<>(filteredChanges.getPublisher(), filteredChanges.getReadWriteLock());
		availablesFiles.addMemberList(
				GlazedLists.eventListOf(availablesFiles.getPublisher(), availablesFiles.getReadWriteLock(), new File[] {null}));
		availablesFiles.addMemberList(UniqueList.create(GlazedLists.transformByFunction(filteredChanges, Change::getFile)));
		JComboBox<File> cbFiles = new JComboBox<>(GlazedListsSwing.eventComboBoxModelWithThreadProxyList(availablesFiles));
		cbFiles.setRenderer(new FileListCellRenderer(() -> getContext().getOptionStore()));

		mainPanel.add(cbFiles, "gaptop 7, split 3, width 350!");
		mainPanel.add(cbSeverity, "gaptop 9");
		laFilteredCount = new JLabel();
		mainPanel.add(laFilteredCount, "wmax 200, gapleft 10");

		filteredChanges = new FilterList<>(filteredChanges, new ComboBoxMatcherEditor<>(cbFiles, Change::getFile));

		SortedList<Change> sortedChanges = new SortedList<>(filteredChanges,
				Comparator.comparing(Change::getFile, Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(Change::getGuid)
						.thenComparing(Change::getSeverity).thenComparing(Change::getMessage));

		SortableEventTable<Change> sortableTable = TableUtil.createSortableTable(sortedChanges, changeClass, columns);
		changeTableModel = sortableTable.tableModel;
		changeTableModel.addTableModelListener(l -> updateFilteredCount());

		changeTable = sortableTable.table;
		changeTable.addMouseListener(TableUtil.createDoubleClickListener(this::onShowInEditor));
		changeTable.getSelectionModel().addListSelectionListener(e -> onSelect());

		repaintListeners = new ArrayList<>();
		getExtraButtons(changeTable, mainPanel::add, repaintListeners::add);

		JButton btnFix = SwingUtils.keyStrokeButton(I.tr("Fix"), I.tr("Fix the selected issue(s)."),
				Icons.getImageIcon(Icons.Misc.WAND_MAGIC), KeyEvent.VK_SPACE, 0, this::onFix);
		repaintListeners.add(TableUtil.enableOnGreaterEqual(changeTable, btnFix, 1,
				() -> getSelectedChange().map(change -> change.canBeFixed() && !change.isFixed()).orElse(false)));
		mainPanel.add(btnFix);

		JButton btnShowInEditor = SwingUtils.keyStrokeButton(I.tr("Open in editor"), I.tr("Open the affected nav object in the editor."),
				Icons.getImageIcon(Icons.Action.EDIT), KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, this::onShowInEditor);
		TableUtil.enableOnGreaterEqual(changeTable, btnShowInEditor, 1);
		mainPanel.add(btnShowInEditor);

		JButton btnTeleport = SwingUtils.keyStrokeButton(I.tr("Goto"), I.tr("Teleport player to the affected nav object."),
				Icons.getImageIcon(Icons.Misc.GEOLOCATION), KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK, this::onTeleport);
		TableUtil.enableOnGreaterEqual(changeTable, btnTeleport, 1);
		mainPanel.add(btnTeleport);

		JButton btnShowOnMap = SwingUtils.keyStrokeButton(I.tr("Show on map"), I.tr("Show the affected nav object on the entity map."),
				Icons.getImageIcon(Icons.Misc.MAP), KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, this::onShowOnMap);
		TableUtil.enableOnGreaterEqual(changeTable, btnShowOnMap, 1);
		mainPanel.add(btnShowOnMap, "wrap");

		JPanel tabContent = new JPanel(new MigLayout("insets 0, fill"));
		tabContent.add(new JScrollPane(changeTable), "height 80%, push, grow, wrap");

		epDetails = HtmlEditorPane.withCssTemplateFile("/css/issue-box.css");
		tabContent.add(epDetails.scrollPane, "height 20%, push, grow");
		mainPanel.add(tabContent, "push, span, grow, wrap");

		return mainPanel;
	}

	protected void repaintChanges() {
		changeTable.repaint();
		repaintListeners.forEach(Runnable::run);
	}

	protected Optional<Change> getSelectedChange() {
		return Optional.of(TableUtil.getSelectedRow(changeTable)).filter(index -> index >= 0).map(changeTableModel::getElementAt);
	}

	protected StreamEx<Change> getSelectedChanges() {
		return TableUtil.getSelectedRows(changeTable).map(changeTableModel::getElementAt);
	}

	private void onSelect() {
		Optional<Change> change = getSelectedChange();
		if (!change.isPresent()) {
			epDetails.setHtml();
			return;
		}

		epDetails.setHtml(b(rawHtml(change.get().getMessage())), p(),
				rawHtml(change.get().getDetails() != null ? change.get().getDetails() : ""));
	}

	private void onFix() {
		getSelectedChanges().filter(Predicates.and(Change::canBeFixed, Predicates.not(Change::isFixed))).forEach(Change::fix);
		repaintChanges();
	}

	private void onShowInEditor() {
		getSelectedChange().ifPresent(Change::showInEditor);
	}

	private void onShowInEditor(int index) {
		changeTableModel.getElementAt(index).showInEditor();
	}

	private void onTeleport() {
		getSelectedChange().ifPresent(Change::teleport);

	}

	private void onShowOnMap() {
		getSelectedChanges().forEach(Change::showOnMap);
	}

	private EditorContext getContext() {
		return ctx;
	}

	public abstract class BaseNamedChange extends BaseChange {
		public BaseNamedChange(String guid, Severity severity, String message, String details) {
			super(guid, severity, message, details);
		}

		public String getName() {
			return Caches.entity(ctx).getDisplayName(getGuid());
		}
	}
}
