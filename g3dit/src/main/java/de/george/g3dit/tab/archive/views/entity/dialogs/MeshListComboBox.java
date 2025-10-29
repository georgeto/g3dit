package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_Mesh.eSVegetationMeshID;
import de.george.lrentnode.classes.eCVegetation_PS;

public class MeshListComboBox {
	private JComboBox<MeshEntry> cbMesh = new JComboBox<>();;

	private eCVegetation_PS vegetationPS;
	private eSVegetationMeshID lastSelectedMeshID = null;

	public MeshListComboBox(eCVegetation_PS vegetationPS, eSVegetationMeshID lastSelectedMeshID) {
		this.vegetationPS = vegetationPS;
		this.lastSelectedMeshID = lastSelectedMeshID;
		loadMeshList();
	}

	private void loadMeshList() {
		List<MeshEntry> meshEntries = new ArrayList<>();
		MeshEntry lastSelectedEntry = null;
		for (eCVegetation_Mesh mesh : vegetationPS.getMeshClasses()) {
			MeshEntry meshEntry = new MeshEntry(mesh.getName(), mesh.getMeshID());
			meshEntries.add(meshEntry);

			if (mesh.getMeshID().equals(lastSelectedMeshID)) {
				lastSelectedEntry = meshEntry;
			}
		}
		SortedList<MeshEntry> sortedMeshList = new SortedList<>(GlazedLists.eventList(meshEntries), Comparator.comparing(e -> e.mesh));
		cbMesh.setModel(GlazedListsSwing.eventComboBoxModel(sortedMeshList));

		if (lastSelectedEntry != null) {
			cbMesh.setSelectedItem(lastSelectedEntry);
		} else if (!sortedMeshList.isEmpty()) {
			cbMesh.setSelectedIndex(0);
		}
	}

	public static class MeshEntry {
		private String mesh;
		private eSVegetationMeshID meshID;

		public MeshEntry(String mesh, eSVegetationMeshID meshID) {
			this.mesh = mesh;
			this.meshID = meshID;
		}

		public String getMesh() {
			return mesh;
		}

		public eSVegetationMeshID getMeshID() {
			return meshID;
		}

		@Override
		public String toString() {
			return mesh;
		}
	}

	public JComboBox<MeshEntry> getComboBox() {
		return cbMesh;
	}

	public eSVegetationMeshID getSelectedMeshID() {
		return ((MeshEntry) cbMesh.getSelectedItem()).getMeshID();
	}
}
