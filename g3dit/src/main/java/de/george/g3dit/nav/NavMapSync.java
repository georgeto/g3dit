package de.george.g3dit.nav;

import java.awt.BorderLayout;
import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import ca.phon.ui.jbreadcrumb.Breadcrumb;
import ca.phon.ui.jbreadcrumb.JBreadcrumb;
import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.util.NavMapManager.NavMapLoadedEvent;
import de.george.g3utils.gui.SwingUtils;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import net.miginfocom.swing.MigLayout;

public class NavMapSync extends JFrame {
	private NavMap navMap;
	private NavCache navCache;
	private NavCalc navCalc;

	private EditorContext ctx;
	private Breadcrumb<NavCalcState, NavCalcStage> breadcrumb;

	public NavMapSync(EditorContext ctx) {
		this.ctx = ctx;
		setTitle(I.tr("NavMap synchronisieren"));
		setIconImage(SwingUtils.getG3Icon());
		setSize(SwingUtils.getScreenWorkingWidth(), SwingUtils.getScreenWorkingHeight());
		setResizable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		ctx.getCacheManager().createCache(NavCache.class);
		ctx.getCacheManager().createCache(EntityCache.class);

		navMap = ctx.getNavMapManager().getNavMap(true);
		navCache = Caches.nav(ctx);
		navCalc = ctx.getNavMapManager().getNavCalc(true);

		createContentPanel();

		ctx.getNavMapManager().eventBus().register(this);
	}

	@Override
	public void dispose() {
		ctx.getNavMapManager().eventBus().unregister(this);
		super.dispose();
	}

	@Subscribe
	private void onNavMapLoaded(NavMapLoadedEvent e) {
		if (e.isSuccessful()) {
			navMap = ctx.getNavMapManager().getNavMap(true);
			navCalc = ctx.getNavMapManager().getNavCalc(true);

			if (breadcrumb != null) {
				breadcrumb.clear();
				gotoState(NavCalcState.NavZone);
			}
		}
	}

	public void createContentPanel() {
		breadcrumb = new Breadcrumb<>();
		JBreadcrumb<NavCalcState, NavCalcStage> bcPath = new JBreadcrumb<>(breadcrumb);

		JPanel mainPanel = new JPanel(new MigLayout("fill"));
		mainPanel.add(bcPath, "wrap");

		breadcrumb.addBreadcrumbListener(l -> {
			mainPanel.removeAll();
			if (!breadcrumb.isEmpty()) {
				JComponent component = breadcrumb.getCurrentValue().getComponent();
				mainPanel.add(bcPath, "height 30!, wrap");
				mainPanel.add(component, "push, grow");
			}
			mainPanel.repaint();
		});

		gotoState(NavCalcState.NavZone);

		add(mainPanel, BorderLayout.CENTER);
	}

	void gotoState(NavCalcState state) {
		if (breadcrumb.containsState(state)) {
			breadcrumb.gotoState(state);
		} else {
			try {
				Constructor<? extends NavCalcStage> constructor = state.getStage().getDeclaredConstructor(EditorContext.class,
						NavMap.class, NavCache.class, NavCalc.class, Consumer.class);
				constructor.setAccessible(true);
				breadcrumb.addState(state,
						constructor.newInstance(ctx, navMap, navCache, navCalc, (Consumer<NavCalcState>) this::gotoState));
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
