package de.george.g3dit.tab.template.views;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.components.tab.JSplittedTypedTabbedPane;
import de.george.g3dit.gui.components.tab.TabSelectedEvent;
import de.george.g3dit.tab.shared.SharedAnimationTab;
import de.george.g3dit.tab.shared.SharedCollisionShapeTab;
import de.george.g3dit.tab.shared.SharedEnclaveTab;
import de.george.g3dit.tab.shared.SharedMeshTab;
import de.george.g3dit.tab.shared.SharedNavOffsetTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.tab.template.views.header.AbstractTemplateTab;
import de.george.g3dit.tab.template.views.header.AllgemeinTab;
import de.george.g3dit.tab.template.views.header.InventarTab;
import de.george.g3dit.tab.template.views.header.ItemScriptTab;
import de.george.g3dit.tab.template.views.header.ItemTab;
import de.george.g3dit.tab.template.views.header.MapTab;
import de.george.g3dit.tab.template.views.header.RecipeTab;
import de.george.g3dit.tab.template.views.header.SharedTemplateTabWrapper;
import de.george.g3dit.tab.template.views.header.StatsTab;
import de.george.g3dit.tab.template.views.header.TreasureSetTab;
import de.george.lrentnode.template.TemplateFile;

public class TemplateHeaderView implements TemplateView {
	private static final Logger logger = LoggerFactory.getLogger(TemplateHeaderView.class);

	private EditorTemplateTab ctx;

	private List<AbstractTemplateTab> tabs;
	private JSplittedTypedTabbedPane<AbstractTemplateTab> tbTabs;

	public TemplateHeaderView(EditorTemplateTab inEditor) {
		ctx = inEditor;

		tbTabs = new JSplittedTypedTabbedPane<>(false);
		tbTabs.eventBus().register(this);

		tabs = new ArrayList<>();
		tabs.add(new AllgemeinTab(ctx).createValiditionPanel());
		tabs.add(new InventarTab(ctx).createScrolledValiditionPanel());
		tabs.add(new RecipeTab(ctx).createScrolledValiditionPanel());
		tabs.add(new ItemTab(ctx).createValiditionPanel());
		tabs.add(new StatsTab(ctx).createScrolledValiditionPanel());
		tabs.add(new TreasureSetTab(ctx).createValiditionPanel());
		tabs.add(new SharedTemplateTabWrapper(SharedEnclaveTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedTemplateTabWrapper(SharedMeshTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedTemplateTabWrapper(SharedAnimationTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedTemplateTabWrapper(SharedCollisionShapeTab.class, ctx).createScrolledValiditionPanel());
		tabs.add(new ItemScriptTab(ctx).createValiditionPanel());
		tabs.add(new SharedTemplateTabWrapper(SharedNavOffsetTab.class, ctx).createScrolledValiditionPanel());
		tabs.add(new MapTab(ctx).createValiditionPanel());
	}

	@Override
	public Component getContent() {
		return tbTabs.getComponent();
	}

	@Subscribe
	public void onTabSelect(TabSelectedEvent<AbstractTemplateTab> event) {
		event.getPreviousTab().ifPresent(tab -> tab.unregisterKeyStrokes((JComponent) ctx.getTabContent()));
		event.getTab().ifPresent(tab -> tab.registerKeyStrokes((JComponent) ctx.getTabContent()));
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	@Override
	public void load(TemplateFile tple) {
		if (tple == null) {
			return;
		}

		for (AbstractTemplateTab tab : tabs) {
			if (tab.isActive(tple)) {
				tbTabs.addTab(tab);
				tab.loadValues(tple);
			} else {
				tbTabs.removeTab(tab);
			}
		}
	}

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
	 */
	@Override
	public void save(TemplateFile tple) {
		if (tple == null) {
			return;
		}

		for (AbstractTemplateTab tab : tabs) {
			try {
				if (tbTabs.containsTab(tab) && tab.isActive(tple)) {
					tab.saveValues(tple);
				}
			} catch (Exception e) {
				TaskDialogs.error(ctx.getParentWindow(), I.trf("Error while saving the tab ''{0}''.", tab.getTabTitle()), e.getMessage());
				logger.warn("Error while saving the tab '{}'.", tab.getTabTitle(), e);
			}
		}
	}

	@Override
	public void cleanUp() {
		for (AbstractTemplateTab tab : tabs) {
			tbTabs.removeTab(tab);
			tab.cleanUp();
		}

	}
}
