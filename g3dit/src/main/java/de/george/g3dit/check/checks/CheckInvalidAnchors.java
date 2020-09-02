package de.george.g3dit.check.checks;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
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
		super(I.tr("Ungültige Anchor ermitteln"), I.tr("Ermittelt Inkonsistenzen in der Zuordnung von Interaktionspunkten zu Anchors."), 0,
				1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
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
				postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
						I.tr("Anchor befindet sich in .node (UserCount nicht Teil des Spielstands)"),
						HtmlCreator.renderList("AnchorType: " + G3Enums.asString(gEAnchorType.class, anchor.anchorType),
								I.tr("Interact Points: ") + anchor.interactPoints.size()));
			}

			for (String interactPointGuid : anchor.interactPoints) {
				InteractPoint interactPoint = interactPoints.get(interactPointGuid);
				if (interactPoint != null) {
					if (interactPoint.anchor == null) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								I.tr("Anchor verweist auf Interaktionspunkt, der keinen Anchor eingetragen hat."),
								HtmlCreator.renderEntity(interactPoint.descriptor));
					} else if (!anchor.descriptor.getGuid().equals(interactPoint.anchor)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								I.tr("Anchor verweist auf Interaktionspunkt, der abweichenden Anchor eingetragen hat."),
								HtmlCreator.renderEntity(interactPoint.descriptor));
					}
				} else {
					if (guids.contains(interactPointGuid)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								I.tr("Anchor verweist auf Entity, die kein Interaktionspunkt ist."), interactPointGuid);
					} else {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								I.tr("Anchor verweist auf nicht existente Entity."), interactPointGuid);
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
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Fatal,
							I.tr("Interaktionspunkt verweist auf Anchor, der diesen nicht eingetragen hat."),
							HtmlCreator.renderEntity(anchor.descriptor));
				}
			} else {
				if (guids.contains(interactPoint.anchor)) {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Fatal,
							I.tr("Interaktionspunkt verweist auf Entity, die kein Anchor ist."), interactPoint.anchor);
				} else {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Fatal,
							I.tr("Interaktionspunkt verweist auf nicht existente Entity."), interactPoint.anchor);
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
