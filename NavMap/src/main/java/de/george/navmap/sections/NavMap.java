package de.george.navmap.sections;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.teamunify.i18n.I;
import com.vividsolutions.jts.geom.Geometry;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.GenomeFile;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPath.ZonePathIntersection;
import de.george.navmap.data.NavPathLink;
import de.george.navmap.data.NavZone;
import de.george.navmap.data.NegCircle;
import de.george.navmap.data.NegZone;
import de.george.navmap.data.PrefPath;
import de.george.navmap.sections.Section3b2.NavZoneWithObjects;
import de.george.navmap.sections.Section3d.Section3dEntry;
import de.george.navmap.util.NavCalc.NavArea;
import de.george.navmap.util.UnionFind;
import one.util.streamex.StreamEx;

public class NavMap extends GenomeFile {
	private static final Logger logger = LoggerFactory.getLogger(NavMap.class);

	private static final byte[] IDENTIFIER = Misc.asByte("4745332D4E41562D4D4150");

	// Used in gCInteraction_PS::GetZoneEntity(). The difference between interact object that has
	// this versus null area assigned or no entry at all is, that for the OUT_OF_NAV_AREA_ID does
	// not even try a costly gCNavigationMap::GetZone() lookup. Whereas for the latter two it tries
	// to dynamically locate a nav area.
	public static final String OUT_OF_NAV_AREA_ID = "A3D9D3161307D749B56613FEB478580100000000";

	public static final String INVALID_ZONE_ID = "0000000000000000000000000000000000000000";

	/**
	 * Gruppierung von NavZones + NavPaths
	 */
	protected Section1 sec1;
	/**
	 * Auflistung aller NegZones
	 */
	protected Section2 sec2;
	/**
	 * Auflistung aller NegCircles
	 */
	protected Section3a sec3a;
	/**
	 * Auflistung aller PrefPaths
	 */
	protected Section3b1 sec3b1;
	/**
	 * Gruppierung der NavObjekte aus 2,3a,3b1 (Zuordnung zu NavZones)
	 */
	protected Section3b2 sec3b2;
	/**
	 * NavObjekte aus 3a) werden untereinander gruppiert (welche NegCircles schneiden sich?)
	 */
	protected Section3c sec3c;
	/**
	 * Aufzählung aller NavPaths
	 */
	protected Section3d sec3d;
	/**
	 * Verknüpfung von Interaktionsgegenständen mit der umliegenden NavZone
	 */
	protected Section3e sec3e;
	/**
	 * Indizierung für Sektion 3h) (NavZones + NavPaths)
	 */
	protected Section3fg sec3fg;
	/**
	 * Verknüpfung von NavZones und NavPaths mit Hilfe der Indizees aus 3f)
	 */
	protected Section3h sec3h;

	protected Section4 sec4;

	private boolean changed;

	private boolean invalid;

	public NavMap(G3FileReaderEx reader) throws IOException {
		super();
		read(reader);
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		if (!Arrays.equals(reader.readByteArray(IDENTIFIER.length), IDENTIFIER)) {
			throw new IOException("'" + reader.getFileName() + "' is not a valid NavigationMap.");
		}

		if (reader.readUnsignedInt() != 3 || reader.readUnsignedInt() > 0) {
			throw new IOException("Unsupported NavigationMap version.");
		}

		sec1 = reader.read(Section1.class);
		sec2 = reader.read(Section2.class);
		sec3a = reader.read(Section3a.class);
		sec3b1 = reader.read(Section3b1.class);
		sec3b2 = reader.read(Section3b2.class);
		sec3c = reader.read(Section3c.class);
		sec3d = reader.read(Section3d.class);
		sec3e = reader.read(Section3e.class);
		sec3fg = reader.read(Section3fg.class);
		sec3h = reader.read(Section3h.class);
		sec4 = reader.read(Section4.class);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) {
		writer.write(IDENTIFIER).writeUnsignedInt(3).writeUnsignedInt(0);

		writer.write(sec1);
		writer.write(sec2);
		writer.write(sec3a);
		writer.write(sec3b1);
		writer.write(sec3b2);
		writer.write(sec3c);
		writer.write(sec3d);
		writer.write(sec3e);
		writer.write(sec3fg);
		writer.write(sec3h);
		writer.write(sec4);

		changed = false;
	}

	private static final String SEPEREATOR = "\n\n==========================================\n";

	public void saveText(Path file) throws IOException {
		StringBuilder builder = new StringBuilder();
		sec1.writeText(builder);
		sec2.writeText(builder.append(SEPEREATOR));
		sec3a.writeText(builder.append(SEPEREATOR));
		sec3b1.writeText(builder.append(SEPEREATOR));
		sec3b2.writeText(builder.append(SEPEREATOR));
		sec3c.writeText(builder.append(SEPEREATOR));
		sec3d.writeText(builder.append(SEPEREATOR));
		sec3e.writeText(builder.append(SEPEREATOR));
		sec3fg.writeText(builder.append(SEPEREATOR));
		sec3h.writeText(builder.append(SEPEREATOR));
		sec4.writeText(builder.append(SEPEREATOR));

		IOUtils.writeTextFile(builder, file, StandardCharsets.UTF_8);
	}

	public Optional<List<eCPropertySetProxy>> getCell(bCVector position) {
		return sec1.getCell(position);
	}

	public StreamEx<NegZone> getNegZones() {
		return StreamEx.of(sec2.getNegZones()).map(NegZone::clone);
	}

	public Optional<NegZone> getNegZone(String guid) {
		return Optional.ofNullable(sec2.getNegZone(guid)).map(NegZone::clone);
	}

	public boolean hasNegZone(String guid) {
		return sec2.getNegZone(guid) != null;
	}

	public StreamEx<NegZone> getNegZonesForZone(String guid) {
		NavZoneWithObjects zone = sec3b2.getEntryByGuid(guid);
		if (zone != null) {
			return StreamEx.of(zone.iNegZones).map(n -> sec2.getNegZone(n).clone());
		} else {
			return StreamEx.empty();
		}
	}

	public StreamEx<NegCircle> getNegCircles() {
		return StreamEx.of(sec3a.getNegCircles()).map(NegCircle::clone);
	}

	public Optional<NegCircle> getNegCircle(String guid) {
		return Optional.ofNullable(sec3a.getNegCircle(guid)).map(NegCircle::clone);
	}

	public Optional<Geometry> getNegCircleConvexHull(String guid) {
		return Optional.ofNullable(sec3a.getNegCircle(guid)).map(NegCircle::getConvexHull);
	}

	public StreamEx<NegCircle> getNegCirclesForZone(String guid) {
		NavZoneWithObjects zone = sec3b2.getEntryByGuid(guid);
		if (zone != null) {
			return StreamEx.of(zone.iNegCircles).map(n -> sec3a.getNegCircle(n).clone());
		} else {
			return StreamEx.empty();
		}
	}

	public boolean hasNegCircle(String guid) {
		return sec3a.getNegCircle(guid) != null;
	}

	public StreamEx<PrefPath> getPrefPaths() {
		return StreamEx.of(sec3b1.prefPaths).map(PrefPath::clone);
	}

	public Optional<PrefPath> getPrefPath(String virtualGuid) {
		return Optional.ofNullable(sec3b1.getPrefPath(virtualGuid));
	}

	public Optional<Geometry> getPrefPathPolygon(String virtualGuid) {
		return getPrefPath(virtualGuid).map(PrefPath::getPolygon);
	}

	public StreamEx<PrefPath> getPrefPathsForZone(String guid) {
		NavZoneWithObjects zone = sec3b2.getEntryByGuid(guid);
		if (zone != null) {
			return StreamEx.of(zone.iPrefPaths).map(n -> sec3b1.prefPaths.get(n).clone());
		} else {
			return StreamEx.empty();
		}
	}

	public boolean hasNavZone(String guid) {
		return sec3b2.getEntryByGuid(guid) != null;
	}

	public boolean hasNavZoneBoundaryChanged(NavZone navZone) {
		return sec1.hasNavZoneBoundaryChanged(navZone);
	}

	public StreamEx<String> getNavZones() {
		return StreamEx.of(sec3b2.navZones).map(n -> n.zoneGuid);
	}

	public boolean hasNavPathBoundaryChanged(NavPath navPath) {
		return sec1.hasNavPathBoundaryChanged(navPath);
	}

	public StreamEx<String> getNavPaths() {
		return StreamEx.of(sec3d.navPaths).map(e -> e.guid);
	}

	/**
	 * @return NavPath ohne Sticks und WorldMatrix
	 */
	public Optional<NavPath> getNavPath(String guid) {
		Section3dEntry entry3d = sec3d.getNavPath(guid);
		if (entry3d == null) {
			return Optional.empty();
		}

		Pair<NavPathLink, NavPathLink> linkPair = sec3h.getNavPathLinkPair(guid, true);

		NavPath navPath = new NavPath(guid, null, null, null);

		navPath.zoneAGuid = linkPair.el0() != null ? linkPair.el0().zoneGuid : null;
		navPath.zoneAIntersection = new ZonePathIntersection();
		navPath.zoneAIntersection.zoneIntersectionCenter = entry3d.zoneACenter.clone();
		navPath.zoneAIntersection.zoneIntersectionMargin1 = entry3d.zoneAMargin1.clone();
		navPath.zoneAIntersection.zoneIntersectionMargin2 = entry3d.zoneAMargin2.clone();

		navPath.zoneBGuid = linkPair.el1() != null ? linkPair.el1().zoneGuid : null;
		navPath.zoneBIntersection = new ZonePathIntersection();
		navPath.zoneBIntersection.zoneIntersectionCenter = entry3d.zoneBCenter.clone();
		navPath.zoneBIntersection.zoneIntersectionMargin1 = entry3d.zoneBMargin1.clone();
		navPath.zoneBIntersection.zoneIntersectionMargin2 = entry3d.zoneBMargin2.clone();

		return Optional.of(navPath);
	}

	public Optional<Pair<bCVector, bCVector>> getNavPathIntersections(String guid) {
		NavObject path = sec3fg.getPath(guid);
		if (path == null) {
			return Optional.empty();
		}

		return Optional.of(
				Pair.of(sec3h.getNavPathLink(path.indexes.get(0)).intersection, sec3h.getNavPathLink(path.indexes.get(1)).intersection));
	}

	public StreamEx<String> getNavPathsForZone(String navZone) {
		return StreamEx.of(sec3fg.getZone(navZone).indexes).map(sec3h::getNavPathLink).map(l -> l.pathGuid);
	}

	public boolean hasNavPath(String guid) {
		return sec3d.getNavPath(guid) != null;
	}

	public Map<String, Optional<NavArea>> getInteractables() {
		return sec3e.interactables.stream().filter(a -> a.interactable.getGuid() != null)
				.collect(Collectors.toMap(a -> a.interactable.getGuid(), a -> NavArea.fromPropertySetProxy(a.area), (v1, v2) -> {
					if (!v1.equals(v2)) {
						throw new IllegalStateException("Interactable with two different areas " + v1 + " vs. " + v2);
					}
					return v1;
				}));
	}

	private void modify(Runnable work) {
		modify(null, v -> work.run());
	}

	private <T> void modify(T data, Consumer<T> work) {
		if (isInvalid()) {
			throw new IllegalStateException(I.tr("NavMap is faulty, further editing is not possible."));
		}

		changed = true;
		try {
			work.accept(data);
		} catch (Exception e) {
			invalid = true;
			throw e;
		}
	}

	public void addNavZone(NavZone navZone) {
		if (hasNavZone(navZone.getGuid())) {
			throw new IllegalArgumentException(I.trf("NavMap already contains NavZone with the Guid ''{0}''.", navZone.getGuid()));
		}

		modify(navZone.clone(), nz -> {
			sec1.addNavZone(nz);
			sec3b2.addNavZone(nz.getGuid(), getNextFreeClusterIndex(getUsedClusters()));
			sec3fg.addNavZone(nz.getGuid());
		});
	}

	private void validateNavPathDependencies(NavPath navPath) {
		if (navPath.zoneAGuid != null && !hasNavZone(navPath.zoneAGuid)) {
			throw new IllegalArgumentException(
					I.tr("ZoneA of the NavPath not included in the NavMap.\nPlease insert this NavZone first."));
		}

		if (navPath.zoneBGuid != null && !hasNavZone(navPath.zoneBGuid)) {
			throw new IllegalArgumentException(
					I.tr("ZoneB of the NavPath not included in the NavMap.\nPlease insert this NavZone first."));
		}
	}

	public void addNavPath(NavPath navPath) {
		if (hasNavPath(navPath.guid)) {
			throw new IllegalArgumentException(I.trf("NavMap already contains NavPath with the Guid ''{0}''.", navPath.guid));
		}
		validateNavPathDependencies(navPath);

		modify(navPath.clone(), np -> {
			sec1.addNavPath(np);

			// Eintragen des Pfades in Sektion 3d
			sec3d.addNavPath(np, getNextFreeClusterIndex(getUsedClusters()));

			// Eintragen in Sektion 3h
			sec3h.addNavPath(np);

			// Pfad Eintragen in Sektion 3fg
			sec3fg.addNavPath(np, this);

			// Cluster aktualisieren
			rebuildClusters();

			// Einträge aus 3h auf 4 anwenden
			sec4.addNavPath(np, this);
		});
	}

	/**
	 * Updatet die Position der NavZone.
	 *
	 * @param navZone
	 */
	public void updateNavZone(NavZone navZone) {
		if (!hasNavZone(navZone.getGuid())) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NavZone with the Guid ''{0}''.", navZone.getGuid()));
		}

		modify(navZone.clone(), nz -> {
			// Eintrag in Sektion 1
			sec1.updateNavZone(nz);
		});
	}

	/**
	 * Updatet die Position, Schnittpunkte und Zonen des NavPaths {@code navPath}.
	 *
	 * @param navPath
	 */
	public void updateNavPath(NavPath navPath) {
		if (!hasNavPath(navPath.guid)) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NavPath with the Guid ''{0}''.", navPath.guid));
		}
		validateNavPathDependencies(navPath);

		modify(navPath.clone(), np -> {
			// Eintrag in Sektion 1
			sec1.updateNavPath(np);

			// Eintragen des Pfades in Sektion 3d
			sec3d.updateNavPath(np);

			// Sektion 3fg und 3h updaten
			sec3h.updateNavPath(np, this);

			// Cluster aktualisieren
			rebuildClusters();

			// Sektion 4 updaten
			sec4.updateNavPath(np, this);
		});
	}

	/**
	 * Löscht eine NavZone. Enthaltene NegCircles und Interaktionsgegenstände bleiben unangetastet.
	 *
	 * @param navZone
	 */
	public void removeNavZone(String navZone) {
		if (!hasNavZone(navZone)) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NavZone with the Guid ''{0}''.", navZone));
		}

		modify(() -> {
			NavObject zoneObject = sec3fg.removeNavZone(navZone);

			sec3h.removeNavZone(zoneObject);

			sec4.removeNavZone(zoneObject);

			sec3b2.removeNavZone(navZone, this);

			sec1.remove(navZone);

			sec3e.removeNavZone(navZone);
		});
	}

	public void removeNavPath(String navPath) {
		if (!hasNavPath(navPath)) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NavPath with the Guid ''{0}''.", navPath));
		}

		modify(() -> {
			sec1.remove(navPath);

			// Eintragen des Pfades in Sektion 3d
			sec3d.removeNavPath(navPath);

			// Einträge aus 3h auf 4 anwenden
			sec4.removeNavPath(navPath, this);

			// Eintragen in Sektion 3h
			sec3h.removeNavPath(navPath, this);

			// Pfad Eintragen in Sektion 3fg
			sec3fg.removeNavPath(navPath);

			// Cluster aktualisieren
			rebuildClusters();

			sec3e.removeNavPath(navPath);
		});
	}

	/**
	 * Fügt NegZone zur NavMap hinzu.
	 *
	 * @param negZone NegZone mit gültiger Guid, alle anderen Eigenschaften können leer sein.
	 */
	public void addNegZone(NegZone negZone) {
		if (hasNegZone(negZone.getGuid())) {
			throw new IllegalArgumentException(I.trf("NavMap already contains NegZone with the Guid ''{0}''.", negZone.getGuid()));
		}

		modify(negZone.clone(), nz -> {
			sec2.addNegZone(nz);

			if (nz.getZoneGuid() != null) {
				NavZoneWithObjects newNavZone = sec3b2.getEntryByGuid(nz.getZoneGuid());
				if (newNavZone != null) {
					newNavZone.iNegZones.add(sec2.getNegZoneIndex(nz.getGuid()));
				}
			}
		});
	}

	/**
	 * Aktualisiert eine NegZone in der NavMap.
	 *
	 * @param negZone NegZone mit gültiger Guid, alle anderen Eigenschaften können leer sein.
	 */
	public void updateNegZone(NegZone negZone) {
		int negZoneIndex = sec2.getNegZoneIndex(negZone.getGuid());
		if (negZoneIndex == -1) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NegZone with the Guid ''{0}''.", negZone.getGuid()));
		}

		modify(negZone.clone(), nz -> {
			NegZone oldNegZone = sec2.getNegZone(negZoneIndex);
			if (oldNegZone.getZoneGuid() != null && !oldNegZone.getZoneGuid().equals(nz.getZoneGuid())) {
				NavZoneWithObjects oldNavZone = sec3b2.getEntryByGuid(oldNegZone.getZoneGuid());
				if (oldNavZone != null) {
					oldNavZone.iNegZones.remove(Integer.valueOf(negZoneIndex));
				}
			}

			if (nz.getZoneGuid() != null && !nz.getZoneGuid().equals(oldNegZone.getZoneGuid())) {
				NavZoneWithObjects newNavZone = sec3b2.getEntryByGuid(nz.getZoneGuid());
				if (newNavZone != null) {
					newNavZone.iNegZones.add(negZoneIndex);
				}
			}

			sec2.updateNegZone(negZoneIndex, nz);
		});
	}

	/**
	 * Entfernt eine NegZone aus der NavMap.
	 *
	 * @param negZone Guid der NegZone
	 */
	public void removeNegZone(String negZone) {
		NegZone zone = sec2.getNegZone(negZone);
		if (zone == null) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NegZone with the Guid ''{0}''.", negZone));
		}

		modify(() -> {
			int zoneIndex = sec2.getNegZoneIndex(zone.getGuid());
			for (NavZoneWithObjects navZone : sec3b2.navZones) {
				Misc.removeRangeAndDisplace(navZone.iNegZones, zoneIndex, 1);
			}

			sec2.removeNegZone(zoneIndex);
		});

	}

	private void validateNegCircleDependencies(NegCircle circle) {
		for (String zoneGuid : circle.zoneGuids) {
			if (!hasNavZone(zoneGuid) && !hasNavPath(zoneGuid)) {
				throw new IllegalArgumentException(I.trf(
						"The NavZone (''{0}'') in which the NegCircle is located is not included in the NavMap.\nPlease insert this NavZone first.",
						zoneGuid));
			}
		}
	}

	public void addNegCircle(NegCircle circle) {
		if (hasNegCircle(circle.circleGuid)) {
			throw new IllegalArgumentException(I.trf("NavMap already contains NegCircle with the Guid ''{0}''.", circle.circleGuid));
		}

		validateNegCircleDependencies(circle);

		modify(circle.clone(), negCircle -> {
			// NegCircle in Liste aufnehmen
			int circleIndex = sec3a.addNegCircle(negCircle);

			sec3b2.addNegCircle(negCircle, circleIndex);

			sec3c.addNegCircle(negCircle, circleIndex, this);
		});
	}

	public void updateNegCircle(NegCircle circle) {
		if (!hasNegCircle(circle.circleGuid)) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NegCircle with the Guid ''{0}''.", circle.circleGuid));
		}

		validateNegCircleDependencies(circle);

		modify(circle.clone(), negCircle -> {
			NegCircle oldNegCircle = sec3a.getNegCircle(circle.circleGuid);

			// NegCircle in Liste aufnehmen
			int circleIndex = sec3a.updateNegCircle(negCircle);

			sec3b2.removeNegCircle(oldNegCircle, circleIndex, false);
			sec3b2.addNegCircle(negCircle, circleIndex);

			sec3c.removeNegCircle(circleIndex, false);
			sec3c.addNegCircle(negCircle, circleIndex, this);
		});
	}

	public void removeNegCircle(String circleGuid) throws IllegalStateException {
		if (!hasNegCircle(circleGuid)) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a NegCircle with the Guid ''{0}''.", circleGuid));
		}

		modify(() -> {
			int circleIndex = sec3a.getNegCircleIndex(circleGuid);
			sec3c.removeNegCircle(circleIndex, true);

			sec3b2.removeNegCircle(sec3a.getNegCircle(circleIndex), circleIndex, true);

			sec3a.removeNegCircle(circleIndex);
		});
	}

	/**
	 * Fügt PrefPath zur NavMap hinzu.
	 *
	 * @param prefPath PrefPath, alle Eigenschaften können leer sein.
	 */
	public void addPrefPath(PrefPath prefPath) {
		if (sec3b1.getPrefPathIndex(prefPath.getVirtualGuid()) != -1) {
			throw new IllegalArgumentException(
					I.trf("NavMap already contains PrefPath with the virtual Guid ''{0}''.", prefPath.getVirtualGuid()));
		}

		modify(prefPath.clone(), pp -> {
			sec3b1.prefPaths.add(pp);

			if (pp.getZoneGuid() != null) {
				NavZoneWithObjects newNavZone = sec3b2.getEntryByGuid(pp.getZoneGuid());
				if (newNavZone != null) {
					newNavZone.iPrefPaths.add(sec3b1.getPrefPathIndex(pp.getVirtualGuid()));
				}
			}
		});
	}

	/**
	 * Aktualisiert eine PrefPath in der NavMap.
	 *
	 * @param prefPath PrefPath mit gültiger Guid, alle anderen Eigenschaften können leer sein.
	 */
	public void updatePrefPath(PrefPath prefPath) {
		int prefPathIndex = sec3b1.getPrefPathIndex(prefPath.getVirtualGuid());
		if (prefPathIndex == -1) {
			throw new IllegalArgumentException(
					I.trf("NavMap does not contain a PrefPath with the virtual Guid ''{0}''.", prefPath.getVirtualGuid()));
		}

		modify(prefPath.clone(), pp -> {
			PrefPath oldPrefPath = sec3b1.prefPaths.get(prefPathIndex);
			if (oldPrefPath.getZoneGuid() != null && !oldPrefPath.getZoneGuid().equals(pp.getZoneGuid())) {
				NavZoneWithObjects oldNavZone = sec3b2.getEntryByGuid(oldPrefPath.getZoneGuid());
				if (oldNavZone != null) {
					oldNavZone.iPrefPaths.remove(Integer.valueOf(prefPathIndex));
				}
			}

			if (pp.getZoneGuid() != null && !pp.getZoneGuid().equals(oldPrefPath.getZoneGuid())) {
				NavZoneWithObjects newNavZone = sec3b2.getEntryByGuid(pp.getZoneGuid());
				if (newNavZone != null) {
					newNavZone.iPrefPaths.add(prefPathIndex);
				}
			}

			sec3b1.prefPaths.set(prefPathIndex, pp);
		});
	}

	/**
	 * Entfernt eine PrefPath aus der NavMap.
	 *
	 * @param prefPath Guid des PrefPath
	 */
	public void removePrefPath(String prefPath) {
		int pathIndex = sec3b1.getPrefPathIndex(prefPath);
		if (pathIndex == -1) {
			throw new IllegalArgumentException(I.trf("NavMap does not contain a PrefPath with the virtual Guid ''{0}''.", prefPath));
		}

		modify(() -> {
			for (NavZoneWithObjects navZone : sec3b2.navZones) {
				Misc.removeRangeAndDisplace(navZone.iPrefPaths, pathIndex, 1);
			}

			sec3b1.prefPaths.remove(pathIndex);
		});
	}

	private void validateInteractableDependencies(NavArea area) {
		if (area.isNavPath) {
			if (!hasNavPath(area.areaId)) {
				throw new IllegalArgumentException(I.tr("NavPath of the Interactable is not included in the NavMap."));
			}
		} else if (!hasNavZone(area.areaId) && !OUT_OF_NAV_AREA_ID.equals(area.areaId)) {
			throw new IllegalArgumentException(I.tr("NavZone of the Interactable is not included in the NavMap."));
		}
	}

	public void addInteractable(String interactable, Optional<NavArea> area) {
		area.ifPresent(this::validateInteractableDependencies);
		modify(() -> sec3e.addInteractable(interactable, area));
	}

	public void updateInteractable(String interactable, Optional<NavArea> area) {
		area.ifPresent(this::validateInteractableDependencies);
		modify(() -> sec3e.updateInteractable(interactable, area));
	}

	public void removeInteractable(String interactable) {
		modify(() -> sec3e.removeInteractable(interactable));
	}

	public float calcDistance(bCVector start, String startZone, bCVector end, String endZone, List<String> traversedNavPaths) {
		float distance = 0.0f;
		String currentZone = startZone;
		bCVector currentPosition = start;

		for (String navPath : traversedNavPaths) {
			Pair<Integer, Integer> linkPair = sec3h.getNavPathLinkIndexPair(navPath, false);
			NavPathLink link0 = sec3h.getNavPathLink(linkPair.el0());
			NavPathLink link1 = sec3h.getNavPathLink(linkPair.el1());

			if (link0.zoneGuid.equals(link1.zoneGuid)) {
				throw new IllegalArgumentException("NavPath that connects the same zone is not supported.");
			}

			NavPathLink currentLink, nextLink;
			int currentIndex, nextIndex;
			if (link0.zoneGuid.equals(currentZone)) {
				currentLink = link0;
				currentIndex = linkPair.el0();
				nextLink = link1;
				nextIndex = linkPair.el1();
			} else if (link1.zoneGuid.equals(currentZone)) {
				currentLink = link1;
				currentIndex = linkPair.el1();
				nextLink = link0;
				nextIndex = linkPair.el0();
			} else {
				throw new IllegalArgumentException("NavPath that connects with wrong zone in traversed path.");
			}

			distance += currentLink.intersection.getInvTranslated(currentPosition).length();
			distance += sec4.waypoints.get(currentIndex).cons.stream().filter(w -> w.backRoad && w.index == nextIndex).findFirst()
					.get().distance;
			currentZone = nextLink.zoneGuid;
			currentPosition = nextLink.intersection;
		}

		if (!endZone.equals(currentZone)) {
			throw new IllegalArgumentException("Traversed path does not end in end zone.");
		}

		distance += end.getInvTranslated(currentPosition).length();
		return distance;
	}

	/**
	 * Berechnung der ClusterIndizes (zusammenhängende Navigationsbereiche)
	 */
	private void rebuildClusters() {
		List<NavZoneWithObjects> navZones = sec3b2.navZones;
		List<Section3dEntry> navPaths = sec3d.navPaths;
		UnionFind clusters = new UnionFind(navZones.size() + navPaths.size());
		for (NavObject path : sec3fg.navPaths) {
			if (path.indexes.size() != 2) {
				continue;
			}

			int link1 = sec3b2.getEntryIndexByGuid(sec3h.getNavPathLink(path.indexes.get(0)).zoneGuid);
			int link2 = sec3b2.getEntryIndexByGuid(sec3h.getNavPathLink(path.indexes.get(1)).zoneGuid);

			if (link1 != -1 && link2 != -1) {
				clusters.union(link1, link2);
			}

			int pathIndex = sec3d.getNavPathIndex(path.guid);
			if (pathIndex != -1 && (link1 != -1 || link2 != -1)) {
				clusters.union(pathIndex + navZones.size(), link1 != -1 ? link1 : link2);
			}
		}

		Multiset<Integer> oldClusterSizes = HashMultiset.create();
		SortedSetMultimap<Integer, Integer> oldToNewClusterMapping = TreeMultimap.create();
		for (int i = 0; i < navZones.size(); i++) {
			int clusterIndex = navZones.get(i).clusterIndex;
			oldToNewClusterMapping.put(clusterIndex, clusters.find(i));
			oldClusterSizes.add(clusterIndex);
		}
		for (int i = 0; i < navPaths.size(); i++) {
			int clusterIndex = navPaths.get(i).clusterIndex;
			oldToNewClusterMapping.put(clusterIndex, clusters.find(i + navZones.size()));
			oldClusterSizes.add(clusterIndex);
		}

		BiFunction<Integer, Integer, Integer> merge = (c1, c2) -> oldClusterSizes.count(c1) > oldClusterSizes.count(c2) ? c1 : c2;
		SortedSet<Integer> usedClusters = getUsedClusters();
		Map<Integer, Integer> newToFinalClusterMapping = new HashMap<>();
		for (Integer oldCluster : oldToNewClusterMapping.keySet()) {
			SortedSet<Integer> newClusters = oldToNewClusterMapping.get(oldCluster);

			// Cluster behält den alten ClusterIndex
			if (newClusters.size() == 1) {
				newToFinalClusterMapping.merge(newClusters.first(), oldCluster, merge);
			} else {
				// Größtes Cluster ermitteln
				int bestCluster = newClusters.first();
				for (int newCluster : newClusters) {
					logger.debug(clusters.size(newCluster) + " > " + clusters.size(bestCluster));
					if (clusters.size(newCluster) > clusters.size(bestCluster)) {
						bestCluster = newCluster;
					}
				}

				for (int newCluster : newClusters) {
					// Größtes Cluster behält den alten ClusterIndex
					if (newCluster == bestCluster) {
						newToFinalClusterMapping.merge(newCluster, oldCluster, merge);
					} else {
						int freeClusterIndex = getNextFreeClusterIndex(usedClusters);
						usedClusters.add(freeClusterIndex);
						newToFinalClusterMapping.merge(newCluster, freeClusterIndex, merge);
					}
				}
			}
		}

		for (int i = 0; i < navZones.size(); i++) {
			Integer newIndex = newToFinalClusterMapping.get(clusters.find(i));
			if (navZones.get(i).clusterIndex != newIndex) {
				logger.info("Changing cluster of the NavZone {} from {} to {}", navZones.get(i).zoneGuid, navZones.get(i).clusterIndex,
						newIndex);
			}
			navZones.get(i).clusterIndex = newIndex;
		}

		for (int i = 0; i < navPaths.size(); i++) {
			Integer newIndex = newToFinalClusterMapping.get(clusters.find(i + navZones.size()));
			if (navPaths.get(i).clusterIndex != newIndex) {
				logger.info("Changing cluster of the NavPath {} from {} to {}", navPaths.get(i).guid, navPaths.get(i).clusterIndex,
						newIndex);
			}
			navPaths.get(i).clusterIndex = newIndex;
		}
	}

	private SortedSet<Integer> getUsedClusters() {
		SortedSet<Integer> usedClusters = new TreeSet<>();
		usedClusters.addAll(sec3b2.navZones.stream().map(n -> n.clusterIndex).collect(Collectors.toSet()));
		usedClusters.addAll(sec3d.navPaths.stream().map(n -> n.clusterIndex).collect(Collectors.toSet()));
		return usedClusters;
	}

	private int getNextFreeClusterIndex(SortedSet<Integer> usedIndices) {
		int prevClusterIndex = 0;
		for (Integer index : usedIndices) {
			if (index - prevClusterIndex > 1) {
				return prevClusterIndex + 1;
			}
			prevClusterIndex = index;
		}
		return prevClusterIndex + 1;
	}

	public boolean isChanged() {
		return changed;
	}

	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void save(Path file) throws IOException {
		if (isInvalid()) {
			throw new IOException(I.tr("NavMap is faulty, saving not possible."));
		}

		super.save(file);
	}

	@Override
	public void save(OutputStream out) throws IOException {
		if (isInvalid()) {
			throw new IOException(I.tr("NavMap is faulty, saving not possible."));
		}

		super.save(out);
	}
}
