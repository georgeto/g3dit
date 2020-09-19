package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCPoint;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCRect;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.classes.eCVegetation_Mesh.eSVegetationMeshID;
import de.george.lrentnode.util.ClassUtil;

public class eCVegetation_PS extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(eCVegetation_PS.class);

	private Map<Integer, String> types;
	private SortedMap<Integer, eCVegetation_Mesh> meshClasses;
	private eCVegetation_Grid grid;
	private bCBox outerBounds;
	private boolean boundsNeedUpdate;

	public eCVegetation_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		// Types
		types = new LinkedHashMap<>(1);
		if (reader.readBool()) {
			int typeCount = reader.readInt();
			for (int i = 0; i < typeCount; i++) {
				types.put(reader.readUnsignedShort(), reader.readEntry());
			}
		}

		@SuppressWarnings("unused")
		int meshCount = reader.readInt();

		// Workaround für einige Dateien in GD, in Gothic 3 galt immer meshCount == meshIDCount.
		int meshIDCount = reader.readInt();

		meshClasses = new TreeMap<>();

		// Mesh Classes
		for (int i = 0; i < meshIDCount; i++) {
			eCVegetation_Mesh mesh = (eCVegetation_Mesh) ClassUtil.readSubClass(reader);
			meshClasses.put(mesh.getMeshID().getIndex(), mesh);
		}

		grid = reader.read(eCVegetation_Grid::new);

		outerBounds = reader.readBox();
		boundsNeedUpdate = false;
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		if (boundsNeedUpdate) {
			grid.updateBounds();
		}

		writer.writeBool(true);
		writer.writeInt(types.size());
		types.forEach((id, type) -> {
			writer.writeUnsignedShort(id);
			writer.writeEntry(type);
		});

		writer.writeInt(meshClasses.isEmpty() ? 0 : meshClasses.lastKey() + 1);
		writer.writeInt(meshClasses.size());
		for (G3Class stack : meshClasses.values()) {
			ClassUtil.writeSubClass(writer, stack);
		}

		grid.write(writer);

		writer.writeBox(outerBounds);
	}

	public void addMeshClass(eCVegetation_Mesh mesh) {
		int firstFreeIndex = 0;
		for (int index : meshClasses.keySet()) {
			// Lücke gefunden
			if (index > firstFreeIndex) {
				break;
			}

			firstFreeIndex++;
		}
		mesh.getMeshID().setIndex(firstFreeIndex);
		meshClasses.put(mesh.getMeshID().getIndex(), mesh);
	}

	public void removeMeshClass(eSVegetationMeshID meshID) {
		removeMeshClass(getMeshClass(meshID));
	}

	public void removeMeshClass(eCVegetation_Mesh mesh) {
		if (getMeshUseCount(mesh.getMeshID()) != 0) {
			throw new IllegalStateException("Mesh wird von Vegetationsobjekten benutzt, entfernen nicht möglich!");
		}

		meshClasses.remove(mesh.getMeshID().getIndex());
	}

	public Collection<eCVegetation_Mesh> getMeshClasses() {
		return meshClasses.values();
	}

	public eCVegetation_Mesh getMeshClass(eSVegetationMeshID meshID) {
		return meshClasses.get(meshID.getIndex());
	}

	public int getMeshUseCount(eSVegetationMeshID meshID) {
		int useCount = 0;
		for (eCVegetation_GridNode node : grid.getGridNodes()) {
			for (PlantRegionEntry entry : node.getEntries()) {
				if (entry.meshID.equals(meshID)) {
					useCount++;
				}
			}
		}
		return useCount;
	}

	public eCVegetation_Grid getGrid() {
		return grid;
	}

	public bCBox getBounds() {
		return outerBounds;
	}

	public class eCVegetation_Grid implements G3Serializable {
		private float nodeDimension;
		private bCRect gridDimension;
		private List<eCVegetation_GridNode> gridNodes;

		public eCVegetation_GridNode insertEntry(PlantRegionEntry entry) {
			bCPoint worldGridPosition = calcWorldGridPosition(entry.position);

			if (!gridDimension.contains(worldGridPosition)) {
				bCRect gridExtension = new bCRect(worldGridPosition,
						new bCPoint(worldGridPosition.getX() + 1, worldGridPosition.getY() + 1));
				resizeGrid(gridExtension, true);
			}

			int nodeIndex = calcNodeIndex(calcLocalGridPosition(entry.position));
			eCVegetation_GridNode node = getOrCreateNode(nodeIndex);
			node.addEntry(entry);
			return node;
		}

		public void clear() {
			while (!gridNodes.isEmpty()) {
				removeNode(gridNodes.get(0));
			}
		}

		private eCVegetation_GridNode getOrCreateNode(int nodeIndex) {
			int i = 0;
			for (i = 0; i < gridNodes.size(); i++) {
				eCVegetation_GridNode node = gridNodes.get(i);
				if (node.index == nodeIndex) {
					return node;
				}

				if (node.index > nodeIndex) {
					break;
				}
			}

			eCVegetation_GridNode newNode = new eCVegetation_GridNode(nodeIndex);
			gridNodes.add(i, newNode);
			return newNode;
		}

		private void removeNode(eCVegetation_GridNode node) {
			if (gridNodes.remove(node)) {
				boundsNeedUpdate = true;
				compactGrid();
			}
		}

		public bCPoint indexToLocalGridPosition(int index, bCRect grid) {
			if (index >= grid.getWidth() * grid.getHeight()) {
				throw new IllegalArgumentException("Index " + index + " befindet sich außerhalb des Grids " + grid);
			}

			return new bCPoint(index % grid.getWidth(), index / grid.getWidth());
		}

		public int localGridPositionToIndex(bCPoint position, bCRect grid) {
			if (!grid.contains(position.clone().translate(grid.getTopLeft()))) {
				throw new IllegalArgumentException("Position " + position + " befindet sich außerhalb des Grids " + grid);
			}

			return position.getY() * grid.getWidth() + position.getX();
		}

		public bCPoint calcWorldGridPosition(bCVector position) {
			// WorldSpace
			return new bCPoint(Math.round(position.getX() / nodeDimension), Math.round(position.getZ() / nodeDimension));
		}

		public bCPoint calcLocalGridPosition(bCVector position) {
			// WorldSpace
			bCPoint gridPosition = new bCPoint(Math.round(position.getX() / nodeDimension), Math.round(position.getZ() / nodeDimension));
			// LocalSpace
			return gridPosition.invTranslate(gridDimension.getTopLeft());
		}

		public int calcNodeIndex(bCPoint gridPosition) {
			return localGridPositionToIndex(gridPosition, gridDimension);
		}

		private void resizeGrid(bCRect gridExtension, boolean expand) {
			// GridDimension ist ungültig -> Grid enthält keine Nodes
			if (!gridDimension.isValid()) {
				if (!gridNodes.isEmpty()) {
					throw new IllegalStateException("Grid mit ungültigen Dimensionen enthält Nodes.");
				}

				gridDimension = gridExtension;
				return;
			}

			if (expand) {
				gridExtension.merge(gridDimension);
				logger.info("Grid expandiert von {} auf {}.", gridDimension, gridExtension);
			}

			// VirtualIndex der Nodes an neues Grid anpassen
			bCPoint topLeftDelta = gridExtension.getTopLeft().clone();
			topLeftDelta.invTranslate(gridDimension.getTopLeft());

			for (eCVegetation_GridNode node : gridNodes) {
				bCPoint nodePosition = indexToLocalGridPosition(node.getIndex(), gridDimension);
				if (calcNodeIndex(nodePosition) != node.getIndex()) {
					logger.warn("Calc mismatch: " + calcNodeIndex(nodePosition) + " | " + node.getIndex());
				}

				// Neuen Index berechnen
				nodePosition.invTranslate(topLeftDelta);
				node.setIndex(localGridPositionToIndex(nodePosition, gridExtension));
			}

			// Neues Grid übernehmen
			gridDimension = gridExtension;
		}

		/**
		 * Wird oft zu einer starken Verkleinerung des Grids führen, da der Gothic 3 Quellcode einen
		 * Bug in der bCRect::Invanlidate() Methode enthält: BottomRight wird mit (-1, -1)
		 * initialisiert.
		 */
		private void compactGrid() {
			// GridDimension ist ungültig -> Grid enthält keine Nodes
			if (!gridDimension.isValid()) {
				if (!gridNodes.isEmpty()) {
					throw new IllegalStateException("Grid mit ungültigen Dimensionen enthält Nodes.");
				}
				return;
			}

			bCRect compactDimension = new bCRect();
			for (eCVegetation_GridNode node : gridNodes) {
				bCPoint position = indexToLocalGridPosition(node.index, gridDimension);
				bCPoint transPosition = position.clone().translate(gridDimension.getTopLeft());
				compactDimension.merge(transPosition);
			}

			// Off-by-One korrigieren
			compactDimension.setBottomRight(compactDimension.getBottomRight().translate(new bCPoint(1, 1)));

			if (compactDimension.isValid() && !compactDimension.equal(gridDimension)) {
				logger.info("Grid verkleinert von {} auf {}.", gridDimension, compactDimension);
				resizeGrid(compactDimension, false);
			}

		}

		@Override
		public void read(G3FileReader reader) {
			if (reader.readShort() != 2) {
				reader.warn(logger, "eCVegetation_Grid hat unbekannte Version.");
			}

			nodeDimension = reader.readFloat();
			gridDimension = reader.read(bCRect.class);
			gridNodes = reader.readList(r -> r.read(eCVegetation_GridNode::new));
		}

		@Override
		public void write(G3FileWriter writer) {
			// Mysterious Section
			writer.writeUnsignedShort(2);
			writer.writeFloat(nodeDimension);
			gridDimension.write(writer);
			writer.writeList(gridNodes);
		}

		public float getNodeDimension() {
			return nodeDimension;
		}

		public bCRect getGridDimension() {
			return gridDimension;
		}

		public eCVegetation_GridNode getGridNode(int i) {
			return gridNodes.get(i);
		}

		public int getGridNodeCount() {
			return gridNodes.size();
		}

		public List<eCVegetation_GridNode> getGridNodes() {
			return gridNodes;
		}

		public void updateBounds() {
			bCBox newBounds = new bCBox();
			for (eCVegetation_GridNode node : getGrid().getGridNodes()) {
				newBounds.merge(node.getBounds());
			}
			outerBounds = newBounds;
			boundsNeedUpdate = false;
		}

		public int getEntryCount() {
			return getGridNodes().stream().map(eCVegetation_GridNode::getEntryCount).reduce(0, Integer::sum);
		}
	}

	public class eCVegetation_GridNode implements G3Serializable {

		private int index;
		private bCBox bounds;
		private List<PlantRegionEntry> entries;

		public eCVegetation_GridNode() {}

		public eCVegetation_GridNode(int index) {
			this.index = index;
			bounds = new bCBox();
			entries = new ArrayList<>();
		}

		@Override
		public void read(G3FileReader reader) {
			index = reader.readInt();
			if (reader.readShort() != 1) {
				reader.warn(logger, "eCVegetation_GridNode hat unbekannte Version.");
			}
			bounds = reader.readBox();
			entries = reader.readList(PlantRegionEntry.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(index);
			writer.write("0100");
			writer.writeBox(bounds);
			writer.writeList(entries);
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public bCBox getBounds() {
			return bounds;
		}

		public int getEntryCount() {
			return entries.size();
		}

		public PlantRegionEntry getEntry(int index) {
			return entries.get(index);
		}

		public List<PlantRegionEntry> getEntries() {
			return entries;
		}

		public void addEntry(PlantRegionEntry entry) {
			entries.add(entry);
			updateBounds();
		}

		public PlantRegionEntry removeEntry(int index) {
			PlantRegionEntry entry = entries.remove(index);
			if (entries.isEmpty()) {
				getGrid().removeNode(this);
			} else {
				updateBounds();
			}
			return entry;
		}

		public void removeEntry(PlantRegionEntry entry) {
			entries.remove(entry);
			if (entries.isEmpty()) {
				getGrid().removeNode(this);
			} else {
				updateBounds();
			}
		}

		public void updateBounds() {
			bounds.invalidate();

			for (PlantRegionEntry plant : entries) {
				bCMatrix transform = new bCMatrix();
				transform.setToRotation(plant.rotation);
				transform.modifyTranslation(plant.position);
				transform.modifyScaling(new bCVector(plant.scaleWidth, plant.scaleHeight, plant.scaleWidth));

				eCVegetation_Mesh vegMesh = getMeshClass(plant.meshID);
				bCBox newBounds = vegMesh.getBounds().clone();
				newBounds.transform(transform);

				bounds.merge(newBounds);
			}

			boundsNeedUpdate = true;
		}
	}

	public static class PlantRegionEntry implements G3Serializable {
		public eSVegetationMeshID meshID;
		public bCVector position;
		public bCQuaternion rotation;
		public float scaleWidth, scaleHeight;
		public int colorARGB; // little endian BGRA

		public PlantRegionEntry(eSVegetationMeshID meshID, bCVector position, bCQuaternion rotation, float scaleWidth, float scaleHeight,
				int colorARGB) {
			this.meshID = meshID;
			this.position = position;
			this.rotation = rotation;
			this.scaleWidth = scaleWidth;
			this.scaleHeight = scaleHeight;
			this.colorARGB = colorARGB;
		}

		@Override
		public void read(G3FileReader reader) {
			meshID = reader.read(eSVegetationMeshID.class);
			position = reader.readVector();
			rotation = reader.read(bCQuaternion.class);
			scaleWidth = reader.readFloat();
			scaleHeight = reader.readFloat();
			colorARGB = reader.readInt();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(meshID);
			writer.writeVector(position);
			writer.write(rotation);
			writer.writeFloat(scaleWidth);
			writer.writeFloat(scaleHeight);
			writer.writeInt(colorARGB);
		}
	}
}
