package de.george.navmap.sections;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCPoint;
import de.george.g3utils.structure.bCRect;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;

/**
 * Gruppierung von NavZones + NavPaths
 */
public class Section1 implements G3Serializable {

	public List<eCPropertySetProxy>[][] navGrid;
	public float minGridX;
	public float maxGridX;
	public float minGridZ;
	public float maxGridZ;
	public float cellSizeX;
	public float cellSizeZ;

	@Override
	@SuppressWarnings("unchecked")
	public void read(G3FileReader reader) {
		navGrid = reader.readPrefixedArray(reader1 -> {
			return reader1.readPrefixedArray(reader2 -> {
				return reader2.readPrefixedList(eCPropertySetProxy.class);
			}, List.class);
		}, List[].class);

		minGridX = reader.readFloat();
		maxGridX = reader.readFloat();
		minGridZ = reader.readFloat();
		maxGridZ = reader.readFloat();
		cellSizeX = reader.readFloat();
		cellSizeZ = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedArray(navGrid, (writer1, navGridRow) -> {
			writer1.writePrefixedArray(navGridRow, G3FileWriter::writePrefixedList);
		});

		writer.writeFloatArray(minGridX, maxGridX, minGridZ, maxGridZ, cellSizeX, cellSizeZ);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 1");
		builder.append("\nRegion Anzahl: " + navGrid.length);
		for (int i = 0; i < navGrid.length; i++) {
			builder.append("\nRegion " + i);
			List<eCPropertySetProxy>[] region = navGrid[i];
			builder.append("\nNavTable Anzahl: " + region.length);
			for (int j = 0; j < region.length; j++) {
				builder.append("\nNavTable " + j + " (" + region[j].size() + " GUIDs)");
				region[j].forEach(e -> builder.append("\n" + e.getPropertySetName() + ": " + e.getGuid()));
			}
		}
		builder.append("\nMinGridX: " + minGridX);
		builder.append("\nMaxGridX: " + maxGridX);
		builder.append("\nMinGridZ: " + minGridZ);
		builder.append("\nMaxGridZ: " + maxGridZ);
		builder.append("\nCellSizeX: " + cellSizeX);
		builder.append("\nCellSizeZ: " + cellSizeZ);

	}

	public bCRect getBoundary(String guid) {
		bCRect bounds = new bCRect();
		for (int r = 0; r < 49; r++) {
			for (int t = 0; t < 45; t++) {
				for (eCPropertySetProxy entry : navGrid[r][t]) {
					if (!entry.getGuid().contains(guid)) {
						continue;
					}
					bounds.merge(new bCPoint(r, t));
				}
			}
		}
		return bounds;
	}

	public void addEntry(int region, int table, eCPropertySetProxy entry) {
		List<eCPropertySetProxy> list = navGrid[region][table];

		if (entry.getPropertySetName().equals("gCNavZone_PS")) {
			for (int i = 0; i < list.size(); i++) {
				// Vor den ersten NavPath setzen
				if (list.get(i).getPropertySetName().equals("gCNavPath_PS")) {
					list.add(i, entry);
					return;
				}
			}
		}
		list.add(entry);
	}

	public boolean isInGrid(NavZone navZone) {
		return isInGrid(navZone.getWorldRadiusOffset(), navZone.getRadius());
	}

	public boolean isInGrid(NavPath navPath) {
		List<bCVector> worldPoints = navPath.getWorldPoints();
		for (int i = 0; i < worldPoints.size(); i++) {
			if (!isInGrid(worldPoints.get(i), navPath.getRadius().get(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean isInGrid(bCVector position, float radius) {
		return position.getX() - radius >= minGridX && position.getX() + radius <= maxGridX && position.getZ() - radius >= minGridZ
				&& position.getZ() + radius <= maxGridZ;

	}

	public boolean isInCell(int x, int z, String guid) {
		if (x < 0 || x >= navGrid.length || z < 0 || z >= navGrid[x].length) {
			return false;
		}

		return navGrid[x][z].stream().anyMatch(p -> p.getGuid().equals(guid));
	}

	public bCRect calcNavZoneBoundary(NavZone navZone) {
		return calcBoundary(navZone.getWorldRadiusOffset(), navZone.getRadius());
	}

	public bCRect calcNavPathBoundary(NavPath navPath) {
		bCRect boundary = new bCRect();
		List<bCVector> worldPoints = navPath.getWorldPoints();
		for (int i = 0; i < worldPoints.size(); i++) {
			boundary.merge(calcBoundary(worldPoints.get(i), navPath.getRadius().get(i)));
		}
		return boundary;
	}

	public bCRect calcBoundary(bCVector position, float radius) {
		int startX = (int) ((position.getX() - radius - minGridX) / cellSizeX);
		int endX = (int) ((position.getX() + radius - minGridX) / cellSizeX);
		int startZ = (int) ((position.getZ() - radius - minGridZ) / cellSizeZ);
		int endZ = (int) ((position.getZ() + radius - minGridZ) / cellSizeZ);
		return new bCRect(new bCPoint(startX, startZ), new bCPoint(endX, endZ));
	}

	public Optional<List<eCPropertySetProxy>> getCell(bCVector position) {
		int x = (int) ((position.getX() - minGridX) / cellSizeX);
		if (x < 0 || x >= navGrid.length) {
			return Optional.empty();
		}

		int z = (int) ((position.getZ() - minGridZ) / cellSizeZ);
		if (z < 0 || z >= navGrid[0].length) {
			return Optional.empty();
		}

		return Optional.of(Collections.unmodifiableList(navGrid[x][z]));
	}

	public boolean hasNavZoneBoundaryChanged(NavZone navZone) {
		return hasBoundaryChanged(calcNavZoneBoundary(navZone), navZone.getGuid());
	}

	public boolean hasNavPathBoundaryChanged(NavPath navPath) {
		return hasBoundaryChanged(calcNavPathBoundary(navPath), navPath.getGuid());
	}

	private boolean hasBoundaryChanged(bCRect boundary, String guid) {
		int startX = boundary.getLeft();
		int startZ = boundary.getTop();
		int endX = boundary.getRight();
		int endZ = boundary.getBottom();

		return !isInCell(startX, startZ, guid) || isInCell(startX - 1, startZ, guid) || isInCell(startX, startZ - 1, guid)
				|| !isInCell(endX, endZ, guid) || isInCell(endX + 1, endZ, guid) || isInCell(endX, endZ + 1, guid);
	}

	private void applyNavZoneBoundary(NavZone navZone, bCRect bounds) {
		applyBoundary(navZone.getGuid(), "gCNavZone_PS", bounds);
	}

	private void applyNavPathBoundary(NavPath navPath, bCRect bounds) {
		applyBoundary(navPath.guid, "gCNavPath_PS", bounds);
	}

	private void applyBoundary(String guid, String type, bCRect bounds) {
		for (int r = bounds.getLeft(); r <= bounds.getRight(); r++) {
			for (int t = bounds.getTop(); t <= bounds.getBottom(); t++) {
				addEntry(r, t, new eCPropertySetProxy(guid, type));
			}
		}
	}

	public void addNavZone(NavZone navZone) {
		if (!isInGrid(navZone)) {
			throw new IllegalArgumentException(
					"NavZone befindet sich außerhalb des Navigation-Grids. Die Vergrößerung des Grids wird bisher nicht unterstützt.");
		}
		applyNavZoneBoundary(navZone, calcNavZoneBoundary(navZone));
	}

	public void addNavPath(NavPath navPath) {
		if (!isInGrid(navPath)) {
			throw new IllegalArgumentException(
					"NavPath befindet sich außerhalb des Navigation-Grids. Die Vergrößerung des Grids wird bisher nicht unterstützt.");
		}
		applyNavPathBoundary(navPath, calcNavPathBoundary(navPath));
	}

	public void updateNavZone(NavZone navZone) {
		remove(navZone.getGuid());
		addNavZone(navZone);
	}

	public void updateNavPath(NavPath navPath) {
		remove(navPath.guid);
		addNavPath(navPath);
	}

	/**
	 * Alle Einträge zu dieser Guid entfernen.
	 *
	 * @param guid NavZone oder NavPath
	 */
	public void remove(String guid) {
		for (int r = 0; r < navGrid.length; r++) {
			for (int t = 0; t < navGrid[r].length; t++) {
				navGrid[r][t].removeIf(nav -> guid.equals(nav.getGuid()));
			}
		}
	}
}
