package de.george.g3dit.tab.archive.views;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.eventbus.Subscribe;

import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.gui.components.tab.JSplittedTypedTabbedPane;
import de.george.g3dit.gui.components.tab.TabSelectedEvent;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.archive.views.entity.AIZoneTab;
import de.george.g3dit.tab.archive.views.entity.AbstractEntityTab;
import de.george.g3dit.tab.archive.views.entity.AllgemeinTab;
import de.george.g3dit.tab.archive.views.entity.AnchorTab;
import de.george.g3dit.tab.archive.views.entity.InteractionTab;
import de.george.g3dit.tab.archive.views.entity.NPCTab;
import de.george.g3dit.tab.archive.views.entity.NavPathTab;
import de.george.g3dit.tab.archive.views.entity.NavZoneTab;
import de.george.g3dit.tab.archive.views.entity.NavigationTab;
import de.george.g3dit.tab.archive.views.entity.PartyTab;
import de.george.g3dit.tab.archive.views.entity.SharedEntityTabWrapper;
import de.george.g3dit.tab.archive.views.entity.TeachTab;
import de.george.g3dit.tab.archive.views.entity.VegetationTab;
import de.george.g3dit.tab.shared.SharedAnimationTab;
import de.george.g3dit.tab.shared.SharedCollisionShapeTab;
import de.george.g3dit.tab.shared.SharedEnclaveTab;
import de.george.g3dit.tab.shared.SharedInventarTab;
import de.george.g3dit.tab.shared.SharedMeshTab;
import de.george.g3dit.tab.shared.SharedNavOffsetTab;
import de.george.lrentnode.archive.eCEntity;

public class EntityView extends SingleEntityArchiveView {
	private static final Logger logger = LoggerFactory.getLogger(EntityView.class);

	private JSplittedTypedTabbedPane<AbstractEntityTab> tabs;
	private List<AbstractEntityTab> listTabs;

	public EntityView(EditorArchiveTab ctx) {
		super(ctx);

		tabs = new JSplittedTypedTabbedPane<>(false);
		tabs.eventBus().register(this);

		listTabs = new ArrayList<>();
		listTabs.add(new AllgemeinTab(ctx).createValiditionPanel());
		listTabs.add(new NPCTab(ctx).createValiditionPanel());
		listTabs.add(new NavigationTab(ctx).createScrolledValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedInventarTab.class, ctx).createScrolledValiditionPanel());
		listTabs.add(new InteractionTab(ctx).createValiditionPanel());
		listTabs.add(new VegetationTab(ctx).createValiditionPanel());
		listTabs.add(new NavZoneTab(ctx).createValiditionPanel());
		listTabs.add(new NavPathTab(ctx).createValiditionPanel());
		listTabs.add(new AnchorTab(ctx).createValiditionPanel());
		listTabs.add(new PartyTab(ctx).createValiditionPanel());
		listTabs.add(new AIZoneTab(ctx).createValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedEnclaveTab.class, ctx).createValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedMeshTab.class, ctx).createValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedAnimationTab.class, ctx).createValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedCollisionShapeTab.class, ctx).createScrolledValiditionPanel());
		listTabs.add(new TeachTab(ctx).createValiditionPanel());
		listTabs.add(new SharedEntityTabWrapper(SharedNavOffsetTab.class, ctx).createScrolledValiditionPanel());
	}

	@Subscribe
	public void onTabSelect(TabSelectedEvent<AbstractEntityTab> event) {
		event.getPreviousTab().ifPresent(tab -> tab.unregisterKeyStrokes((JComponent) ctx.getTabContent()));
		event.getTab().ifPresent(tab -> tab.registerKeyStrokes((JComponent) ctx.getTabContent()));
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	@Override
	public void loadInternal(eCEntity entity) {
		for (AbstractEntityTab tab : listTabs) {
			if (tab.isActive(entity)) {
				tabs.addTab(tab);
				tab.loadValues(entity);
			} else {
				tabs.removeTab(tab);
			}
		}
	}

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
	 */
	@Override
	public void save(eCEntity entity) {
		if (entity == null) {
			return;
		}

		for (AbstractEntityTab tab : listTabs) {
			try {
				if (tabs.containsTab(tab) && tab.isActive(entity)) {
					tab.saveValues(entity);
				}
			} catch (Exception e) {
				TaskDialogs.error(ctx.getParentWindow(), "Fehler beim Speichern des Tabs " + tab.getTabTitle(), e.getMessage());
				logger.warn("Fehler beim Speichern des Tabs {}: ", tab.getTabTitle(), e);
			}
		}
	}

	@Override
	public Component getContent() {
		return tabs.getComponent();
	}

	@Override
	public ITreeExtension getTreeExtension() {
		return new EntityTreeExtension(ctx);
	}

	@Override
	public void onEnter() {
		ctx.getEntityTree().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	@Override
	public void cleanUp() {
		tabs = null;
		for (AbstractEntityTab tab : listTabs) {
			tab.cleanUp();
		}
	}
}
