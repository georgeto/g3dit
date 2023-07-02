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
import de.george.navmap.data.NegZone;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

public class NegZoneStage extends NavCalcStage {
	public NegZoneStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
	}

	@Override
	public String getTitle() {
		return "NegZone";
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanel();

		addBackButton(mainPanel, NavCalcState.NavZone);
		addNextButton(mainPanel, NavCalcState.NavPath);

		return mainPanel;
	}

	@Override
	protected void doExecute() {
		clearChanges();
		for (NegZone negZone : navMap.getNegZones()) {
			if (negZone.getPointCount() < 3) {
				addChange(new InvalidNegZone(negZone));
				continue;
			}

			String detecedZoneGuid = navCalc.getNavZoneGuid(negZone);

			String zoneGuid = negZone.getZoneGuid();
			if (!Objects.equals(detecedZoneGuid, zoneGuid)) {
				addChange(new ChangedNavZone(negZone, detecedZoneGuid));
			} else if (detecedZoneGuid.equals(NavMap.INVALID_ZONE_ID)) {
				addChange(new NoNavZone(negZone));
			}

			if (!negZone.getZoneGuid().equals(NavMap.INVALID_ZONE_ID)
					&& navMap.getNegZonesForZone(negZone.getZoneGuid()).noneMatch(n -> n.getGuid().equals(negZone.getGuid()))) {
				addChange(new UniRefNegZone(negZone, negZone.getZoneGuid()));
			}
		}

		for (String navZone : navMap.getNavZones()) {
			for (NegZone negZone : navMap.getNegZonesForZone(navZone)) {
				if (!negZone.getZoneGuid().equals(navZone)) {
					addChange(new UniRefNavZone(navZone, negZone, negZone.getZoneGuid()));
				}
			}
		}
	}

	private class BaseNegZoneChange extends BaseChange {
		protected NegZone negZone;

		public BaseNegZoneChange(NegZone negZone, Severity severity, String message, String details) {
			super(negZone.getGuid(), severity, message, details);
			this.negZone = negZone;
		}

		@Override
		public Path getFile() {
			return ctx.getNavMapManager().getNavMapFile();
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEditNegZones().selectObject(getGuid());
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addNegZone(negZone);
			EntityMap.getInstance(ctx).showArea(negZone.getWorldPoints().stream().reduce(new bCBox(), bCBox::merge, bCBox::merge));
		}

		@Override
		public void teleport() {
			IpcUtil.gotoPosition(negZone.getWorldRadiusOffset());
		}
	}

	private class InvalidNegZone extends BaseNegZoneChange {
		public InvalidNegZone(NegZone negZone) {
			super(negZone, Severity.Info, I.trf("Invalid NegZone with only {0, number} sticks.", negZone.getPointCount()), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class ChangedNavZone extends BaseNegZoneChange {
		private String zoneGuid;

		public ChangedNavZone(NegZone negZone, String detecedZoneGuid) {
			super(negZone, Severity.Error, I.tr("Detected different containing NavZone."),
					I.trf("Detected {0} instead of {1}.", detecedZoneGuid == null ? I.tr("no NavZone") : detecedZoneGuid,
							negZone.getZoneGuid() == null ? I.tr("no NavZone") : negZone.getZoneGuid()));
			zoneGuid = detecedZoneGuid;
		}

		@Override
		public void fix() {
			negZone.setZoneGuid(zoneGuid);
			navMap.updateNegZone(negZone);
			markFixed();
		}

		// TODO: Show the changed zones!
	}

	private class NoNavZone extends BaseNegZoneChange {
		public NoNavZone(NegZone negZone) {
			super(negZone, Severity.Warn, I.tr("Detected no containing NavZone."), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class UniRefNegZone extends BaseNegZoneChange {
		public UniRefNegZone(NegZone negZone, String navZone) {
			super(negZone, Severity.Error, I.trf("NavZone {0} referred by NegZone does not refer to NegZone.", navZone), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class UniRefNavZone extends BaseNegZoneChange {
		public UniRefNavZone(String refNavZone, NegZone negZone, String realNavZone) {
			super(negZone, Severity.Error, I.trf("NegZone referred by NavZone {0} refers to other NavZone {1}.", refNavZone, realNavZone),
					null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}
}
