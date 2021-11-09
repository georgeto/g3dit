package de.george.g3dit.scripts;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;

public class ScriptDumpNavAreas implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Export NavZones and NavPaths");
	}

	@Override
	public String getDescription() {
		return I.tr("Exports NavZones and NavPaths to a text file.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveFile = FileDialogWrapper.saveFile(I.tr("Save NavZones and NavPaths in text form"), "NavAreas.txt",
				env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
		if (saveFile == null) {
			return false;
		}

		List<NavZone> navZones = new ArrayList<>();
		List<NavPath> navPaths = new ArrayList<>();
		ArchiveFileIterator iter = env.getFileManager().worldFilesIterator();
		while (iter.hasNext()) {
			ArchiveFile aFile = iter.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.gCNavZone_PS.class)) {
					navZones.add(NavZone.fromArchiveEntity(entity));
				} else if (entity.hasClass(CD.gCNavPath_PS.class)) {
					navPaths.add(NavPath.fromArchiveEntity(entity));
				}
			}
		}

		try (PrintWriter writer = new PrintWriter(Files.newWriter(saveFile, StandardCharsets.UTF_8))) {
			writer.println("NavZone Anzahl: " + navZones.size());
			for (int i = 0; i < navZones.size(); i++) {
				writer.println("NavZone " + i);
				NavZone navZone = navZones.get(i);
				writer.println("Guid: " + navZone.getGuid());
				writer.println("RadiusOffset: " + navZone.getRadiusOffset());
				writer.println("Radius: " + navZone.getRadius());
				writer.println("CCW: " + navZone.isCcw());
				writer.println("Stick Anzahl: " + navZone.getPointCount());
				navZone.getPoints().forEach(writer::println);

			}

			writer.println();
			writer.println("==========================================");

			writer.println("NavPath Anzahl: " + navPaths.size());
			for (int i = 0; i < navPaths.size(); i++) {
				writer.println("NavPathPath " + i);
				NavPath navPath = navPaths.get(i);
				writer.println("Guid: " + navPath.guid);
				writer.println("ZoneA: " + navPath.zoneAGuid);
				writer.println("ZoneAIntersectionCenter: " + navPath.zoneAIntersection.zoneIntersectionCenter);
				writer.println("ZoneAIntersectionMargin1: " + navPath.zoneAIntersection.zoneIntersectionMargin1);
				writer.println("ZoneAIntersectionMargin2: " + navPath.zoneAIntersection.zoneIntersectionMargin2);
				writer.println("ZoneB: " + navPath.zoneBGuid);
				writer.println("ZoneBIntersectionCenter: " + navPath.zoneBIntersection.zoneIntersectionCenter);
				writer.println("ZoneBIntersectionMargin1: " + navPath.zoneBIntersection.zoneIntersectionMargin1);
				writer.println("ZoneBIntersectionMargin2: " + navPath.zoneBIntersection.zoneIntersectionMargin2);
				writer.println("Point Anzahl: " + navPath.getPoints().size());
				navPath.getWorldPoints().forEach(writer::println);
				writer.println("PointRadius Anzahl: " + navPath.getRadius().size());
				navPath.getRadius().forEach(writer::println);
			}

			return true;
		} catch (Exception e) {
			env.log(e.getMessage());
			return false;
		}
	}
}
