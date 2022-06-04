package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import de.george.g3dit.gui.components.FloatSpinner;
import de.george.g3dit.gui.components.HidingGroup;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.dialogs.AbstractSelectDialog;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.dialogs.ListSelectDialog;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.jme.asset.IntermediateMesh;
import de.george.g3dit.jme.asset.MeshUtil;
import de.george.g3dit.jme.asset.MeshUtil.IllegalMeshException;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.AssetResolver.MaterialAsset;
import de.george.g3dit.util.AssetResolver.TextureAsset;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCDateTime;
import de.george.g3utils.util.Notifier;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.util.ClassUtil;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class EditVegetationMeshesDialog extends ExtStandardDialog {

	private EditorAbstractFileTab ctx;
	private eCVegetation_PS vegetationPS;
	private Notifier notifyBoundsNeedUpdate;

	private eCVegetation_Mesh selectedMesh;

	private EventList<eCVegetation_Mesh> eventList;

	private HidingGroup hidingGroup;
	private PropertyPanel propertyPanel;

	public EditVegetationMeshesDialog(Window owner, EditorAbstractFileTab ctx, eCVegetation_PS vegetationPS,
			Notifier notifyBoundsNeedUpdate) {
		super(owner, I.tr("Edit vegetation meshes"), true);
		this.ctx = ctx;
		this.vegetationPS = vegetationPS;
		this.notifyBoundsNeedUpdate = notifyBoundsNeedUpdate;
		setType(Type.UTILITY);
		setSize(800, 500);
		setResizable(true);
	}

	@Override
	public JComponent createContentPanel() {
		propertyPanel = new PropertyPanel(ctx);
		//@foff
        propertyPanel
                .add(CD.eCVegetation_Mesh.MeshFilePath).editable(false).growx()
                .<eCVegetation_Mesh, bCString>add(CD.eCVegetation_Mesh.class, m -> new bCString(m.diffuseTexture), (m, v) -> m.diffuseTexture = v.getString(), null, bCString.class, "").name("DiffuseTexture").growx()
                .add(CD.eCVegetation_Mesh.MeshShading).horizontalStart(2).growx()
                .add(CD.eCVegetation_Mesh.DoubleSided).horizontal()
                .add(CD.eCVegetation_Mesh.WindStrength).<FloatSpinner>customize(sp -> sp.setStepSize(0.1f)).horizontalStart()
                .add(CD.eCVegetation_Mesh.MinSpacing).horizontal()
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(m.getMeshID().getIndex()), null, null, gInt.class, "").name("MeshID").editable(false)
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(vegetationPS.getMeshUseCount(m.getMeshID())), null, null, gInt.class, "").name(I.tr("Number of occurrences")).editable(false)
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(m.positions.size()), null, null, gInt.class, "").name("Vertices").editable(false).horizontalStart()
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(m.normals.size()), null, null, gInt.class, "").name("Normals").editable(false).horizontal()
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(m.uvs.size()), null, null, gInt.class, "").name("UVs").editable(false).horizontal()
                .<eCVegetation_Mesh, gInt>add(CD.eCVegetation_Mesh.class, m -> new gInt(m.indices.size()), null, null, gInt.class, "").name("Indices").editable(false).horizontal()
				.done();
        //@fon

		Action saveAction = SwingUtils.createAction(I.tr("Save"), Icons.getImageIcon(Icons.IO.SAVE),
				() -> propertyPanel.save(ClassUtil.wrapPropertySet(selectedMesh)));

		Action updateAction = SwingUtils.createAction(I.tr("Update from .xcmsh"), Icons.getImageIcon(Icons.IO.IMPORT), () -> {
			if (updateMeshFromXcmsh(selectedMesh)) {
				notifyBoundsNeedUpdate.signal();
				eventList.set(eventList.indexOf(selectedMesh), selectedMesh);
				meshSelected(selectedMesh);
			}
		});

		JButton btnSave = new JButton(saveAction);
		JButton btnUpdate = new JButton(updateAction);

		hidingGroup = new HidingGroup();
		hidingGroup.add(propertyPanel.getContent());
		hidingGroup.add(btnSave);
		hidingGroup.add(btnUpdate);

		JPanel mainPanel = new JPanel(new MigLayout("fillx"));
		mainPanel.add(propertyPanel.getContent(), "spanx, wrap");
		mainPanel.add(btnSave, "gaptop 12px");
		mainPanel.add(btnUpdate, "wrap");

		eventList = GlazedLists.eventList(vegetationPS.getMeshClasses());
		ListManageAndEdit<eCVegetation_Mesh> edit = ListManageAndEdit.create(eventList, this::importMeshes, mainPanel)
				.matcherEditor(tfFilter -> new TextComponentMatcherEditor<>(tfFilter, GlazedLists.textFilterator("Name")))
				.cellRenderer(new BeanListCellRenderer("Name")).changeMonitor(ctx).onSelect(this::meshSelected)
				.onMultiDelete(this::deleteMeshes).build();

		return edit.getContent();
	}

	private Optional<eCVegetation_Mesh> importMeshes() {
		int result = TaskDialogs.choice(ctx.getParentWindow(), I.tr("Import meshes"), "", 0,
				new CommandLink(I.tr("Import meshes from another VegetationRoot"), ""),
				new CommandLink(I.tr("Import mesh from .xcmsh"), ""));

		return switch (result) {
			case 0 -> importMeshesFromVegetationRoot();
			case 1 -> importMeshesFromXcmsh();
			default -> Optional.empty();
		};
	}

	private boolean deleteMeshes(List<eCVegetation_Mesh> meshes) {
		for (eCVegetation_Mesh mesh : meshes)
			if (vegetationPS.getMeshUseCount(mesh.getMeshID()) > 0)
				return false;

		for (eCVegetation_Mesh mesh : meshes)
			vegetationPS.removeMeshClass(mesh);
		return true;
	}

	private void meshSelected(eCVegetation_Mesh mesh) {
		selectedMesh = mesh;
		hidingGroup.setVisible(mesh != null);
		if (mesh != null) {
			propertyPanel.load(ClassUtil.wrapPropertySet(mesh));
		}
	}

	private Optional<eCVegetation_Mesh> importMeshesFromVegetationRoot() {
		File file = FileDialogWrapper.openFile(I.tr("Import meshes from another VegetationRoot"), EditVegetationMeshesDialog.this,
				FileDialogWrapper.ARCHIVE_FILTER);
		if (file == null)
			return Optional.empty();

		try {
			ArchiveFile archive = FileUtil.openArchive(file, false);
			Optional<eCEntity> vegetationRoot = archive.getEntities().firstMatch(le -> le.hasClass(CD.eCVegetation_PS.class)).toJavaUtil();
			if (vegetationRoot.isPresent()) {
				Collection<eCVegetation_Mesh> meshes = vegetationRoot.get().<eCVegetation_PS>getClass(CD.eCVegetation_PS.class)
						.getMeshClasses();
				ListSelectDialog<eCVegetation_Mesh> dialog = new ListSelectDialog<>(EditVegetationMeshesDialog.this,
						I.tr("Import meshes from another VegetationRoot"), AbstractSelectDialog.SELECTION_MULTIPLE, meshes);

				if (dialog.openAndWasSuccessful()) {
					List<eCVegetation_Mesh> selectedMeshes = dialog.getSelectedEntries();
					selectedMeshes.forEach(vegetationPS::addMeshClass);
					eventList.addAll(selectedMeshes);
					ctx.fileChanged();
				}
			} else {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "",
						I.trf("''{0}'' does not contain a VegetationRoot.", file.getName()));
			}
		} catch (Exception ex) {
			TaskDialogs.showException(ex);
		}

		return Optional.empty();
	}

	private boolean updateMeshFromXcmsh(eCVegetation_Mesh vegMesh) {
		File file = FileDialogWrapper.openFile(I.tr("Import mesh from .xcmsh"), EditVegetationMeshesDialog.this,
				FileDialogWrapper.createFilter("Mesh", "xcmsh"));
		if (file == null)
			return false;

		try {
			List<IntermediateMesh> meshes = MeshUtil.toIntermediateMesh(FileUtil.openMesh(file));
			if (meshes.isEmpty()) {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "", I.trf("''{0}'' does not contain a submesh.", file.getName()));
				return false;
			} else if (meshes.size() >= 2) {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "", I.trf("''{0}'' contains more than one submesh.", file.getName()));
				return false;
			}

			IntermediateMesh mesh = meshes.get(0);
			MaterialAsset material = AssetResolver.with(ctx).noRecurse().build().resolveMaterial(mesh.materialName, 0);
			if (!material.isFound()) {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "",
						I.trf("Material ''{0}'' could not be found, unable to resolve texture.", mesh.materialName));
				return false;
			}

			List<TextureAsset> textures = StreamEx.of(material.getTextures()).filter(t -> t.getUseType().equals("Diffuse")).toList();
			if (textures.isEmpty()) {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "",
						I.trf("Material ''{0}'' does not contain a diffuse texture.", material.getName()));
				return false;
			} else if (textures.size() >= 2) {
				TaskDialogs.inform(EditVegetationMeshesDialog.this, "",
						I.trf("Material ''{0}'' contains more than one diffuse texture.", material.getName()));
				return false;
			}

			vegMesh.setMeshFilePath(file.getName());
			vegMesh.property(CD.eCVegetation_Mesh.MinSpacing).setFloat(mesh.boundingBox.getExtent().to2D().length());
			vegMesh.timestamp = bCDateTime.fromInstant(Files.getLastModifiedTime(file.toPath()).toInstant());
			vegMesh.diffuseTexture = textures.get(0).getBaseName();
			vegMesh.bounds = mesh.boundingBox;
			vegMesh.positions = mesh.vertices;
			vegMesh.normals = mesh.normals;
			vegMesh.uvs = mesh.texCoords;
			vegMesh.indices = mesh.indices;
			return true;
		} catch (IOException | IllegalMeshException e) {
			TaskDialogs.showException(e);
			return false;
		}
	}

	private Optional<eCVegetation_Mesh> importMeshesFromXcmsh() {
		eCVegetation_Mesh vegMesh = (eCVegetation_Mesh) ClassUtil.createDefaultPropertySet(CD.eCVegetation_Mesh.class);
		if (updateMeshFromXcmsh(vegMesh)) {
			vegetationPS.addMeshClass(vegMesh);
			return Optional.of(vegMesh);
		} else
			return Optional.empty();

	}
}
