package de.george.navmap.sections;

import java.util.List;
import java.util.Optional;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.util.NavCalc.NavArea;

/**
 * Verknüpfung von Interaktionsgegenständen mit der umliegenden NavZone
 */
public class Section3e implements G3Serializable {
	public List<AssignZoneToInteractable> interactables;

	@Override
	public void read(G3FileReader reader) {
		interactables = reader.readPrefixedList(AssignZoneToInteractable.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(interactables);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3e");
		builder.append("\nInteraktionsgegenstände Anzahl: " + interactables.size());
		for (int i = 0; i < interactables.size(); i++) {
			builder.append("\nObjekt " + i);
			AssignZoneToInteractable obj = interactables.get(i);
			builder.append("\nGuid: " + Optional.ofNullable(obj.interactable.getGuid()).orElse("-"));
			builder.append("\nType: " + obj.area.getPropertySetName());
			builder.append("\nNavArea-Guid: " + Optional.ofNullable(obj.area.getGuid()).orElse("-"));
		}
	}

	public void addInteractable(String guid, Optional<NavArea> area) {
		interactables
				.add(new AssignZoneToInteractable(new eCPropertySetProxy(guid, "gCInteraction_PS"), NavArea.toPropertySetProxy(area)));
	}

	public void updateInteractable(String guid, Optional<NavArea> area) {
		// Interactable can be listed multiple times (bug in the Gothic 3 code?)
		int previousIndex = -1;
		// Remove all entries for this guid except the first one
		for (int i = interactables.size() - 1; i >= 0; i--) {
			AssignZoneToInteractable interactable = interactables.get(i);
			if (guid.equals(interactable.interactable.getGuid())) {
				if (previousIndex != -1) {
					// Remove duplicate entry
					interactables.remove(previousIndex);
				}
				previousIndex = i;
			}
		}

		if (previousIndex == -1) {
			// Entry had not entry
			addInteractable(guid, area);
		} else {
			// Update existing entry
			AssignZoneToInteractable interactable = interactables.get(previousIndex);
			interactable.area = NavArea.toPropertySetProxy(area);
		}

	}

	public void removeInteractable(String guid) {
		interactables.removeIf(e -> guid.equals(e.interactable.getGuid()));
	}

	public void removeNavZone(String guid) {
		for (AssignZoneToInteractable interactable : interactables) {
			if (guid.equals(interactable.area.getGuid())) {
				interactable.area = NavArea.toPropertySetProxy(Optional.empty());
			}
		}
	}

	public void removeNavPath(String guid) {
		removeNavZone(guid);
	}

	public static class AssignZoneToInteractable implements G3Serializable {
		// Entries with interactable.getGuid() == null and zone.getGuid() == null, are entries that
		// have at least one of gCNavigation_PS or gCItem_PS.
		public eCPropertySetProxy interactable;
		public eCPropertySetProxy area;

		public AssignZoneToInteractable(eCPropertySetProxy interactable, eCPropertySetProxy area) {
			this.interactable = interactable;
			this.area = area;
		}

		@Override
		public void read(G3FileReader reader) {
			interactable = reader.read(eCPropertySetProxy.class);
			area = reader.read(eCPropertySetProxy.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(interactable);
			writer.write(area);
		}
	}
}
