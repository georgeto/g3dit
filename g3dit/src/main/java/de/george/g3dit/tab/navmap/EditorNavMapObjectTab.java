package de.george.g3dit.tab.navmap;

import java.awt.Component;

import javax.swing.Icon;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.EditorTab;

public class EditorNavMapObjectTab extends EditorTab {
	private String title;
	private NavMapObjectContentPane content;

	public EditorNavMapObjectTab(EditorContext ctx, EditorTabType tabType, NavMapObjectContentPane content) {
		super(ctx, tabType);
		title = I.trf("NavMap -> {0}s", tabType.name());
		this.content = content;
		content.initGui();
	}

	@Override
	public String getTabTitle() {
		return title;
	}

	@Override
	public Icon getTabIcon() {
		return null;
	}

	@Override
	public Component getTabContent() {
		return content.getLayeredPane();
	}

	@Override
	public boolean onClose(boolean appExit) {
		content.onClose();
		return true;
	}

	@Override
	public void onSave() {}

	@Override
	public void onSaveAs() {}

	@Override
	public String getTitle() {
		return title;
	}

	public void selectObject(String guid) {
		content.selectObject(guid);
	}
}
