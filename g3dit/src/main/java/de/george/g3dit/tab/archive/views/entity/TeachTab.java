package de.george.g3dit.tab.archive.views.entity;

import javax.swing.DefaultCellEditor;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.edit.handler.RefFilters;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;

public class TeachTab extends AbstractPropertyEntityTab {
	private static final String[] ATTRIBUTES = {"STR", "DEX", "INT", "SMT", "THF", "ALC", "HP", "SP", "MP"};

	private static final TableColumnDef ATTRIBUTE_COLUMN = TableColumnDef.withName("String").editable(true)
			.cellEditor(new DefaultCellEditor(new JComboBoxExt<>(ATTRIBUTES))).b();

	public TeachTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		propertyPanel
			.add(CD.gCNPC_PS.TeachAttribs)
			.name("Attributes")
				.tableColumns(ATTRIBUTE_COLUMN)
				.grow().sizegroup("teachTables")
			.add(CD.gCNPC_PS.TeachSkills)
				.name("Skills")
				.customize(RefFilters::tpleSkillOrMagic)
				.validate(validation(), GuidValidator.INSTANCE, new TemplateExistenceValidator(validation(), ctx))
				.grow().sizegroup("teachTables")
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Teach";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCNPC_PS.class);
	}
}
