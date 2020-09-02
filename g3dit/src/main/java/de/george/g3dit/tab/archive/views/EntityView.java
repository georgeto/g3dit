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
import com.teamunify.i18n.I;

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

	private List<AbstractEntityTab> tabs;
	private JSplittedTypedTabbedPane<AbstractEntityTab> tbTabs;

	public EntityView(EditorArchiveTab ctx) {
		super(ctx);

		tbTabs = new JSplittedTypedTabbedPane<>(false);
		tbTabs.eventBus().register(this);

		tabs = new ArrayList<>();
		tabs.add(new AllgemeinTab(ctx).createValiditionPanel());
		tabs.add(new NPCTab(ctx).createValiditionPanel());
		tabs.add(new NavigationTab(ctx).createScrolledValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedInventarTab.class, ctx).createScrolledValiditionPanel());
		tabs.add(new InteractionTab(ctx).createValiditionPanel());
		tabs.add(new VegetationTab(ctx).createValiditionPanel());
		tabs.add(new NavZoneTab(ctx).createValiditionPanel());
		tabs.add(new NavPathTab(ctx).createValiditionPanel());
		tabs.add(new AnchorTab(ctx).createValiditionPanel());
		tabs.add(new PartyTab(ctx).createValiditionPanel());
		tabs.add(new AIZoneTab(ctx).createValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedEnclaveTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedMeshTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedAnimationTab.class, ctx).createValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedCollisionShapeTab.class, ctx).createScrolledValiditionPanel());
		tabs.add(new TeachTab(ctx).createValiditionPanel());
		tabs.add(new SharedEntityTabWrapper(SharedNavOffsetTab.class, ctx).createScrolledValiditionPanel());
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
		for (AbstractEntityTab tab : tabs) {
			if (tab.isActive(entity)) {
				tbTabs.addTab(tab);
				tab.loadValues(entity);
			} else {
				tbTabs.removeTab(tab);
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

		for (AbstractEntityTab tab : tabs) {
			try {
				if (tbTabs.containsTab(tab) && tab.isActive(entity)) {
					tab.saveValues(entity);
				}
			} catch (Exception e) {
				TaskDialogs.error(ctx.getParentWindow(), I.trf("Fehler beim Speichern des Tabs '{0}'.", tab.getTabTitle()),
						e.getMessage());
				logger.warn("Error while saving the Tab {}.", tab.getTabTitle(), e);
			}
		}
	}

	@Override
	public Component getContent() {
		return tbTabs.getComponent();
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
		tbTabs = null;
		for (AbstractEntityTab tab : tabs) {
			tab.cleanUp();
		}
	}
}
