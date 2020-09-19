package de.george.g3dit.tab.template.views.header;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.classes.gCItem_PS;
import de.george.lrentnode.classes.gCItem_PS.ScriptLine;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateFile;

public class ItemScriptTab extends AbstractPropertyTemplateTab {
	private static final String[] SCRIPT_COMMANDS = {"AddQuestLog", "Attack", "ClearGameEvent", "CloseQuest", "Give", "GiveXP",
			"PlaySound", "RunQuest", "SetGameEvent", "SetGuardStatus", "SetPartyEnabled", "SetRelaxingPoint", "SetRoutine",
			"SetSectorStatus", "SetSleepingPoint", "SetTeachEnabled", "SetTradeEnabled", "SetWorkingPoint", "ShowGameMessage",
			"StartEffect"};

	private static final TableColumnDef LINE_COMMAND = TableColumnDef.withName("Command").editable(true).cellEditor(Misc.evaluate(() -> {
		JComboBox<String> cbCommands = new JComboBox<>(Misc.concat(SCRIPT_COMMANDS));
		cbCommands.setEditable(true);
		return new DefaultCellEditor(cbCommands);
	})).b();
	private static final TableColumnDef LINE_ENTITY1 = TableColumnDef.withName("Entity1").editable(true).b();
	private static final TableColumnDef LINE_ENTITY2 = TableColumnDef.withName("Entity2").editable(true).b();
	private static final TableColumnDef LINE_ID1 = TableColumnDef.withName("ID1").fieldName("Id1").editable(true).b();
	private static final TableColumnDef LINE_ID2 = TableColumnDef.withName("ID2").fieldName("Id2").editable(true).b();

	public ItemScriptTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel) {
		// @foff
		propertyPanel
			.addHeadline("Script")
			.add(CD.gCItem_PS.class, gCItem_PS::getScript, gCItem_PS::setScript, ScriptLine.class)
				.tableColumns(LINE_COMMAND, LINE_ENTITY1, LINE_ENTITY2, LINE_ID1, LINE_ID2)
				.constraints("width 200:1200:1200").grow()
			.done();
		// @fon
	}

	@Override
	public String getTabTitle() {
		return "Script";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCItem_PS.class);
	}

}
