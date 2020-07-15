package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.ModelKey;

import de.george.lrentnode.classes.eCResourceMeshLoD_PS;
import de.george.lrentnode.util.FileUtil;

public class XlmshLoader implements AssetLoader {
	private static final Logger logger = Logger.getLogger(XimgLoader.class.getName());

	@Override
	public Object load(AssetInfo info) throws IOException {
		if (!(info.getKey() instanceof ModelKey)) {
			throw new IllegalArgumentException("Model assets must be loaded using a ModelKey");
		}

		logger.info("Loading: " + info.getKey().getName());

		int materialSwitch = 0;
		if (info.getKey() instanceof SwitchedModelKey) {
			materialSwitch = ((SwitchedModelKey) info.getKey()).getMaterialSwitch();
		}

		try (InputStream is = info.openStream()) {
			eCResourceMeshLoD_PS lodMesh = FileUtil.openLodMesh(is);
			return info.getManager().loadModel(new SwitchedModelKey(lodMesh.getMeshes().get(0), materialSwitch));
		}
	}
}
