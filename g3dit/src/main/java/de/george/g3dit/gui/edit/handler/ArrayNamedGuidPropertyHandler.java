package de.george.g3dit.gui.edit.handler;

import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.gui.table.renderer.NamedGuidTableCellReditor;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.eCEntityProxy;

public class ArrayNamedGuidPropertyHandler extends TitledPropertyHandler<bTObjArray_eCEntityProxy> {
	private final EditorContext ctx;
	private final boolean template;

	private SortableEventTable<eCEntityProxy> table;

	public ArrayNamedGuidPropertyHandler(PropertyPanelDef def, EditorContext ctx, boolean template) {
		super(def);
		this.ctx = ctx;
		this.template = template;
	}

	@Override
	protected void addValueComponent(JPanel content) {
		TableColumnDef column = TableColumnDef.withName("Guid").size(700).editable(true).cellRenderer(buildTableReditor(false))
				.cellEditor(buildTableReditor(true)).b();

		table = TableUtil.createTable(GlazedLists.eventList(null), eCEntityProxy.class, column);
		table.table.setTableHeader(null);
		table.table.setRowHeight((int) (table.table.getRowHeight() * 1.3));
		content.add(new JScrollPane(table.table), "sgx table, wrap");
		FileChangeMonitor changeMonitor = ctx instanceof FileChangeMonitor ? (FileChangeMonitor) ctx : null;
		content.add(new TableModificationControl<>(changeMonitor, table.table, table.sortedSource, eCEntityProxy::new), "sgx table");

		// Update validation when model changes
		if (def.hasValidators()) {
			table.addModelListener(l -> def.getValidation().performValidation());
		}
	}

	@Override
	protected void load(bTObjArray_eCEntityProxy value) {
		table.sortedSource.clear();
		table.sortedSource.addAll(value.getEntries(eCEntityProxy::clone));
	}

	@Override
	protected bTObjArray_eCEntityProxy save() {
		TableUtil.stopEditing(table.table);

		bTObjArray_eCEntityProxy result = new bTObjArray_eCEntityProxy();
		result.setEntries(table.sortedSource, eCEntityProxy::clone);
		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private NamedGuidTableCellReditor buildTableReditor(boolean editor) {
		if (def.hasValidators()) {
			Validator[] validators = def.getValidators();
			validators = Arrays.stream(validators).map(v -> new AbstractValidator(v.modelType()) {
				@Override
				public void validate(Problems problems, String compName, Object model) {
					// Only validate renderer if table is not empty and only validate editor if it
					// is active
					boolean validate = table != null && (editor ? table.table.isEditing() : !table.sortedSource.isEmpty());
					if (validate) {
						v.validate(problems, compName, model);
					}
				}
			}).toArray(AbstractValidator[]::new);

			return new NamedGuidTableCellReditor(ctx, template, def.getValidation(), def.getName(), validators);
		} else {
			return new NamedGuidTableCellReditor(ctx, template);
		}
	}
}
