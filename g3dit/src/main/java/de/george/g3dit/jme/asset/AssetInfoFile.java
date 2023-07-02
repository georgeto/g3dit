package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;

class AssetInfoFile extends AssetInfo {
	private Path file;

	public AssetInfoFile(AssetManager manager, AssetKey<?> key, Path file) {
		super(manager, key);
		this.file = file;
	}

	@Override
	public InputStream openStream() {
		try {
			return Files.newInputStream(file);
		} catch (IOException ex) {
			// NOTE: Can still happen even if file.exists() is true, e.g.
			// permissions issue and similar
			throw new AssetLoadException("Failed to open file: " + file, ex);
		}
	}
}
