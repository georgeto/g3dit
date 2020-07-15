package de.george.g3dit.tab.negzone;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.navmap.EditorNavMapObjectTab;

public class EditorNegZoneTab extends EditorNavMapObjectTab {
	public EditorNegZoneTab(EditorContext ctx) {
		super(ctx, EditorTabType.NegZone, new NegZoneContentPane(ctx));
	}
}
