package de.george.g3dit.jme.asset;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;

/**
 * <code>AbsolutePathLocator</code> load assets whose name is an absolute path from disk.
 */
public class AbsolutePathLocator implements AssetLocator {
	@Override
	public void setRootPath(String rootPath) {}

	@Override
	public AssetInfo locate(AssetManager manager, @SuppressWarnings("rawtypes") AssetKey key) {
		Path file = Paths.get(key.getName());
		if (file.isAbsolute() && Files.isRegularFile(file)) {
			return new AssetInfoFile(manager, key, file);
		} else {
			return null;
		}
	}
}
