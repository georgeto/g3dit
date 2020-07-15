package de.george.g3dit.tab.archive.views.entity;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SingleColumnTableModel;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCAnchor_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEAnchorType;
import net.miginfocom.swing.MigLayout;

public class AnchorTab extends AbstractEntityTab {
	private JEnumComboBox<gEAnchorType> cbAnchorType;

	private JTable tableInteractPoints;
	private SingleColumnTableModel<String> modelInteractPoints;

	public AnchorTab(EditorArchiveTab ctx) {
		super(ctx);

		setLayout(new MigLayout("fillx", "[]"));

		add(new JLabel("AnchorType"), "wrap");

		cbAnchorType = new JEnumComboBox<>(gEAnchorType.class);
		add(cbAnchorType, "width 50:100:100, wrap");

		add(new JLabel("Childs"), "wrap");

		modelInteractPoints = new SingleColumnTableModel<>("Points", true);
		tableInteractPoints = new JTable(modelInteractPoints);
		tableInteractPoints.setTableHeader(null);
		JScrollPane scroll = new JScrollPane(tableInteractPoints);
		add(scroll, "width 150:300:300, sgx tableInteractPoints, wrap");

		add(new TableModificationControl<>(ctx, tableInteractPoints, modelInteractPoints, () -> ""), "gapleft 7, sgx tableInteractPoints");
	}

	@Override
	public String getTabTitle() {
		return "Anchor";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCAnchor_PS.class);
	}

	@Override
	public void initValidation() {}

	@Override
	public void loadValues(eCEntity entity) {
		gCAnchor_PS anchor = entity.getClass(CD.gCAnchor_PS.class);

		PropertySync.wrap(anchor).readEnum(cbAnchorType, CD.gCAnchor_PS.AnchorType);
		modelInteractPoints.setEntries(anchor.interactPoints.getNativeEntries());
	}

	@Override
	public void saveValues(eCEntity entity) {
		gCAnchor_PS anchor = entity.getClass(CD.gCAnchor_PS.class);

		PropertySync.wrap(anchor).writeEnum(cbAnchorType, CD.gCAnchor_PS.AnchorType);

		if (tableInteractPoints.isEditing()) {
			tableInteractPoints.getCellEditor().stopCellEditing();
		}

		anchor.interactPoints.setNativeEntries(
				modelInteractPoints.getEntries().stream().map(GuidUtil::parseGuid).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}
