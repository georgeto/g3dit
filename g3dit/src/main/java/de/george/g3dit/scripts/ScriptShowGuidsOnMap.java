package de.george.g3dit.scripts;

import java.util.Optional;

import com.teamunify.i18n.I;

import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.navmap.sections.NavMap;

public class ScriptShowGuidsOnMap implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Objekte zu Guids auf Map anzeigen");
	}

	@Override
	public String getDescription() {
		return I.tr("Zeigt für Guids in der Zwischenablage die dazugehörigen Objekte (NavZone, NavPath, ...) auf der Karte an.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		// TODO: Show dialog
		NavCache cache = Caches.nav(env.getEditorContext());
		EntityMap map = EntityMap.getInstance(env.getEditorContext());
		NavMap navMap = env.getEditorContext().getNavMapManager().getNavMap(false);

		for (String guid : ScriptUtils.extractGuidFromClipboard()) {
			Optional.ofNullable(cache.getPath(guid).orElseGet(() -> navMap.getNavPath(guid).orElse(null))).ifPresent(map::addNavPath);
			cache.getZone(guid).ifPresent(map::addNavZone);
			navMap.getNegZone(guid).ifPresent(map::addNegZone);
			navMap.getNegCircle(guid).ifPresent(map::addNegCircle);
			navMap.getPrefPath(guid).ifPresent(map::addPrefPath);
		}

		// TODO: Scan entities

		return true;
	}
}
