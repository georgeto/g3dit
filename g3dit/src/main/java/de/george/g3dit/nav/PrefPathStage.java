package de.george.g3dit.nav;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3utils.structure.bCBox;
import de.george.navmap.data.PrefPath;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

public class PrefPathStage extends NavCalcStage {
	public PrefPathStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
	}

	@Override
	public String getTitle() {
		return "PrefPath";
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanel();

		addBackButton(mainPanel, NavCalcState.NavPath);
		addNextButton(mainPanel, NavCalcState.NegCircle);

		return mainPanel;
	}

	@Override
	protected void doExecute() {
		clearChanges();
		for (PrefPath prefPath : navMap.getPrefPaths()) {
			if (prefPath.getPoints().size() < 1) {
				addChange(new InvalidPrefPath(prefPath));
				continue;
			}

			String detecedZoneStart = navCalc.getZone(prefPath.getPoints().get(0), true, false, -1.0f).map(a -> a.areaId)
					.orElse(NavMap.INVALID_ZONE_ID);
			String detecedZoneEnd = navCalc.getZone(prefPath.getPoints().get(prefPath.getPoints().size() - 1), true, false, -1.0f)
					.map(a -> a.areaId).orElse(NavMap.INVALID_ZONE_ID);

			String zoneGuid = prefPath.getZoneGuid();

			Change spansTwoZones = null;
			boolean startInZone = !detecedZoneStart.equals(NavMap.INVALID_ZONE_ID);
			boolean endInZone = !detecedZoneEnd.equals(NavMap.INVALID_ZONE_ID);
			if (startInZone && endInZone && !detecedZoneStart.equals(detecedZoneEnd)) {
				spansTwoZones = addChange(new SpansTwoZones(prefPath));
			}

			if (!Objects.equals(detecedZoneStart, zoneGuid) && !Objects.equals(detecedZoneEnd, zoneGuid)) {
				addChange(new ChangedNavZone(prefPath, startInZone ? detecedZoneStart : detecedZoneEnd).dependsOn(spansTwoZones));
			} else if (!startInZone && !endInZone) {
				addChange(new NoNavZone(prefPath));

			}
		}
	}

	private class BasePrefPathChange extends BaseChange {
		protected PrefPath prefPath;

		public BasePrefPathChange(PrefPath prefPath, Severity severity, String message, String details) {
			super(prefPath.getVirtualGuid(), severity, message, details);
			this.prefPath = prefPath;
		}

		@Override
		public Path getFile() {
			return ctx.getNavMapManager().getNavMapFile();
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEditPrefPaths().selectObject(getGuid());
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addPrefPath(prefPath);
			EntityMap.getInstance(ctx).showArea(prefPath.getPoints().stream().reduce(new bCBox(), bCBox::merge, bCBox::merge));
		}

		@Override
		public void teleport() {
			IpcUtil.gotoPosition(prefPath.getRadiusOffset());
		}
	}

	private class InvalidPrefPath extends BasePrefPathChange {
		public InvalidPrefPath(PrefPath prefPath) {
			super(prefPath, Severity.Info, I.trf("Invalid PrefPath with only {0, number} points.", prefPath.getPoints().size()), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class ChangedNavZone extends BasePrefPathChange {
		private String zoneGuid;

		public ChangedNavZone(PrefPath prefPath, String detecedZoneGuid) {
			super(prefPath, Severity.Warn, I.tr("Detected different containing NavZone."),
					I.trf("Detected {0} instead of {1}.", detecedZoneGuid == null ? I.tr("no NavZone") : detecedZoneGuid,
							prefPath.getZoneGuid() == null ? I.tr("no NavZone") : prefPath.getZoneGuid()));
			zoneGuid = detecedZoneGuid;
		}

		@Override
		public void fix() {
			prefPath.setZoneGuid(zoneGuid);
			navMap.updatePrefPath(prefPath);
			markFixed();
		}

		// TODO: Show the changed zones!
	}

	private class NoNavZone extends BasePrefPathChange {
		public NoNavZone(PrefPath prefPath) {
			super(prefPath, Severity.Warn, I.tr("Detected no containing NavZone."), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class SpansTwoZones extends BasePrefPathChange {
		public SpansTwoZones(PrefPath prefPath) {
			super(prefPath, Severity.Warn, I.tr("Spans more than one NavZone."), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}
}
