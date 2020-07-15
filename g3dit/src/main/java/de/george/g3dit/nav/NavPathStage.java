package de.george.g3dit.nav;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.google.common.base.Joiner;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Pair;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPath.ZonePathIntersection;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import de.george.navmap.util.NavCalc.IntersectZone;

public class NavPathStage extends NavCalcStage {

	public NavPathStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
	}

	@Override
	public String getTitle() {
		return "NavPath";
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanelNamed();

		addBackButton(mainPanel, NavCalcState.NegZone);
		addNextButton(mainPanel, NavCalcState.PrefPath);

		return mainPanel;
	}

	private final void compareNavPath(NavPath changed, NavPath base, Consumer<String> report,
			Optional<Pair<bCVector, bCVector>> baseIntersections) {
		if (!Objects.equals(changed.zoneAGuid, base.zoneAGuid)) {
			report.accept(String.format("ZoneA %s instead of %s.", changed.zoneAGuid, base.zoneAGuid));
		}

		if (!Objects.equals(changed.zoneBGuid, base.zoneBGuid)) {
			report.accept(String.format("ZoneB %s instead of %s.", changed.zoneBGuid, base.zoneBGuid));
		}

		if (changed.zoneAGuid != null) {
			if (!changed.zoneAIntersection.zoneIntersectionCenter.simliar(base.zoneAIntersection.zoneIntersectionCenter, 0.002f)) {
				report.accept("ZoneAIntersectionCenter: " + changed.zoneAIntersection.zoneIntersectionCenter + " instead of "
						+ base.zoneAIntersection.zoneIntersectionCenter);
			} else if (baseIntersections.isPresent()) {
				bCVector zoneAIntersection = baseIntersections.get().el0().getTransformed(changed.getInvertedWorldMatrix());
				if (!changed.zoneAIntersection.zoneIntersectionCenter.simliar(zoneAIntersection, 0.1f)) {
					report.accept("ZoneAIntersectionCenter: " + changed.zoneAIntersection.zoneIntersectionCenter + " instead of "
							+ zoneAIntersection);
				}
			}
			if (!changed.zoneAIntersection.zoneIntersectionMargin1.simliar(base.zoneAIntersection.zoneIntersectionMargin1, 0.002f)) {
				report.accept("ZoneAIntersectionMargin1: " + changed.zoneAIntersection.zoneIntersectionMargin1 + " instead of "
						+ base.zoneAIntersection.zoneIntersectionMargin1);
			}
			if (!changed.zoneAIntersection.zoneIntersectionMargin2.simliar(base.zoneAIntersection.zoneIntersectionMargin2, 0.002f)) {
				report.accept("ZoneAIntersectionMargin2: " + changed.zoneAIntersection.zoneIntersectionMargin2 + " instead of "
						+ base.zoneAIntersection.zoneIntersectionMargin2);
			}
		}

		if (changed.zoneBGuid != null) {
			if (!changed.zoneBIntersection.zoneIntersectionCenter.simliar(base.zoneBIntersection.zoneIntersectionCenter, 0.002f)) {
				report.accept("ZoneBIntersectionCenter: " + changed.zoneBIntersection.zoneIntersectionCenter + " instead of "
						+ base.zoneBIntersection.zoneIntersectionCenter);
			} else if (baseIntersections.isPresent()) {
				bCVector zoneBIntersection = baseIntersections.get().el1().getTransformed(changed.getInvertedWorldMatrix());
				if (!changed.zoneBIntersection.zoneIntersectionCenter.simliar(zoneBIntersection, 0.1f)) {
					report.accept("ZoneBIntersectionCenter: " + changed.zoneBIntersection.zoneIntersectionCenter + " instead of "
							+ zoneBIntersection);
				}
			}
			if (!changed.zoneBIntersection.zoneIntersectionMargin1.simliar(base.zoneBIntersection.zoneIntersectionMargin1, 0.002f)) {
				report.accept("ZoneBIntersectionMargin1: " + changed.zoneBIntersection.zoneIntersectionMargin1 + " instead of "
						+ base.zoneBIntersection.zoneIntersectionMargin1);
			}
			if (!changed.zoneBIntersection.zoneIntersectionMargin2.simliar(base.zoneBIntersection.zoneIntersectionMargin2, 0.002f)) {
				report.accept("ZoneBIntersectionMargin2: " + changed.zoneBIntersection.zoneIntersectionMargin2 + " instead of "
						+ base.zoneBIntersection.zoneIntersectionMargin2);
			}
		}
	}

	private void processNavPath(NavPath navPath) {
		if (navPath.getPoints().size() < 2) {
			addChange(new InvalidNavPath(navPath));
			return;
		}

		String zoneA = navCalc.detectZoneForNavPath(navPath, IntersectZone.ZoneA).orElse(null);
		String zoneB = navCalc.detectZoneForNavPath(navPath, IntersectZone.ZoneB).orElse(null);

		ZonePathIntersection calcZoneAIntersection = navCalc.calcZonePathIntersection(navPath.getPoints(), navPath.getRadius(),
				navPath.getWorldMatrix(), zoneA, IntersectZone.ZoneA);
		ZonePathIntersection calcZoneBIntersection = navCalc.calcZonePathIntersection(navPath.getPoints(), navPath.getRadius(),
				navPath.getWorldMatrix(), zoneB, IntersectZone.ZoneB);

		NavPath calcNavPath = navPath.clone();
		calcNavPath.zoneAGuid = zoneA;
		calcNavPath.zoneAIntersection = calcZoneAIntersection;
		calcNavPath.zoneBGuid = zoneB;
		calcNavPath.zoneBIntersection = calcZoneBIntersection;

		// Compare with NavPath in cache / world entity
		{
			List<String> details = new ArrayList<>();
			compareNavPath(calcNavPath, navPath, details::add, Optional.empty());
			if (!details.isEmpty()) {
				addChange(new ZoneIntersectionStale(calcNavPath, HtmlCreator.renderList(details)));
			}
		}
		// Compare with NavPath in NavMap
		NavPath mapPath = navMap.getNavPath(calcNavPath.guid).orElse(null);
		if (mapPath == null) {
			addChange(new NotInNavmap(calcNavPath.clone()));
			return;
		}

		List<String> details = new ArrayList<>();
		if (navMap.hasNavPathBoundaryChanged(calcNavPath)) {
			details.add("Boundary in navigation grid has changed.");
		}

		compareNavPath(calcNavPath, mapPath, details::add, navMap.getNavPathIntersections(calcNavPath.guid));

		if (!details.isEmpty()) {
			addChange(new ZoneIntersectionChanged(calcNavPath, HtmlCreator.renderList(details)));
		}

		if (zoneA == null || zoneB == null) {
			addChange(new InvalidNavPath(calcNavPath));
		}
	}

	@Override
	protected void doExecute() {
		clearChanges();

		Awaitable awaitProcess = ConcurrencyUtil.processInPartitions(this::processNavPath, new ArrayList<>(navCache.getPaths()));

		for (String navPath : navMap.getNavPaths()) {
			if (!navCache.getPath(navPath).isPresent()) {
				addChange(new OnlyInNavmap(navMap.getNavPath(navPath).get()));
			}
		}

		awaitProcess.await();
	}

	private static String formatNavPathZones(NavPath navPath) {
		return HtmlCreator.renderList(String.format("ZoneA: %s", navPath.zoneAGuid != null ? navPath.zoneAGuid : "-"),
				String.format("ZoneB: %s", navPath.zoneBGuid != null ? navPath.zoneBGuid : "-"));
	}

	private static String getInvalidNavPathMessage(NavPath navPath) {
		if (navPath.getPoints().size() < 2) {
			return String.format("Invalid NavPath with only %d points.", navPath.getPoints().size());
		}

		String nullZones = Joiner.on(" and ").skipNulls().join(navPath.zoneAGuid == null ? "ZoneA" : null,
				navPath.zoneBGuid == null ? "ZoneB" : null);
		if (!nullZones.isEmpty()) {
			return String.format("Unable to detect %s.", nullZones);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private class BaseNavPathChange extends BaseNamedChange {
		protected NavPath navPath;

		public BaseNavPathChange(NavPath navPath, Severity severity, String message, String details) {
			super(navPath.getGuid(), severity, message, details);
			this.navPath = navPath;
		}

		@Override
		public File getFile() {
			return Caches.entity(ctx).getFile(getGuid()).map(FileDescriptor::getPath).orElse(null);
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEntity(navPath.getGuid());
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addNavPath(navPath);
			EntityMap.getInstance(ctx).showArea(navPath.getWorldPoints().stream().reduce(new bCBox(), bCBox::merge, bCBox::merge));
		}

		@Override
		public void teleport() {
			IpcUtil.gotoGuid(getGuid());
		}
	}

	private class InvalidNavPath extends BaseNavPathChange {
		public InvalidNavPath(NavPath navPath) {
			super(navPath, Severity.Info, getInvalidNavPathMessage(navPath), formatNavPathZones(navPath));
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class NotInNavmap extends BaseNavPathChange {
		public NotInNavmap(NavPath navPath) {
			super(navPath, Severity.Error, "Not in NavMap.", formatNavPathZones(navPath));
		}

		@Override
		public void fix() {
			navMap.addNavPath(navPath);
			markFixed();
		}
	}

	private class OnlyInNavmap extends BaseNavPathChange {
		public OnlyInNavmap(NavPath navPath) {
			super(navPath, Severity.Warn, "Only in NavMap.", formatNavPathZones(navPath));
		}

		@Override
		public void fix() {
			navMap.removeNavPath(getGuid());
			markFixed();
		}
	}

	private class ZoneIntersectionChanged extends BaseNavPathChange {
		public ZoneIntersectionChanged(NavPath navPath, String details) {
			super(navPath, Severity.Error, "Intersections with NavZone have changed.",
					HtmlCreator.renderList(details, "", formatNavPathZones(navPath)));
		}

		@Override
		public void fix() {
			navMap.updateNavPath(navPath);
			markFixed();
		}
	}

	private class ZoneIntersectionStale extends BaseNavPathChange {
		public ZoneIntersectionStale(NavPath navPath, String details) {
			super(navPath, Severity.Info, "Intersections stored in the NavPath are stale.",
					HtmlCreator.renderList(details, "", formatNavPathZones(navPath)));
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}
}
