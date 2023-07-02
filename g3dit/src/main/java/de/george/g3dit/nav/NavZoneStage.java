package de.george.g3dit.nav;

import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3utils.structure.bCBox;
import de.george.navmap.data.NavZone;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

public class NavZoneStage extends NavCalcStage {
	public NavZoneStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
	}

	@Override
	public String getTitle() {
		return "NavZone";
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanelNamed();

		addNextButton(mainPanel, NavCalcState.NegZone);

		return mainPanel;
	}

	@Override
	protected void doExecute() {
		clearChanges();
		for (NavZone navZone : navCache.getZones()) {
			if (!navMap.hasNavZone(navZone.getGuid())) {
				addChange(new NotInNavmap(navZone));
			} else if (navMap.hasNavZoneBoundaryChanged(navZone)) {
				addChange(new BoundaryChanged(navZone));
			}
		}

		for (String navZone : navMap.getNavZones()) {
			if (!navCache.getZone(navZone).isPresent()) {
				addChange(new OnlyInNavmap(navZone));
			}
		}
	}

	private class BaseNavZoneChange extends BaseNamedChange {
		protected NavZone navZone;

		public BaseNavZoneChange(NavZone navZone, Severity severity, String message, String details) {
			super(navZone.getGuid(), severity, message, details);
			this.navZone = navZone;
		}

		@Override
		public Path getFile() {
			return Caches.entity(ctx).getFile(getGuid()).map(FileDescriptor::getPath).orElse(null);
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEntity(navZone.getGuid());
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addNavZone(navZone);
			EntityMap.getInstance(ctx).showArea(navZone.getWorldPoints().stream().reduce(new bCBox(), bCBox::merge, bCBox::merge));
		}

		@Override
		public void teleport() {
			IpcUtil.gotoGuid(getGuid());
		}
	}

	private class NotInNavmap extends BaseNavZoneChange {
		public NotInNavmap(NavZone navZone) {
			super(navZone, Severity.Error, I.tr("Not in NavMap."), null);
		}

		@Override
		public void fix() {
			navMap.addNavZone(navCache.getZone(getGuid()).get());
			markFixed();
		}
	}

	private String getNavZoneConnectionDetails(String navZone) {
		return HtmlCreator.renderList(I.tr("Related nav objects"), "  NavPaths: " + navMap.getNavPathsForZone(navZone).count(),
				"  NegZones: " + navMap.getNegZonesForZone(navZone).count(),
				"  NegCircles: " + navMap.getNegCirclesForZone(navZone).count(),
				"  PrefPaths: " + navMap.getPrefPathsForZone(navZone).count());
	}

	private class OnlyInNavmap extends BaseNamedChange {
		public OnlyInNavmap(String navZone) {
			super(navZone, Severity.Warn, I.tr("Only in NavMap."), getNavZoneConnectionDetails(navZone));
		}

		@Override
		public Path getFile() {
			return ctx.getNavMapManager().getNavMapFile();
		}

		@Override
		public void fix() {
			navMap.removeNavZone(getGuid());
			markFixed();
		}
	}

	private class BoundaryChanged extends BaseNavZoneChange {
		public BoundaryChanged(NavZone navZone) {
			super(navZone, Severity.Error, I.tr("Shape or position has changed."), getNavZoneConnectionDetails(navZone.getGuid()));
		}

		@Override
		public void fix() {
			navMap.updateNavZone(navCache.getZone(getGuid()).get());
			markFixed();
		}
	}
}
