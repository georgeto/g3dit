package de.george.g3dit.nav;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
								insideNegDetails = "InteractObject is inside of NegCircle.";
							} else if (!navCalc.testPointAgainstNegZones(area.areaId, entity.getWorldPosition())) {
								insideNegDetails = "InteractObject is inside of NegZone.";
							}

							if (insideNegDetails != null) {
								// Inside NegZone or NegCircle -> Assign OUT_OF_NAV_AREA_ID
								area.areaId = NavMap.OUT_OF_NAV_AREA_ID;
								addChange(new CriticalInteractObject(descriptor.get(), entity.getWorldPosition(), insideNegDetails));
							}
						}
					} else {
						// No area detected -> Assign OUT_OF_NAV_AREA_ID
						area = new NavArea(NavMap.OUT_OF_NAV_AREA_ID, false);
					}

					Optional<NavArea> navMapAreaOpt = navMapInteractables.get(entity.getGuid());
					if (!navMapAreaOpt.isPresent()) {
						addChange(new NotInNavmap(descriptor.get(), entity.getWorldPosition(), area));
					} else {
						NavArea navMapArea = navMapAreaOpt.orElseGet(() -> new NavArea(NavMap.OUT_OF_NAV_AREA_ID, false));
						if (!area.equals(navMapArea)) {
							addChange(new AreaChanged(descriptor.get(), entity.getWorldPosition(), area, navMapArea));
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
		// if (area == null || !NavMap.OUT_OF_NAV_AREA_ID.equals(area.areaId)) {
		// return "no NavArea";
		if (area == null) {
			return "no NavArea (null)";
		} else if (NavMap.OUT_OF_NAV_AREA_ID.equals(area.areaId)) {
			return "no NavArea (out of)";
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
		public File getFile() {
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
			super(entity, position, Severity.Error, "Not in NavMap.", String.format("Registered to %s.", getRegisteredToArea(area)));
			this.area = area;
		}

		@Override
		public void fix() {
			navMap.addInteractable(getGuid(), Optional.of(area));
			markFixed();
		}
	}

	private class OnlyInNavmap extends BaseInteractObjectChange {
		public OnlyInNavmap(String guid, Optional<NavArea> area) {
			super(guid, Severity.Warn, "Only in NavMap, no corresponding entity found!",
					String.format("Registered to %s.", getRegisteredToArea(area.orElse(null))));
		}

		@Override
		public File getFile() {
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
			super(entity, position, Severity.Error, "Registered NavArea has changed.",
					String.format("Registered to %s instead of %s.", getRegisteredToArea(area), getRegisteredToArea(navMapArea)));
			this.area = area;
		}

		@Override
		public void fix() {
			navMap.updateInteractable(getGuid(), Optional.of(area));
			markFixed();
		}

		// TODO: Show the changed zones!
	}

	private class CriticalInteractObject extends BaseExtInteractObjectChange {
		public CriticalInteractObject(EntityDescriptor entity, bCVector position, String details) {
			super(entity, position, Severity.Info, "Critical InteractObject.", details);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}
}
