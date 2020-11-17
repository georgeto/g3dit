package de.george.g3dit.entitytree.filter;

import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;

import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Holder;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.CD.gCInventorySlot;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bTValArray_bCPropertyID;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.util.PropertyUtil;
import de.george.lrentnode.util.PropertyUtil.ReferenceVisitor;

public class GuidEntityFilter extends AbstractEntityFilter {

	private static final ImmutableList<PropertyDescriptor<bTValArray_bCPropertyID>> NAV_ROUTINE_PROPERTIES = ImmutableList
			.of(CD.gCNavigation_PS.WorkingPoints, CD.gCNavigation_PS.RelaxingPoints, CD.gCNavigation_PS.SleepingPoints);

	public enum MatchMode {
		Guid,
		Stack,
		Routine,
		Enclave,
		AnchorPoint,
		PartyLeader,
		Entity,
		Template
	}

	private MatchMode matchMode;
	private String guidToMatch;

	public GuidEntityFilter(String text) {
		guidToMatch = processGuid(text);
		if (text.startsWith("#i#")) {
			matchMode = MatchMode.Stack;
		} else if (text.startsWith("#r#")) {
			matchMode = MatchMode.Routine;
		} else if (text.startsWith("#e#")) {
			matchMode = MatchMode.Enclave;
		} else if (text.startsWith("#a#")) {
			matchMode = MatchMode.AnchorPoint;
		} else if (text.startsWith("#p#")) {
			matchMode = MatchMode.PartyLeader;
		} else if (text.startsWith("#g#")) {
			matchMode = MatchMode.Entity;
		} else if (text.startsWith("#t#")) {
			matchMode = MatchMode.Template;
		} else {
			matchMode = MatchMode.Guid;
		}

		guidToMatch = processGuid(matchMode == MatchMode.Guid ? text : text.substring(3));
	}

	public GuidEntityFilter(MatchMode matchMode, String guidToMatch) {
		this.matchMode = matchMode;
		this.guidToMatch = processGuid(guidToMatch);
	}

	private String processGuid(String text) {
		String guid = GuidUtil.parseGuidPartial(text);
		return guid != null ? guid : "";
	}

	@Override
	public boolean matches(eCEntity entity) {
		switch (matchMode) {
			case Guid:
				return matchesGuid(entity);
			case Stack:
				return matchesStack(entity);
			case Routine:
				return matchesRoutine(entity);
			case Enclave:
				return matchesEnclave(entity);
			case AnchorPoint:
				return matchesAnchorPoint(entity);
			case PartyLeader:
				return matchesPartyLeader(entity);
			case Entity:
				return matchesEntity(entity);
			case Template:
				return matchesTemplate(entity);
		}
		return false;
	}

	private boolean matchesGuid(eCEntity entity) {
		return entity.getGuid().contains(guidToMatch);
	}

	private boolean matchesStack(eCEntity entity) {
		if (entity.hasClass(CD.gCInventory_PS.class)) {
			gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);
			for (G3Class stack : inv.stacks) {
				eCEntityProxy tpleGuid = stack.propertyNoThrow(gCInventorySlot.Template).orElse(null);
				if (tpleGuid != null
						&& (guidToMatch.isEmpty() || tpleGuid.getGuid() != null && tpleGuid.getGuid().contains(guidToMatch))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean matchesEnclave(eCEntity entity) {
		return entity.getClassOptional(CD.gCNPC_PS.class).flatMap(c -> c.propertyNoThrow(CD.gCNPC_PS.Enclave)).map(bCPropertyID::getGuid)
				.filter(g -> g.contains(guidToMatch)).isPresent();
	}

	private boolean matchesRoutine(eCEntity entity) {
		if (entity.hasClass(CD.gCNavigation_PS.class)) {
			G3Class nav = entity.getClass(CD.gCNavigation_PS.class);

			for (PropertyDescriptor<bTValArray_bCPropertyID> navProp : NAV_ROUTINE_PROPERTIES) {
				bTValArray_bCPropertyID routineGuids = nav.propertyNoThrow(navProp).orElse(null);
				if (routineGuids != null) {
					// #r# funktioniert auch falls Entity keine Routinen hat
					if (guidToMatch.isEmpty()) {
						return true;
					}

					for (bCPropertyID guid : routineGuids.getEntries()) {
						if (guid != null && guid.getGuid() != null && guid.getGuid().contains(guidToMatch)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean matchesAnchorPoint(eCEntity entity) {
		return entity.getClassOptional(CD.gCInteraction_PS.class).flatMap(c -> c.propertyNoThrow(CD.gCInteraction_PS.AnchorPoint))
				.map(eCEntityProxy::getGuid).filter(g -> g.contains(guidToMatch)).isPresent();
	}

	private boolean matchesPartyLeader(eCEntity entity) {
		return entity.getClassOptional(CD.gCParty_PS.class).flatMap(c -> c.propertyNoThrow(CD.gCParty_PS.PartyLeaderEntity))
				.map(eCEntityProxy::getGuid).filter(g -> g.contains(guidToMatch)).isPresent();
	}

	private boolean hasMatchingReference(BiConsumer<eCEntity, ReferenceVisitor> visit, eCEntity entity) {
		Holder<Boolean> matches = new Holder<>(false);
		visit.accept(entity, (value, property, propertySet) -> {
			if (value.getGuid() != null && value.getGuid().contains(guidToMatch)) {
				matches.hold(true);
				return false;
			}
			return true;
		});
		return matches.held();
	}

	private boolean matchesEntity(eCEntity entity) {
		return hasMatchingReference(PropertyUtil::visitEntityReferences, entity);
	}

	private boolean matchesTemplate(eCEntity entity) {
		if (entity.getCreator() != null && entity.getCreator().contains(guidToMatch)) {
			return true;
		}

		return hasMatchingReference(PropertyUtil::visitTemplateReferences, entity);
	}

	@Override
	public boolean isValid() {
		return !guidToMatch.isEmpty();
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public String getGuidToMatch() {
		return guidToMatch;
	}

	public static String getToolTipText() {
		// @foff
		return    "<html>"
				+ "Es kann sowohl nach vollständigen als auch unvollständigen Guids gesucht werden.<br>"
				+ "Der Guid kann einer der folgenden Präfixe vorangestellt werden."
				+ "<ul>"
				+ "<li><b>Kein Präfix</b>: Guid</li>"
				+ "<li><b>#i#</b>: Entity enthält InventoryStack mit passender Guid</li>"
				+ "<li><b>#r#</b>: Entity hat Routine mit passender Guid</li>"
				+ "<li><b>#e#</b>: Mitglieder der Enclave</li>"
				+ "<li><b>#a#</b>: Entities des AnchorPoints</li>"
				+ "<li><b>#p#</b>: PartyMember des PartyLeaders</li>"
				+ "<li><b>#g#</b>: Entities die in irgendeiner ihrer Eigenschaften auf diese Entity verweisen</li>"
				+ "<li><b>#t#</b>: Entities die in irgendeiner ihrer Eigenschaften auf dieses Template verweisen</li>"
				+ "</ul></html>";
		// @fon
	}
}
