package de.george.g3dit.tab.template.views.header;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.VectorValidator;
import de.george.lrentnode.classes.gCMap_PS;
import de.george.lrentnode.classes.gCMap_PS.MapMarker;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public class MapTab extends AbstractTemplateTab {
	private JTextField tfHeader, tfBitmap, tfWorldTopLeft, tfWorldBottomRight;
	private MapMarkerPanel markerPanel;

	public MapTab(EditorTemplateTab ctx) {
		super(ctx);
		setLayout(new MigLayout("", "[]20[]20[]"));

		add(SwingUtils.createBoldLabel("Eigenschaften"), "spanx, wrap");
		add(new JLabel("Header"), "gapleft 7, gaptop 5, wrap");
		tfHeader = SwingUtils.createUndoTF();
		add(tfHeader, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(new JLabel("Bitmap"), "gapleft 7, gaptop 5, wrap");
		tfBitmap = SwingUtils.createUndoTF();
		add(tfBitmap, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(new JLabel("WorldTopLeft"), "gapleft 7, gaptop 5, wrap");
		tfWorldTopLeft = SwingUtils.createUndoTF();
		add(tfWorldTopLeft, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(new JLabel("WorldBottomRight"), "gapleft 7, gaptop 5, wrap");
		tfWorldBottomRight = SwingUtils.createUndoTF();
		add(tfWorldBottomRight, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(SwingUtils.createBoldLabel("Markers"), "gaptop 10, wrap");
		markerPanel = new MapMarkerPanel();
		add(markerPanel, "gapleft 7, grow, spanx 4, width 400:500:500, wrap");
	}

	@Override
	public String getTabTitle() {
		return "Map";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCMap_PS.class);
	}

	@Override
	public void initValidation() {
		ValidationGroup group = getValidationPanel().getValidationGroup();
		tfHeader.setName("Header");
		group.add(tfHeader, EmtpyWarnValidator.INSTANCE);
		tfBitmap.setName("Bitmap");
		group.add(tfBitmap, EmtpyWarnValidator.INSTANCE);
		tfWorldTopLeft.setName("WorldTopLeft");
		group.add(tfWorldTopLeft, VectorValidator.INSTANCE);
		tfWorldBottomRight.setName("WorldBottomRight");
		group.add(tfWorldBottomRight, VectorValidator.INSTANCE);
	}

	@Override
	public void loadValues(TemplateFile tple) {
		gCMap_PS map = tple.getReferenceHeader().getClass(CD.gCMap_PS.class);

		PropertySync syncMap = PropertySync.wrap(map);
		syncMap.readString(tfHeader, CD.gCMap_PS.Header);
		syncMap.readString(tfBitmap, CD.gCMap_PS.Bitmap);
		syncMap.readVector(tfWorldTopLeft, CD.gCMap_PS.WorldTopLeft);
		syncMap.readVector(tfWorldBottomRight, CD.gCMap_PS.WorldBottomRight);

		markerPanel.clearMapMarkers();
		map.markers.forEach(markerPanel::addMapMarker);
	}

	@Override
	public void saveValues(TemplateFile tple) {
		gCMap_PS map = tple.getReferenceHeader().getClass(CD.gCMap_PS.class);

		PropertySync syncMap = PropertySync.wrap(map);
		syncMap.writeString(tfHeader, CD.gCMap_PS.Header);
		syncMap.writeString(tfBitmap, CD.gCMap_PS.Bitmap);
		syncMap.writeVector(tfWorldTopLeft, CD.gCMap_PS.WorldTopLeft);
		syncMap.writeVector(tfWorldBottomRight, CD.gCMap_PS.WorldBottomRight);

		map.markers.clear();
		map.markers.addAll(markerPanel.getMapMarkers());
	}

	private class MapMarkerPanel extends JPanel {
		private JXTable table;
		private MapMarkerTabelModel model;

		public MapMarkerPanel() {
			setLayout(new MigLayout("fillx, insets 0", "[]", "[grow][]"));

			table = new JXTable();
			TableUtil.disableSearch(table);
			table.setColumnFactory(new ItemTableColumFactory());
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			model = new MapMarkerTabelModel();
			table.setModel(model);

			JScrollPane scroll = new JScrollPane(table);
			add(scroll, "width 100%, sgx tableMembers, growy, wrap");

			add(new TableModificationControl<>(ctx, table, model, () -> new MapMarker("", bCVector.nullVector(), false)),
					"sgx tableMembers");
		}

		public void clearMapMarkers() {
			model.clearEntries();
		}

		public void addMapMarker(MapMarker marker) {
			model.addEntry(marker);
		}

		public List<MapMarker> getMapMarkers() {
			if (table.isEditing()) {
				table.getCellEditor().stopCellEditing();
			}

			return model.getEntries();
		}

		private class MapMarkerTabelModel extends ListTableModel<MapMarker> {
			public MapMarkerTabelModel() {
				super("Name", "Position", "Active");
			}

			@Override
			public void addEntry(MapMarker entry) {
				super.addEntry(entry);
			}

			@Override
			public Object getValueAt(MapMarker entry, int col) {
				switch (col) {
					case 0:
						return entry.name;
					case 1:
						return entry.position;
					case 2:
						return entry.active;
				}
				return null;
			}

			@Override
			public void setValueAt(Object value, int row, int col) {
				MapMarker entry = getEntries().get(row);
				switch (col) {
					case 0:
						if (!entry.name.equals(value)) {
							ctx.fileChanged();
							entry.name = (String) value;
						}
						break;
					case 1:
						bCVector position = bCVector.fromString((String) value);
						if (!entry.position.equals(position)) {
							ctx.fileChanged();
							entry.position = position;
						}
						break;
					case 2:
						if (entry.active != (boolean) value) {
							ctx.fileChanged();
							entry.active = (boolean) value;
						}
						break;
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		}

		private class ItemTableColumFactory extends ColumnFactory {

			@Override
			public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
				columnExt.setEditable(true);
				switch (columnExt.getTitle()) {
					case "Name":
						columnExt.setPreferredWidth(200);
						break;
					case "Position":
						columnExt.setPreferredWidth(200);
						break;
					case "Active":
						columnExt.setPreferredWidth(30);
						columnExt.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
						columnExt.setCellEditor(new JXTable.BooleanEditor());
						break;
					default:
						super.configureColumnWidths(table, columnExt);
				}
			}
		}
	}
}
