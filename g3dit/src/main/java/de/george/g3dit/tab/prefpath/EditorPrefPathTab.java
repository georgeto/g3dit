package de.george.g3dit.tab.prefpath;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.navmap.EditorNavMapObjectTab;

public class EditorPrefPathTab extends EditorNavMapObjectTab {
	public EditorPrefPathTab(EditorContext ctx) {
		super(ctx, EditorTabType.PrefPath, new PrefPathContentPane(ctx));
	}
}
