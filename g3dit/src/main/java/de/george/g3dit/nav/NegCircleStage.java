package de.george.g3dit.nav;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.config.ConfigFiles;
import de.george.g3dit.config.GuidWithCommentConfigFile;
import de.george.g3dit.config.NegCirclePrototypeConfigFile;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3dit.util.GuidWithComment;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.data.NegCircle;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import de.george.navmap.util.NavCalc.NavArea;
import de.george.navmap.util.NegCircleCalc;
import one.util.streamex.StreamEx;

public class NegCircleStage extends NavCalcStage {
	private NegCirclePrototypeConfigFile negCirclePrototypes;
	private GuidWithCommentConfigFile negCirclesWithoutObject;
	private GuidWithCommentConfigFile objectsWithoutNegCircle;

	public NegCircleStage(EditorContext ctx, NavMap navMap, NavCache navCache, NavCalc navCalc, Consumer<NavCalcState> gotoState) {
		super(ctx, navMap, navCache, navCalc, gotoState);
		negCirclePrototypes = ctx.getFileManager().getConfigFile("NegCirclePrototypes.json", NegCirclePrototypeConfigFile.class);
		negCirclesWithoutObject = ConfigFiles.negCirclesWithoutObject(ctx);
		objectsWithoutNegCircle = ConfigFiles.objectsWithoutNegCircles(ctx);
	}

	@Override
	public String getTitle() {
		return "NegCircle";
	}

	@Override
	protected void getExtraButtons(JXTable changeTable, Consumer<JButton> add, Consumer<Runnable> repaintListener) {
		JButton btnBlacklist = SwingUtils.keyStrokeButton(I.tr("Blacklist"), I.tr("Mark the selected issue(s) as having no NegCircle."),
				Icons.getImageIcon(Icons.Select.CANCEL), KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK, this::onBlacklist);
		repaintListener.accept(TableUtil.enableOnGreaterEqual(changeTable, btnBlacklist, 1,
				() -> getSelectedChange().map(change -> change instanceof ObjectWithoutNegCircle && !change.isFixed()).orElse(false)));
		add.accept(btnBlacklist);
	}

	@Override
	public JComponent createComponent() {
		JPanel mainPanel = createMainPanelNamed();

		addBackButton(mainPanel, NavCalcState.PrefPath);
		addNextButton(mainPanel, NavCalcState.InteractObject);

		return mainPanel;
	}

	private static void assignAreas(NegCircle negCircle, List<NavArea> assignedAreas) {
		negCircle.zoneGuids = assignedAreas.stream().map(a -> a.areaId).collect(Collectors.toList());
	}

	private void checkCriticalNegCircle(NegCircle negCircle, List<NavArea> assignedAreas) {
		String navPaths = assignedAreas.stream().filter(a -> a.isNavPath).map(a -> a.areaId).collect(HtmlCreator.collectList());
		if (!navPaths.isEmpty()) {
			negCircle = negCircle.clone();
			assignAreas(negCircle, assignedAreas);
			addChange(new CriticalNegCircle(negCircle, HtmlCreator.renderList(I.tr("Overlaps with NavPaths:"), navPaths)));
		}
	}

	private void onBlacklist() {
		List<BaseNamedChange> blacklistedChanges = getSelectedChanges()
				.filter(change -> change instanceof ObjectWithoutNegCircle && !change.isFixed()).map(change -> (BaseNamedChange) change)
				.collect(Collectors.toList());

		List<GuidWithComment> blacklistedGuids = blacklistedChanges.stream()
				.map(change -> new GuidWithComment(change.getGuid(), change.getName())).collect(Collectors.toList());

		GuidWithCommentConfigFile objectsWithoutNegCircles = ConfigFiles.objectsWithoutNegCircles(ctx);
		if (objectsWithoutNegCircles.acquireEditLock()) {
			ImmutableSet<GuidWithComment> newContent = ImmutableSet
					.copyOf(Iterables.concat(objectsWithoutNegCircles.getContent(), blacklistedGuids));
			objectsWithoutNegCircles.updateContent(newContent);
			objectsWithoutNegCircles.releaseEditLock();

			// Mark blacklisted changes as fixed
			blacklistedChanges.forEach(BaseChange::markFixed);
			repaintChanges();
		} else {
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Objects without NegCircle"), I.tr("Already opened for editing."));
		}
	}

	@Override
	protected void doExecute() {
		clearChanges();

		NegCircleCalc calc = new NegCircleCalc();
		calc.setNegCirclePrototypes(negCirclePrototypes.getContent());

		Set<String> withoutNegCircle = objectsWithoutNegCircle.getGuids();
		Set<String> allEntities = ConcurrentHashMap.newKeySet();
		Set<String> movedNegCircles = ConcurrentHashMap.newKeySet();

		// Improve performance by caching NavZone lookup result
		navCalc.cacheNavZones();

		ConcurrencyUtil.processInPartitions(file -> {
			ArchiveFile archive = FileUtil.openArchiveSafe(file, false, false).orElse(null);
			if (archive == null) {
				return;
			}

			List<String> entities = new ArrayList<>();
			for (eCEntity entity : archive.getEntities()) {
				entities.add(entity.getGuid());
				if (!withoutNegCircle.contains(entity.getGuid()) && calc.hasNegCirclePrototype(entity)
						&& !navMap.hasNegCircle(entity.getGuid())) {
					// Object with NegCirclePrototype without NegCircle
					addChange(new ObjectWithoutNegCircle(calc.createNegCircleFromEntity(entity, navCalc)));
				} else if (!calc.hasNegCirclePrototype(entity) && navMap.hasNegCircle(entity.getGuid())) {
					// Object with NegCircle that has no NegCirclePrototype
					addChange(new NegCircleWithoutNegCirclePrototype(navMap.getNegCircle(entity.getGuid()).get()));
				} else if (!withoutNegCircle.contains(entity.getGuid()) && calc.hasNegCirclePrototype(entity)) {
					NegCircle ourNegCircle = calc.createNegCircleFromEntity(entity);
					NegCircle negCircle = navMap.getNegCircle(entity.getGuid()).get();

					List<String> details = new ArrayList<>(2);
					if (ourNegCircle.circleOffsets.size() != negCircle.circleOffsets.size()) {
						// Amount of NegCircle offsets has changed
						details.add(I.trf("NegCircle has {0, number} offsets instead of {1, number}.", ourNegCircle.circleOffsets.size(),
								negCircle.circleOffsets.size()));
					} else {
						for (int i = 0; i < ourNegCircle.circleOffsets.size(); i++) {
							// NegCircle offset has changed
							bCVector ourOffset = ourNegCircle.circleOffsets.get(i);
							bCVector offset = negCircle.circleOffsets.get(i);
							if (!ourOffset.simliar(offset, 0.01f)) {
								details.add(I.trf("Offset {0, number} differs: {1} instead of {2} ({3}).", i, ourOffset, offset,
										offset.getTransformed(entity.getWorldMatrix().getInverted())));
							}

							float ourRadius = ourNegCircle.circleRadius.get(i);
							float radius = negCircle.circleRadius.get(i);
							if (!Misc.compareFloat(ourRadius, radius, 0.0001f)) {
								// Neg Circle radius has changed.
								details.add(
										I.trf("Radius {0, number} differs: {1, number} instead of {2, number}.", i, ourRadius, radius));
							}
						}
					}

					if (!details.isEmpty()) {
						// Recalculate assigned areas for moved NegCircle
						List<NavArea> assignedAreas = NegCircleCalc.calcAssignedAreas(ourNegCircle, navCalc);
						checkCriticalNegCircle(ourNegCircle, assignedAreas);
						assignAreas(ourNegCircle, assignedAreas);
						addChange(new MovedOrModified(ourNegCircle, HtmlCreator.renderList(details)));
					}
				}
			}
			allEntities.addAll(entities);
		}, ctx.getFileManager().listWorldFiles(), 3).await();

		// Search for NegCircles with a changed area assigment (NavZone was moved or NegCircle was
		// moved)
		Awaitable awaitable = ConcurrencyUtil.processInPartitions(negCircle -> {
			// Skip NegCircles that already have been detected as moved
			if (movedNegCircles.contains(negCircle.getCircleGuid())) {
				return;
			}

			List<NavArea> assignedAreas = NegCircleCalc.calcAssignedAreas(negCircle, navCalc);
			checkCriticalNegCircle(negCircle, assignedAreas);

			List<String> details = new ArrayList<>();
			for (String navArea : negCircle.zoneGuids) {
				if (assignedAreas.stream().noneMatch(area -> area.areaId.equals(navArea))) {
					if (navMap.hasNavPath(navArea)) {
						details.add(I.trf("Not detected in NavPath {0}.", navArea));
					} else {
						details.add(I.trf("Not detected in NavZone {0}.", navArea));
					}
				}
			}

			for (NavArea navArea : assignedAreas) {
				if (!negCircle.zoneGuids.contains(navArea.areaId)) {
					details.add(I.trf("Unexpectedly detected in {0} {1}.", navArea.isNavPath ? "NavPath" : "NavZone", navArea.areaId));
				}
			}

			if (!details.isEmpty()) {
				assignAreas(negCircle, assignedAreas);
				addChange(new ChangedNavAreas(negCircle, HtmlCreator.renderList(details)));
			}
		}, navMap.getNegCircles().toList());

		// Search for NegCircles without a corresponding entity
		Set<String> withoutObject = negCirclesWithoutObject.getGuids();
		for (NegCircle negCircle : navMap.getNegCircles()) {
			if (!allEntities.contains(negCircle.circleGuid) && !withoutObject.contains(negCircle.circleGuid)) {
				addChange(new NegCircleWithoutObject(negCircle));
			}
		}

		// TODO: Check for stale neg circle clusters?

		awaitable.await();

		navCalc.uncacheNavZones();
	}

	private String formatAssignedNavAreas(NegCircle negCircle, String details) {
		String assignedNavAreas = HtmlCreator.renderList(StreamEx.of(negCircle.zoneGuids)
				.map(guid -> String.format("  %s %s", navCache.getPath(guid).isPresent() ? "NavPath" : "NavZone", guid)));

		if (details != null) {
			return HtmlCreator.renderList(details, "", I.tr("Assigned NavAreas:"), assignedNavAreas);
		} else {
			return HtmlCreator.renderList(I.tr("Assigned NavAreas:"), assignedNavAreas);
		}
	}

	public class BaseNegCircleChange extends BaseNamedChange {
		protected NegCircle negCircle;

		public BaseNegCircleChange(NegCircle negCircle, Severity severity, String message, String details) {
			super(negCircle.circleGuid, severity, message, formatAssignedNavAreas(negCircle, details));
			this.negCircle = negCircle;
		}

		@Override
		public File getFile() {
			return Caches.entity(ctx).getFile(getGuid()).map(FileDescriptor::getPath).orElse(null);
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEditNegCircles().selectObject(getGuid());
		}

		@Override
		public void showOnMap() {
			EntityMap.getInstance(ctx).addNegCircle(negCircle);
			EntityMap.getInstance(ctx).showArea(negCircle.circleOffsets.stream().reduce(new bCBox(), bCBox::merge, bCBox::merge));
		}

		@Override
		public void teleport() {
			IpcUtil.gotoPosition(negCircle.getCenter());
		}
	}

	private class CriticalNegCircle extends BaseNegCircleChange {
		public CriticalNegCircle(NegCircle negCircle, String details) {
			super(negCircle, Severity.Info, I.tr("Critical NegCircle."), details);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class ChangedNavAreas extends BaseNegCircleChange {
		public ChangedNavAreas(NegCircle negCircle, String details) {
			super(negCircle, Severity.Error, I.tr("Detected different containing NavAreas."), details);
		}

		@Override
		public void fix() {
			navMap.updateNegCircle(negCircle);
			markFixed();
		}
	}

	private class MovedOrModified extends BaseNegCircleChange {
		public MovedOrModified(NegCircle negCircle, String details) {
			super(negCircle, Severity.Error, I.tr("Entity was moved or modified."), details);
		}

		@Override
		public void fix() {
			navMap.updateNegCircle(negCircle);
			markFixed();
		}
	}

	private class ObjectWithoutNegCircle extends BaseNegCircleChange {
		public ObjectWithoutNegCircle(NegCircle negCircle) {
			super(negCircle, Severity.Warn, I.tr("Object without NegCircle."), null);
		}

		@Override
		public void showInEditor() {
			ctx.getEditor().openEntity(getGuid());
		}

		@Override
		public void fix() {
			navMap.addNegCircle(negCircle);
			markFixed();
		}
	}

	private class NegCircleWithoutNegCirclePrototype extends BaseNegCircleChange {
		public NegCircleWithoutNegCirclePrototype(NegCircle negCircle) {
			super(negCircle, Severity.Info, I.tr("NegCircle for object without NegCirclePrototype."), null);
		}

		@Override
		protected boolean fixable() {
			return false;
		}
	}

	private class NegCircleWithoutObject extends BaseNegCircleChange {
		public NegCircleWithoutObject(NegCircle negCircle) {
			super(negCircle, Severity.Warn, I.tr("NegCircle without object."), null);
		}

		@Override
		public void fix() {
			navMap.removeNegCircle(negCircle.getCircleGuid());
			markFixed();
		}
	}
}
