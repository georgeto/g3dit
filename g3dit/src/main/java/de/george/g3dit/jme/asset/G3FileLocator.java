package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Splitter;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;

import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;
import one.util.streamex.StreamEx;

/**
 * <code>FileLocator</code> allows you to specify a folder where to look for assets from a list of
 * whitelisted file extensions.
 */
public class G3FileLocator implements AssetLocator {

	private Path root;
	private String lastRootPath;
	private List<String> extensions;
	private Map<String, Path> fileTable = new HashMap<>();

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
			Path newRoot = Paths.get(splitted.get(1));
			if (!Files.exists(newRoot)) {
				throw new IllegalArgumentException("Given root path \"" + newRoot + "\" does not exist");
			}

			if (!Files.isDirectory(newRoot)) {
				throw new IllegalArgumentException("Given root path \"" + newRoot + "\" is not a directory");
			}

			root = newRoot.toRealPath();
			lastRootPath = rootPath;

			fileTable.clear();
			List<Path> files = IOUtils.listFiles(root, (f) -> extensions.contains(FilesEx.getFileExtension(f).toLowerCase()));
			for (Path file : files) {
				fileTable.put(FilesEx.getFileNameLowerCase(file), file);
			}

		} catch (IOException ex) {
			throw new AssetLoadException("Root path is invalid", ex);
		}
	}

	@Override
	public AssetInfo locate(AssetManager manager, @SuppressWarnings("rawtypes") AssetKey key) {
		String name = key.getName();

		if (!extensions.contains(FilesEx.getFileExtension(name).toLowerCase())) {
			return null;
		}

		Path file = root.resolve(name);
		if (!Files.exists(file)) {
			file = fileTable.get(name.toLowerCase());
		}

		if (file == null || !Files.exists(file)) {
			file = IOUtils.findFirstFile(root, (ftf) -> FilesEx.getFileName(ftf).equalsIgnoreCase(name)).orElse(null);
			if (file == null) {
				return null;
			}
		}

		if (Files.isRegularFile(file)) {
			try {
				// Now, check asset name requirements
				String canonical = file.toRealPath().toString();
				String absolute = FilesEx.getAbsolutePath(file);
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
