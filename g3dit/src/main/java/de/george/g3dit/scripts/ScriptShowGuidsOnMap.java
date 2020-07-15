package de.george.g3dit.scripts;

import java.util.Optional;

import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.IOUtils;
import de.george.navmap.sections.NavMap;

public class ScriptShowGuidsOnMap implements IScript {

	@Override
	public String getTitle() {
		return "Objekte zu Guids auf Map anzeigen";
	}

	@Override
	public String getDescription() {
		return "Zeigt für Guids in der Zwischenablage die dazugehörigen Objekte (NavZone, NavPath, ...) auf der Karte an.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		// TODO: Show dialog
		String content = IOUtils.getClipboardContent();

		NavCache cache = Caches.nav(env.getEditorContext());
		EntityMap map = EntityMap.getInstance(env.getEditorContext());
		NavMap navMap = env.getEditorContext().getNavMapManager().getNavMap(false);

		for (String candidate : content.split("\r\n|\n|\\s+")) {
			String guid = GuidUtil.parseGuid(candidate);
			if (guid != null) {
				Optional.ofNullable(cache.getPath(guid).orElseGet(() -> navMap.getNavPath(guid).orElse(null))).ifPresent(map::addNavPath);
				cache.getZone(guid).ifPresent(map::addNavZone);
				navMap.getNegZone(guid).ifPresent(map::addNegZone);
				navMap.getNegCircle(guid).ifPresent(map::addNegCircle);
				navMap.getPrefPath(guid).ifPresent(map::addPrefPath);
			}
		}

		// TODO: Scan entities

		return true;
	}
}
