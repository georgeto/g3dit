package de.george.g3dit.check.checks;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.util.HtmlCreator;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCAnchor_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEAnchorType;

public class CheckInvalidAnchors extends AbstractEntityCheck {
	private static class Anchor {
		private final EntityDescriptor descriptor;
		private final Set<String> interactPoints;
		private final boolean dynamic;
		private final int anchorType;

		public Anchor(EntityDescriptor descriptor, Set<String> interactPoints, boolean dynamic, int anchorType) {
			this.descriptor = descriptor;
			this.interactPoints = interactPoints;
			this.dynamic = dynamic;
			this.anchorType = anchorType;
		}
	}

	private static class InteractPoint {
		private final EntityDescriptor descriptor;
		private final String anchor;
		private final boolean dynamic;

		public InteractPoint(EntityDescriptor descriptor, String anchor, boolean dynamic) {
			this.descriptor = descriptor;
			this.anchor = anchor;
			this.dynamic = dynamic;
		}
	}

	private Map<String, Anchor> anchors = new HashMap<>();
	private Map<String, InteractPoint> interactPoints = new HashMap<>();
	private Set<String> guids = new HashSet<>();

	public CheckInvalidAnchors() {
		super(I.tr("Find invalid anchors"), I.tr("Finds inconsistencies in the assignment of interaction points to anchors."), 0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, Path dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		guids.add(entity.getGuid());

		if (entity.hasClass(CD.gCAnchor_PS.class)) {
			gCAnchor_PS anchor = entity.getClass(CD.gCAnchor_PS.class);
			anchors.put(entity.getGuid(), new Anchor(descriptor.get(), ImmutableSet.copyOf(anchor.interactPoints.getNativeEntries()),
					archiveFile.isLrentdat(), anchor.property(CD.gCAnchor_PS.AnchorType).getEnumValue()));
		} else if (entity.hasClass(CD.gCInteraction_PS.class)) {
			String anchorPoint = entity.getProperty(CD.gCInteraction_PS.AnchorPoint).getGuid();
			interactPoints.put(entity.getGuid(), new InteractPoint(descriptor.get(), anchorPoint, archiveFile.isLrentdat()));
		}

		return EntityPassStatus.Next;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		for (Anchor anchor : anchors.values()) {
			if (!anchor.dynamic) {
				postEntityProblem(problemConsumer, anchor.descriptor, Severity.Error,
						I.tr("Anchor is inside .node (UserCount not part of the save game)"),
						HtmlCreator.renderList("AnchorType: " + G3Enums.asString(gEAnchorType.class, anchor.anchorType),
								I.tr("Interact points") + ": " + anchor.interactPoints.size()));
			}

			for (String interactPointGuid : anchor.interactPoints) {
				InteractPoint interactPoint = interactPoints.get(interactPointGuid);
				if (interactPoint != null) {
					if (interactPoint.anchor == null) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Error,
								I.tr("Anchor refers to interaction point that has no anchor set."),
								HtmlCreator.renderEntity(interactPoint.descriptor));
					} else if (!anchor.descriptor.getGuid().equals(interactPoint.anchor)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Error,
								I.tr("Anchor refers to interaction point that has different anchor set."),
								HtmlCreator.renderEntity(interactPoint.descriptor));
					}
				} else {
					if (guids.contains(interactPointGuid)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Error,
								I.tr("Anchor refers to entity, which is not an interaction point."), interactPointGuid);
					} else {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Error,
								I.tr("Anchor refers to non-existent entity."), interactPointGuid);
					}
				}
			}
		}

		for (InteractPoint interactPoint : interactPoints.values()) {
			if (interactPoint.anchor == null) {
				continue;
			}

			Anchor anchor = anchors.get(interactPoint.anchor);
			if (anchor != null) {
				if (!anchor.interactPoints.contains(interactPoint.descriptor.getGuid())) {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Error,
							I.tr("Interaction point has an anchor set, which does not refer to it."),
							HtmlCreator.renderEntity(anchor.descriptor));
				}
			} else {
				if (guids.contains(interactPoint.anchor)) {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Error,
							I.tr("Interaction point refers to entity that is not an anchor."), interactPoint.anchor);
				} else {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Error,
							I.tr("Interaction point refers to non-existent entity."), interactPoint.anchor);
				}
			}
		}
	}

	@Override
	public void reset() {
		anchors.clear();
		interactPoints.clear();
		guids.clear();
	}
}
