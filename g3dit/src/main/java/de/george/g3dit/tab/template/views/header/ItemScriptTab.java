package de.george.g3dit.tab.template.views.header;

import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.classes.gCItem_PS;
import de.george.lrentnode.classes.gCItem_PS.ScriptLine;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public class ItemScriptTab extends AbstractTemplateTab {
	private static final String[] SCRIPT_COMMANDS = {"AddQuestLog", "Attack", "ClearGameEvent", "CloseQuest", "Give", "GiveXP",
			"PlaySound", "RunQuest", "SetGameEvent", "SetGuardStatus", "SetPartyEnabled", "SetRelaxingPoint", "SetRoutine",
			"SetSectorStatus", "SetSleepingPoint", "SetTeachEnabled", "SetTradeEnabled", "SetWorkingPoint", "ShowGameMessage",
			"StartEffect"};

	private JXTable scriptTable;
	private ScriptTableModel scriptModel;

	public ItemScriptTab(EditorTemplateTab ctx) {
		super(ctx);
		setLayout(new MigLayout("fill, insets 0", "[]"));

		scriptTable = new JXTable();
		TableUtil.disableSearch(scriptTable);
		scriptTable.setColumnFactory(new ScriptTableColumFactory());
		scriptTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scriptModel = new ScriptTableModel();
		scriptTable.setModel(scriptModel);

		add(SwingUtils.createBoldLabel("Script"), "wrap");
		add(new JScrollPane(scriptTable), "width 200:1200:1200, sgx table, grow, push, wrap");
		add(new TableModificationControl<>(ctx, scriptTable, scriptModel, () -> new ScriptLine()), "sgx table, grow, wrap");
	}

	@Override
	public String getTabTitle() {
		return "Script";
	}

	@Override
	public void loadValues(TemplateFile tple) {
		gCItem_PS item = tple.getReferenceHeader().getClass(CD.gCItem_PS.class);
		scriptModel.clearEntries();
		item.getScript().forEach(scriptModel::addEntry);
	}

	@Override
	public void saveValues(TemplateFile tple) {
		TableUtil.stopEditing(scriptTable);

		gCItem_PS item = tple.getReferenceHeader().getClass(CD.gCItem_PS.class);
		item.setScript(new ArrayList<>(scriptModel.getEntries()));
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCItem_PS.class);
	}

	@Override
	public void initValidation() {

	}

	private class ScriptTableModel extends ListTableModel<ScriptLine> {
		public ScriptTableModel() {
			super("Command", "Entity1", "Entity2", "ID1", "ID2");
		}

		@Override
		public Object getValueAt(ScriptLine line, int col) {
			switch (col) {
				case 0:
					return line.command;
				case 1:
					return line.entity1;
				case 2:
					return line.entity2;
				case 3:
					return line.id1;
				case 4:
					return line.id2;
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			ScriptLine line = getEntries().get(row);
			switch (col) {
				case 0:
					line.command = (String) value;
					break;
				case 1:
					line.entity1 = (String) value;
					break;
				case 2:
					line.entity2 = (String) value;
					break;
				case 3:
					line.id1 = (String) value;
					break;
				case 4:
					line.id2 = (String) value;
					break;
			}
			ctx.fileChanged();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
	}

	private class ScriptTableColumFactory extends ColumnFactory {

		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			columnExt.setEditable(true);
			switch (columnExt.getTitle()) {
				case "Command":
					JComboBox<String> cbCommands = new JComboBox<>(Misc.concat(SCRIPT_COMMANDS));
					cbCommands.setEditable(true);
					columnExt.setCellEditor(new DefaultCellEditor(cbCommands));
				case "Entity1":
				case "Entity2":
				case "ID1":
				case "ID2":
				default:
					super.configureColumnWidths(table, columnExt);
			}
		}
	}
}
