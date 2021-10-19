package de.george.g3dit.jme.asset;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;

import de.george.g3utils.util.IOUtils;
import one.util.streamex.StreamEx;

/**
 * <code>FileLocator</code> allows you to specify a folder where to look for assets from a list of
 * whitelisted file extensions.
 */
public class G3FileLocator implements AssetLocator {

	private File root;
	private String lastRootPath;
	private List<String> extensions;
	private Map<String, String> fileTable = new HashMap<>();

	@Override
	public void setRootPath(String rootPath) {
		Objects.requireNonNull(rootPath);

		if (lastRootPath != null && lastRootPath.equals(rootPath)) {
			return;
		}

		List<String> splitted = Splitter.on("#").limit(2).splitToList(rootPath);
		if (splitted.size() != 2) {
			throw new IllegalArgumentException("Given root path \"" + rootPath + "\" has an invalid syntax");
		}

		extensions = StreamEx.of(Splitter.on(";").splitToList(splitted.get(0))).map(String::toLowerCase).toList();

		try {
			root = new File(splitted.get(1)).getCanonicalFile();
			if (!root.exists()) {
				throw new IllegalArgumentException("Given root path \"" + root + "\" does not exist");
			}

			if (!root.isDirectory()) {
				throw new IllegalArgumentException("Given root path \"" + root + "\" is not a directory");
			}

			lastRootPath = rootPath;

			fileTable.clear();
			List<File> files = IOUtils.listFiles(root.getAbsolutePath(),
					(f) -> extensions.contains(Files.getFileExtension(f.getName()).toLowerCase()));
			for (File file : files) {
				fileTable.put(file.getName().toLowerCase(), file.getCanonicalPath());
			}

		} catch (IOException ex) {
			throw new AssetLoadException("Root path is invalid", ex);
		}
	}

	@Override
	public AssetInfo locate(AssetManager manager, @SuppressWarnings("rawtypes") AssetKey key) {
		String name = key.getName();

		if (!extensions.contains(Files.getFileExtension(name).toLowerCase())) {
			return null;
		}

		File file = new File(root, name);
		if (!file.exists()) {
			String filePath = fileTable.get(name.toLowerCase());
			if (filePath != null) {
				file = new File(filePath);
			}
		}

		if (!file.exists()) {
			file = IOUtils.findFirstFile(root.getAbsolutePath(), (ftf) -> ftf.getName().equalsIgnoreCase(name)).orElse(null);
			if (file == null) {
				return null;
			}
		}

		if (file.exists() && file.isFile()) {
			try {
				// Now, check asset name requirements
				String canonical = file.getCanonicalPath();
				String absolute = file.getAbsolutePath();
				if (!canonical.endsWith(absolute)) {
					throw new AssetNotFoundException(
							"Asset name doesn't match requirements.\n" + "\"" + canonical + "\" doesn't match \"" + absolute + "\"");
				}
			} catch (IOException ex) {
				throw new AssetLoadException("Failed to get file canonical path " + file, ex);
			}

			return new AssetInfoFile(manager, key, file);
		} else {
			return null;
		}
	}

}
