package de.george.g3dit.nav;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import de.george.navmap.util.NavCalc.NavArea;

public class InteractObjectStage extends NavCalcStage {

	public InteractObjectStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
	}

	@Override
	public String getTitle() {
		return "InteractObject";
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanel(BaseInteractObjectChange.class, COLUMN_NAME, COLUMN_GUID, COLUMN_MESSAGE, COLUMN_FIXED,
				COLUMN_FILE);

		addBackButton(mainPanel, NavCalcState.NegCircle);

		return mainPanel;
	}

	@Override
	protected void doExecute() {
		clearChanges();

		// TODO: Cleanup duplicate entries in NavMap
		// TODO: Exclude Minhs_Testzone.lrentdat (maybe blacklist in config?)
		Set<String> interactList = ConcurrentHashMap.newKeySet();
		Map<String, Optional<NavArea>> navMapInteractables = navMap.getInteractables();

		ConcurrencyUtil.processInPartitions(file -> {
			ArchiveFile archive = FileUtil.openArchiveSafe(file, false, false).orElse(null);
			if (archive == null) {
				return;
			}

			for (eCEntity entity : archive.getEntities()) {
				if (entity.hasClass(CD.gCInteraction_PS.class) && !entity.hasClass(CD.gCNavigation_PS.class)
						&& !entity.hasClass(CD.gCItem_PS.class)
						&& (!entity.hasClass(CD.gCNPC_PS.class) || entity.hasClass(CD.gCNavOffset_PS.class))) {
					interactList.add(entity.getGuid());

					NavArea area = navCalc.getZone(entity, false, false).orElse(null);

					Supplier<EntityDescriptor> descriptor = () -> new EntityDescriptor(entity,
							new FileDescriptor(file, archive.getArchiveType()));

					if (area != null) {
						if (entity.hasClass(CD.gCAIHelper_FreePoint_PS.class) && !area.isNavPath) {
							// TODO: Shouldn't we test the NavOffset instead of world position?
							// (Bug in Gothic 3 code?)
							String insideNegDetails = null;
							if (!navCalc.testPointAgainstNegCircles(area.areaId, entity.getWorldPosition())) {
								insideNegDetails = I.tr("InteractObject is inside of NegCircle.");
							} else if (!navCalc.testPointAgainstNegZones(area.areaId, entity.getWorldPosition())) {
								insideNegDetails = I.tr("InteractObject is inside of NegZone.");
							}

							if (insideNegDetails != null) {
								// Inside NegZone or NegCircle -> Assign OUT_OF_NAV_AREA_ID
								area.areaId = NavMap.OUT_OF_NAV_AREA_ID;
								addChange(new CriticalInteractObject(descriptor.get(), entity.getWorldPosition(), insideNegDetails));
							}
						}
					}

					Optional<NavArea> navMapArea = navMapInteractables.get(entity.getGuid());
					if (navMapArea == null) {
						addChange(new NotInNavmap(descriptor.get(), entity.getWorldPosition(), area));
					} else {
						if (!Optional.ofNullable(area).equals(navMapArea)) {
							addChange(new AreaChanged(descriptor.get(), entity.getWorldPosition(), area, navMapArea.orElse(null)));
						}
					}
				}
			}
		}, ctx.getFileManager().listWorldFiles(), 3).await();

		for (

		String interactable : navMapInteractables.keySet()) {
			if (!interactList.contains(interactable)) {
				addChange(new OnlyInNavmap(interactable, navMapInteractables.get(interactable)));
			}

		}
	}

	private static String getRegisteredToArea(NavArea area) {
		if (area == null) {
			return I.tr("no NavArea (outside)");
		} else if (NavMap.OUT_OF_NAV_AREA_ID.equals(area.areaId)) {
			return I.tr("no NavArea (in NegZone/NegCircle)");
		} else {
			return String.format("%s %s", area.isNavPath ? "NavPath" : "NavZone", area.areaId);
		}
	}

	public abstract static class BaseInteractObjectChange extends BaseChange {
		public BaseInteractObjectChange(String guid, Severity severity, String message, String details) {
			super(guid, severity, message, details);
		}

		public String getName() {
			return "";
		}
	}

	private class BaseExtInteractObjectChange extends BaseInteractObjectChange {
		protected EntityDescriptor entity;
		protected bCVector position;

		public BaseExtInteractObjectChange(EntityDescriptor entity, bCVector position, Severity severity, String message, String details) {
			super(entity.getGuid(), severity, message, details);
			this.entity = entity;
			this.position = position;
		}

		@Override
		public Path getFile() {
			return entity.getFile().getPath();
		}

		@Override
		public String getName() {
			return entity.getDisplayName();
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEntity(entity);
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addEntity(entity, position);
		}

		@Override
		public void teleport() {
			IpcUtil.gotoPosition(position);
		}
	}

	private class NotInNavmap extends BaseExtInteractObjectChange {
		private NavArea area;

		public NotInNavmap(EntityDescriptor entity, bCVector position, NavArea area) {
			super(entity, position, Severity.Error, I.tr("Not in NavMap."), I.trf("Registered to {0}.", getRegisteredToArea(area)));
			this.area = area;
		}

		@Override
		public void fix() {
			navMap.addInteractable(getGuid(), Optional.ofNullable(area));
			markFixed();
		}
	}

	private class OnlyInNavmap extends BaseInteractObjectChange {
		public OnlyInNavmap(String guid, Optional<NavArea> area) {
			super(guid, Severity.Warn, I.tr("Only in NavMap, no corresponding entity found!"),
					I.trf("Registered to {0}.", getRegisteredToArea(area.orElse(null))));
		}

		@Override
		public Path getFile() {
			return ctx.getNavMapManager().getNavMapFile();
		}

		@Override
		public void fix() {
			navMap.removeInteractable(getGuid());
			markFixed();
		}
	}

	private class AreaChanged extends BaseExtInteractObjectChange {
		private NavArea area;

		public AreaChanged(EntityDescriptor entity, bCVector position, NavArea area, NavArea navMapArea) {
			super(entity, position, Severity.Error, I.tr("Registered NavArea has changed."),
					I.trf("Registered to {0} instead of {1}.", getRegisteredToArea(area), getRegisteredToArea(navMapArea)));
			this.area = area;
		}

		@Override
		public void fix() {
			navMap.updateInteractable(getGuid(), Optional.ofNullable(area));
			markFixed();
		}

		// TODO: Show the changed zones!
	}

	private class CriticalInteractObject extends BaseExtInteractObjectChange {
		public CriticalInteractObject(EntityDescriptor entity, bCVector position, String details) {
			super(entity, position, Severity.Info, I.tr("Critical InteractObject."), details);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}
}
