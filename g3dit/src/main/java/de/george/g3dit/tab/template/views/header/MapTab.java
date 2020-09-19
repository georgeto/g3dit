package de.george.g3dit.tab.template.views.header;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.editor.LambdaConvertEditor;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.VectorValidator;
import de.george.lrentnode.classes.gCMap_PS;
import de.george.lrentnode.classes.gCMap_PS.MapMarker;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateFile;

public class MapTab extends AbstractPropertyTemplateTab {
	private static final TableColumnDef MARKER_NAME = TableColumnDef.withName("Name").editable(true).size(250).b();
	private static final TableColumnDef MARKER_POSITION = TableColumnDef.withName("Position").editable(true)
			.cellEditor(new LambdaConvertEditor<>(bCVector::toString, bCVector::fromString)).size(250).b();
	private static final TableColumnDef MARKER_ACTIVE = TableColumnDef.withName("Active").editable(true).size(30)
			.cellRenderer(new DefaultTableRenderer(new CheckBoxProvider())).cellEditor(new JXTable.BooleanEditor()).b();

	public MapTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel) {
		// @foff
		propertyPanel
			.addHeadline("Eigenschaften")
			.add(CD.gCMap_PS.Header)
				.constraints("width 175:225:250").growx()
				.validate(validation(), EmtpyWarnValidator.INSTANCE)
			.add(CD.gCMap_PS.Bitmap)
				.constraints("width 175:225:250").growx()
				.validate(validation(), EmtpyWarnValidator.INSTANCE)
			.add(CD.gCMap_PS.WorldTopLeft)
				.constraints("width 175:225:250").growx()
				.validate(validation(), VectorValidator.INSTANCE)
			.add(CD.gCMap_PS.WorldBottomRight)
				.constraints("width 175:225:250").growx()
				.validate(validation(), VectorValidator.INSTANCE)
			.addHeadline("Markers")
			.add(CD.gCMap_PS.class, gCMap_PS::getMarkers, gCMap_PS::setMarkers, MapMarker.class)
				.tableColumns(MARKER_NAME, MARKER_POSITION, MARKER_ACTIVE)
				.constraints("width 500:600:700")
				.grow()
			.done();
		// @fon
	}

	@Override
	public String getTabTitle() {
		return "Map";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCMap_PS.class);
	}
}
