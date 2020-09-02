package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import com.ezware.dialog.task.TaskDialogs;
import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.gui.components.JEventList;
import de.george.g3dit.gui.dialogs.AbstractSelectDialog;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.dialogs.ListSelectDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;

public class EditVegetationMeshesDialog extends ExtStandardDialog {

	private EditorAbstractFileTab ctx;
	private eCVegetation_PS vegetationPS;

	private JEventList<eCVegetation_Mesh> listMeshes;
	private EventList<eCVegetation_Mesh> eventList;

	private JTextField tfMeshFilePath, tfMeshID, tfDiffuseTexture, tfUseCount, tfVertices, tfNormals, tfUVs, tfIndices;
	private UndoableTextField tfWindStrength;

	public EditVegetationMeshesDialog(Window owner, EditorAbstractFileTab ctx, eCVegetation_PS vegetationPS) {
		super(owner, I.tr("Vegetation-Meshes bearbeiten"), true);
		this.ctx = ctx;
		setType(Type.UTILITY);
		setSize(800, 500);
		setResizable(true);

		this.vegetationPS = vegetationPS;
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fillx, insets dialog", "[fill, grow][fill, grow][fill, grow][fill, grow]push[]"));
		add(mainPanel, BorderLayout.CENTER);

		eventList = GlazedLists.eventList(vegetationPS.getMeshClasses());
		listMeshes = new JEventList<>(eventList);
		listMeshes.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		listMeshes.setCellRenderer(new BeanListCellRenderer("Name"));
		listMeshes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMeshes.addListSelectionListener(e -> meshSelected());
		mainPanel.add(new JScrollPane(listMeshes), "dock west, height 100%, width 250:300:350, grow");

		mainPanel.add(new JLabel(I.tr("MeshFilePath")), "spanx 4, wrap");
		tfMeshFilePath = new JTextField();
		tfMeshFilePath.setEditable(false);
		mainPanel.add(tfMeshFilePath, "spanx 4, growx, wrap");

		mainPanel.add(new JLabel(I.tr("MeshID")), "wrap");
		tfMeshID = new JTextField();
		tfMeshID.setEditable(false);
		mainPanel.add(tfMeshID, "wrap");

		mainPanel.add(new JLabel(I.tr("DiffuseTexture")), "spanx 4, wrap");
		tfDiffuseTexture = new JTextField();
		tfDiffuseTexture.setEditable(false);
		mainPanel.add(tfDiffuseTexture, "spanx 4, growx, wrap");

		mainPanel.add(new JLabel(I.tr("Anzahl der Vorkommen")), "spanx 4, wrap");
		tfUseCount = new JTextField();
		tfUseCount.setEditable(false);
		mainPanel.add(tfUseCount, "wrap");

		mainPanel.add(new JLabel(I.tr("Vertices")));
		mainPanel.add(new JLabel(I.tr("Normals")));
		mainPanel.add(new JLabel(I.tr("UVs")));
		mainPanel.add(new JLabel(I.tr("Indices")), "wrap");
		tfVertices = new JTextField();
		tfVertices.setEditable(false);
		tfNormals = new JTextField();
		tfNormals.setEditable(false);
		tfUVs = new JTextField();
		tfUVs.setEditable(false);
		tfIndices = new JTextField();
		tfIndices.setEditable(false);
		mainPanel.add(tfVertices, "sgx stats");
		mainPanel.add(tfNormals, "sgx stats");
		mainPanel.add(tfUVs, "sgx stats");
		mainPanel.add(tfIndices, "sgx stats, wrap");

		mainPanel.add(new JLabel(I.tr("WindStrength")), "wrap");
		tfWindStrength = SwingUtils.createUndoTF();
		mainPanel.add(tfWindStrength, "");

		Action saveAction = SwingUtils.createAction(I.tr("Speichern"), Icons.getImageIcon(Icons.IO.SAVE), () -> {
			PropertySync.wrap(listMeshes.getSelectedValue()).writeFloat(tfWindStrength, CD.eCVegetation_Mesh.WindStrength);
			tfWindStrength.setText(Misc.formatFloat(listMeshes.getSelectedValue().property(CD.eCVegetation_Mesh.WindStrength).getFloat()),
					true);
		});

		JButton btnSave = new JButton(saveAction);
		mainPanel.add(btnSave, "wrap, gapy push");
		tfWindStrength.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> {
			btnSave.setEnabled(!tfWindStrength.getText()
					.equals(Misc.formatFloat(listMeshes.getSelectedValue().property(CD.eCVegetation_Mesh.WindStrength).getFloat())));
		}));

		listMeshes.setSelectedIndex(0);

		return mainPanel;
	}

	private void meshSelected() {
		eCVegetation_Mesh mesh = listMeshes.getSelectedValue();

		PropertySync.wrap(mesh).readString(tfMeshFilePath, CD.eCVegetation_Mesh.MeshFilePath);
		tfMeshID.setText(Integer.toString(mesh.getMeshID().getIndex()));
		tfDiffuseTexture.setText(mesh.getDiffuseTexture());
		tfUseCount.setText(Integer.toString(vegetationPS.getMeshUseCount(mesh.getMeshID())));
		tfVertices.setText(Integer.toString(mesh.getPositions().size()));
		tfNormals.setText(Integer.toString(mesh.getNormals().size()));
		tfUVs.setText(Integer.toString(mesh.getUVs().size()));
		tfIndices.setText(Integer.toString(mesh.getIndices().size()));
		PropertySync.wrap(mesh).readFloat(tfWindStrength, CD.eCVegetation_Mesh.WindStrength);
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();
		buttonPanel.setAlignment(SwingConstants.LEFT);

		Action importAction = SwingUtils.createAction(I.tr("Meshes importieren"), Icons.getImageIcon(Icons.IO.IMPORT), () -> {
			File file = FileDialogWrapper.openFile(I.tr("Meshes aus anderer VegetationRoot importieren"), EditVegetationMeshesDialog.this,
					FileDialogWrapper.ARCHIVE_FILTER);
			if (file != null) {
				try {
					ArchiveFile archive = FileUtil.openArchive(file, false);
					Optional<eCEntity> vegetationRoot = archive.getEntities().firstMatch(le -> le.hasClass(CD.eCVegetation_PS.class))
							.toJavaUtil();
					if (vegetationRoot.isPresent()) {
						Collection<eCVegetation_Mesh> meshes = vegetationRoot.get().<eCVegetation_PS>getClass(CD.eCVegetation_PS.class)
								.getMeshClasses();
						ListSelectDialog<eCVegetation_Mesh> dialog = new ListSelectDialog<>(EditVegetationMeshesDialog.this,
								I.tr("Meshes aus anderer VegetationRoot importieren"), AbstractSelectDialog.SELECTION_MULTIPLE, meshes);

						if (dialog.openAndWasSuccessful()) {
							List<eCVegetation_Mesh> selectedMeshes = dialog.getSelectedEntries();
							for (eCVegetation_Mesh mesh : selectedMeshes) {
								vegetationPS.addMeshClass(mesh);
							}
							eventList.addAll(selectedMeshes);
							ctx.fileChanged();
						}
					} else {
						TaskDialogs.inform(EditVegetationMeshesDialog.this, "",
								I.trf("'{0}' enthält keine VegetationRoot.", file.getName()));
					}
				} catch (Exception ex) {
					TaskDialogs.showException(ex);
				}
			}
		});

		addButton(buttonPanel, importAction, ButtonPanel.OTHER_BUTTON);

		setDefaultCancelAction(SwingUtils.createAction(this::dispose));

		return buttonPanel;
	}
}
