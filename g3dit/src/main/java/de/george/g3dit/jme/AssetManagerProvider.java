package de.george.g3dit.jme;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.jme3.asset.AssetManager;
import com.jme3.system.JmeSystem;

import de.george.g3dit.EditorContext;
import de.george.g3dit.jme.asset.AbsolutePathLocator;
import de.george.g3dit.jme.asset.G3FileLocator;
import de.george.g3dit.jme.asset.XactLoader;
import de.george.g3dit.jme.asset.XcmshLoader;
import de.george.g3dit.jme.asset.XimgLoader;
import de.george.g3dit.jme.asset.XlmshLoader;
import de.george.g3dit.jme.asset.XshmatLoader;
import de.george.g3dit.settings.SettingsUpdatedEvent;

public class AssetManagerProvider {
	private static class Holder {
		public static final AssetManagerProvider INSTANCE = new AssetManagerProvider();
	}

	private Set<String> assetLocations = new LinkedHashSet<>();
	private AssetManager assetManager;
	private EditorContext ctx;

	private AssetManagerProvider() {
		initAssetManager();
	}

	private void initAssetManager() {
		assetManager = JmeSystem.newAssetManager(JmeSystem.getPlatformAssetConfigURL());
		assetManager.registerLoader(XcmshLoader.class, "xcmsh");
		assetManager.registerLoader(XactLoader.class, "xact");
		assetManager.registerLoader(XshmatLoader.class, "xshmat");
		assetManager.registerLoader(XimgLoader.class, "ximg");
		assetManager.registerLoader(XlmshLoader.class, "xlmsh");

		assetManager.registerLocator("", AbsolutePathLocator.class);
	}

	private void updateAssetLocations() {
		List<String> newAssetLocations = ctx.getFileManager().getAssetLocations();

		// Unregister obsolete assetLocations
		for (String location : assetLocations) {
			assetManager.unregisterLocator(location, G3FileLocator.class);
		}

		assetLocations.clear();
		assetLocations.addAll(newAssetLocations);

		// Register new assetLocations
		for (String location : assetLocations) {
			assetManager.registerLocator(location, G3FileLocator.class);
		}
	}

	private synchronized void initEditorContext(EditorContext ctx) {
		if (this.ctx == null) {
			this.ctx = ctx;
			ctx.eventBus().register(this);
			updateAssetLocations();
		}
	}

	@Subscribe
	public void onSettingsUpdated(SettingsUpdatedEvent event) {
		updateAssetLocations();
	}

	public static AssetManager getAssetManager(EditorContext ctx) {
		AssetManagerProvider provider = Holder.INSTANCE;
		provider.initEditorContext(ctx);
		return provider.assetManager;
	}
}
