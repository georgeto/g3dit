package de.george.g3dit.check.checks;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.util.HtmlCreator;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCAnchor_PS;
import de.george.lrentnode.classes.desc.CD;

public class CheckInvalidAnchors extends AbstractEntityCheck {
	private static class Anchor {
		private final EntityDescriptor descriptor;
		private final Set<String> interactPoints;

		public Anchor(EntityDescriptor descriptor, Set<String> interactPoints) {
			this.descriptor = descriptor;
			this.interactPoints = interactPoints;
		}
	}

	private static class InteractPoint {
		private final EntityDescriptor descriptor;
		private final String anchor;

		public InteractPoint(EntityDescriptor descriptor, String anchor) {
			this.descriptor = descriptor;
			this.anchor = anchor;
		}
	}

	private Map<String, Anchor> anchors = new HashMap<>();
	private Map<String, InteractPoint> interactPoints = new HashMap<>();
	private Set<String> guids = new HashSet<>();

	public CheckInvalidAnchors() {
		super("Ungültige Anchor ermitteln", "Ermittelt Inkosistenzen in der Zuordnung von Interaktionspunkten zu Anchorn.", 0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		guids.add(entity.getGuid());

		if (entity.hasClass(CD.gCAnchor_PS.class)) {
			gCAnchor_PS anchor = entity.getClass(CD.gCAnchor_PS.class);
			anchors.put(entity.getGuid(), new Anchor(descriptor.get(), ImmutableSet.copyOf(anchor.interactPoints.getNativeEntries())));
		} else if (entity.hasClass(CD.gCInteraction_PS.class)) {
			String anchorPoint = entity.getProperty(CD.gCInteraction_PS.AnchorPoint).getGuid();
			interactPoints.put(entity.getGuid(), new InteractPoint(descriptor.get(), anchorPoint));
		}

		return EntityPassStatus.Next;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		for (Anchor anchor : anchors.values()) {
			for (String interactPointGuid : anchor.interactPoints) {
				InteractPoint interactPoint = interactPoints.get(interactPointGuid);
				if (interactPoint != null) {
					if (interactPoint.anchor == null) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								"Anchor verweist auf Interaktionspunkt, der keinen Anchor eingetragen hat.",
								HtmlCreator.renderEntity(interactPoint.descriptor));
					} else if (!anchor.descriptor.getGuid().equals(interactPoint.anchor)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								"Anchor verweist auf Interaktionspunkt, der abweichenden Anchor eingetragen hat.",
								HtmlCreator.renderEntity(interactPoint.descriptor));
					}
				} else {
					if (guids.contains(interactPointGuid)) {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								"Anchor verweist auf Entity, die kein Interaktionspunkt ist.", interactPointGuid);
					} else {
						postEntityProblem(problemConsumer, anchor.descriptor, Severity.Fatal,
								"Anchor verweist auf nicht existente Entity.", interactPointGuid);
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
							"Interaktionspunkt verweist auf Anchor, der diesen nicht eingetragen hat.",
							HtmlCreator.renderEntity(anchor.descriptor));
				}
			} else {
				if (guids.contains(interactPoint.anchor)) {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Fatal,
							"Interaktionspunkt verweist auf Entity, die kein Anchor ist.", interactPoint.anchor);
				} else {
					postEntityProblem(problemConsumer, interactPoint.descriptor, Severity.Fatal,
							"Interaktionspunkt verweist auf nicht existente Entity.", interactPoint.anchor);
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
