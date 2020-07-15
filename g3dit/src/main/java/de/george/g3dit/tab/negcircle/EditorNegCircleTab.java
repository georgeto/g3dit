package de.george.g3dit.tab.negcircle;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.navmap.EditorNavMapObjectTab;

public class EditorNegCircleTab extends EditorNavMapObjectTab {
	public EditorNegCircleTab(EditorContext ctx) {
		super(ctx, EditorTabType.NegCircle, new NegCircleContentPane(ctx));
	}
}
