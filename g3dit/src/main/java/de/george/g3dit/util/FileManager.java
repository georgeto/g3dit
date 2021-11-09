package de.george.g3dit.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.teamunify.i18n.I;

import de.george.g3dit.Editor;
import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.config.ReloadableConfigFile;
import de.george.g3dit.gui.dialogs.NavigateTemplateDialog;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.Option;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.CompositeFileLocator;
import de.george.g3utils.io.FileLocator;
import de.george.g3utils.io.RecursiveFileLocator;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

/**
 * Methods are threadsafe if not stated otherwise.
 */
public class FileManager {
	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

	public static final String RP_LOCAL_CONFIG = ".config/g3dit/";
	public static final String RP_PROJECTS_COMPILED = "Projects_compiled/G3_World_01/";
	public static final String RP_STRINGS = "Strings/";
	public static final String RP_TEMPLATES = "Templates/";
	public static final String RP_COMPILED_ANIMATION = "_compiledAnimation/";
	public static final String RP_COMPILED_IMAGE = "_compiledImage/";
	public static final String RP_COMPILED_MATERIAL = "_compiledMaterial/";
	public static final String RP_COMPILED_MESH = "_compiledMesh/";
	public static final String RP_COMPILED_PHYSIC = "_compiledPhysic/";
	public static final String RP_LIGHTMAPS = "Lightmaps/";

	private EditorContext ctx;

	private ConcurrentMap<String, ReloadableConfigFile<?>> configFiles = new ConcurrentHashMap<>();

	public FileManager(EditorContext ctx) {
		this.ctx = ctx;
	}

	public String getPrimaryDataFolder() {
		return ctx.getOptionStore().get(EditorOptions.Path.PRIMARY_DATA_FOLDER);
	}

	public String getPrimaryPath(String relativePath) {
		return getPrimaryDataFolder() + relativePath;
	}

	public List<File> listPrimaryFiles(String relativePath, FileFilter fileFilter) {
		return IOUtils.listFiles(getPrimaryPath(relativePath), fileFilter);
	}

	public String getSecondaryDataFolder() {
		return ctx.getOptionStore().get(EditorOptions.Path.SECONDARY_DATA_FOLDER);
	}

	public String getSecondaryPath(String relativePath) {
		return getSecondaryDataFolder() + relativePath;
	}

	public List<File> listSecondaryFiles(String relativePath, FileFilter fileFilter) {
		return IOUtils.listFiles(getSecondaryPath(relativePath), fileFilter);
	}

	private ImmutableList<String> getPaths(String relativePath) {
		return ImmutableList.of(getPrimaryPath(relativePath), getSecondaryPath(relativePath));
	}

	public List<File> listFiles(String relativePath, FileFilter fileFilter) {
		return IOUtils.listFilesPrioritized(getPaths(relativePath), fileFilter);
	}

	public Optional<File> searchFile(String relativePath, String fileName) {
		return IOUtils.findFirstFile(getPaths(relativePath), file -> file.getName().equalsIgnoreCase(fileName));
	}

	public Optional<File> searchFile(String relativePath) {
		return getPaths(relativePath).stream().map(File::new).filter(File::isFile).findFirst();
	}

	public Optional<File> searchFile(File file) {
		return getRelativePath(file).flatMap(this::searchFile);
	}

	public List<File> listWorldFiles() {
		return listFiles(RP_PROJECTS_COMPILED, IOUtils.archiveFileFilter);
	}

	public ArchiveFileIterator worldFilesIterator() {
		return new ArchiveFileIterator(listWorldFiles());
	}

	public Callable<List<File>> worldFilesCallable() {
		return this::listWorldFiles;
	}

	public List<File> listTemplateFiles() {
		return listFiles(RP_TEMPLATES,
				f -> IOUtils.tpleFileFilter.accept(f) && !f.getName().startsWith("_deleted") && !f.getName().startsWith("Testzeug_"));
	}

	public TemplateFileIterator templateFilesIterator() {
		return new TemplateFileIterator(listTemplateFiles());
	}

	public Callable<List<File>> templateFilesCallable() {
		return this::listTemplateFiles;
	}

	public boolean isInPrimaryDataFolder(File file) {
		String primaryDataFolder = getPrimaryDataFolder().toLowerCase();
		return !primaryDataFolder.isEmpty() && file != null && file.getAbsolutePath().toLowerCase().startsWith(primaryDataFolder);
	}

	public boolean isInSecondaryDataFolder(File file) {
		String secondaryDataFolder = getSecondaryDataFolder().toLowerCase();
		return !secondaryDataFolder.isEmpty() && file != null && file.getAbsolutePath().toLowerCase().startsWith(secondaryDataFolder);
	}

	public Optional<File> moveFromPrimaryToSecondary(File file) {
		String primaryDataFolder = getPrimaryDataFolder();
		String secondaryDataFolder = getSecondaryDataFolder();
		if (!secondaryDataFolder.isEmpty() && isInPrimaryDataFolder(file)) {
			return Optional.of(new File(secondaryDataFolder + file.getAbsolutePath().substring(primaryDataFolder.length())));
		}
		return Optional.empty();
	}

	public Optional<File> moveFromSecondaryToPrimary(File file) {
		String primaryDataFolder = getPrimaryDataFolder().toLowerCase();
		String secondaryDataFolder = getSecondaryDataFolder().toLowerCase();
		if (!primaryDataFolder.isEmpty() && isInSecondaryDataFolder(file)) {
			return Optional.of(new File(primaryDataFolder + file.getAbsolutePath().substring(secondaryDataFolder.length())));
		}
		return Optional.empty();
	}

	public Optional<String> getRelativePath(File file) {
		Option<String> dataFolder = null;
		if (isInPrimaryDataFolder(file)) {
			dataFolder = EditorOptions.Path.PRIMARY_DATA_FOLDER;
		} else if (isInSecondaryDataFolder(file)) {
			dataFolder = EditorOptions.Path.SECONDARY_DATA_FOLDER;
		}

		return dataFolder != null ? Optional.of(file.getAbsolutePath().substring(ctx.getOptionStore().get(dataFolder).length()))
				: Optional.empty();
	}

	public List<String> getDataFolders() {
		ArrayList<String> dataFolders = new ArrayList<>();
		String dataFolder1 = getPrimaryDataFolder();
		if (!dataFolder1.isEmpty()) {
			dataFolders.add(dataFolder1);
		}
		String dataFolder2 = getSecondaryDataFolder();
		if (!dataFolder2.isEmpty()) {
			dataFolders.add(dataFolder2);
		}
		return dataFolders;
	}

	public List<String> getAssetLocations() {
		LinkedHashSet<String> locations = new LinkedHashSet<>();

		BiConsumer<String, Iterable<String>> locationAdder = (loc, ext) -> {
			File file = new File(loc);
			if (file.isDirectory()) {
				locations.add(Joiner.on(';').join(ext) + "#" + file.getAbsolutePath());
			}
		};

		for (String dataFolder : getDataFolders()) {
			locationAdder.accept(dataFolder + RP_COMPILED_MESH, Arrays.asList("xcmsh", "xlmsh"));
			locationAdder.accept(dataFolder + RP_COMPILED_MATERIAL, Arrays.asList("xshmat"));
			locationAdder.accept(dataFolder + RP_COMPILED_IMAGE, Arrays.asList("ximg"));
			locationAdder.accept(dataFolder + RP_COMPILED_ANIMATION, Arrays.asList("xact"));
		}

		return new LinkedList<>(locations);
	}

	public FileLocator getFileLocator(String relativePath, boolean refreshOnCacheMiss) {
		CompositeFileLocator locator = new CompositeFileLocator();

		for (String dataFolder : getDataFolders()) {
			try {
				String rootPath = dataFolder + relativePath;
				if (new File(rootPath).exists()) {
					locator.addLocator(new RecursiveFileLocator(rootPath, refreshOnCacheMiss));
				}
			} catch (IOException e) {
				logger.warn("Error while setting the root path.", e);
			}
		}

		return locator;

	}

	/**
	 * @param relativePath
	 * @return {@code Optional.empty()} if no primary data folder is set.
	 */
	public Optional<File> getLocalConfigFile(String relativePath) {
		String dataFolder = getPrimaryDataFolder();
		if (dataFolder.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(Paths.get(dataFolder, RP_LOCAL_CONFIG, relativePath).toFile());
	}

	public File getDefaultConfigFile(String relativePath) {
		return Paths.get(Editor.EDITOR_CONFIG_FOLDER, "default", relativePath).toFile();
	}

	@SuppressWarnings("unchecked")
	public <T extends ReloadableConfigFile<?>> T getConfigFile(String relativePath, Class<T> configType) {
		return (T) configFiles.computeIfAbsent(relativePath, path -> {
			try {
				return configType.getConstructor(EditorContext.class, String.class).newInstance(ctx, path);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Warn the user if he is to write into the secondary data folder.
	 *
	 * @throws IllegalStateException if this method is called from any Thread other than the Swing
	 *             Event Dispatch Thread
	 */
	public Optional<File> confirmSaveInSecondary(File file) {
		SwingUtils.checkAccessThread();
		if (isInSecondaryDataFolder(file)) {
			int result;
			if (moveFromSecondaryToPrimary(file).map(File::exists).orElse(false)) {
				result = TaskDialogs.choice(ctx.getParentWindow(), I.tr("Do you really want to save the file?"),
						I.trf("''{0}'' is located in the secondary data directory.\n"
								+ "Normally, no changes should be made to files in this directory.\n\n"
								+ "A file already exists in the primary data directory under the same relative path.\n\n"
								+ "Should it be saved anyway?", file.getName()),
						1, new CommandLink(I.tr("Yes"), ""), new CommandLink(I.tr("No"), ""));
			} else {
				result = TaskDialogs.choice(ctx.getParentWindow(), I.tr("Do you really want to save the file?"),
						I.trf("''{0}'' is located in the secondary data directory.\n"
								+ "Normally, no changes should be made to files in this directory.\n\n" + "Should it be saved anyway?",
								file.getName()),
						1, new CommandLink(I.tr("Yes"), ""), new CommandLink(I.tr("No"), ""), new CommandLink(
								I.tr("Primary data directory"), I.tr("Save under the same relative path in the primary data directory.")));
			}

			return switch (result) {
				case 0 -> Optional.of(file);
				case -1, 1 -> Optional.empty();
				case 2 -> moveFromSecondaryToPrimary(file);
				default -> throw new IllegalStateException();
			};
		} else {
			return Optional.of(file);
		}
	}

	public boolean explorePath(File path) {
		try {
			String fileManager = ctx.getOptionStore().get(EditorOptions.Path.FILE_MANAGER);
			if (!fileManager.isEmpty()) {
				Runtime.getRuntime().exec(fileManager.replace("%path", path.getAbsolutePath()));
			} else {
				// Fallback to explorer
				new ProcessBuilder("explorer.exe", "/select,", path.getAbsolutePath()).start();
			}
			return true;
		} catch (Exception e) {
			logger.warn("Failed to open file manager.", e);
			return false;
		}
	}

	public Optional<TemplateFile> selectAndOpenTemplate() {
		NavigateTemplateDialog templateSelect = new NavigateTemplateDialog(ctx.getParentWindow(), ctx);
		if (templateSelect.openAndWasSuccessful()) {
			TemplateCacheEntry entry = templateSelect.getSelectedEntries().get(0);
			try {
				return Optional.of(FileUtil.openTemplate(entry.getFile()));
			} catch (IOException e) {
				logger.warn("Failed to open selected template.", e);
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}

	public List<File> listMeshes() {
		return listFiles(FileManager.RP_COMPILED_MESH, IOUtils.meshFilter);
	}

	public List<File> listStaticMeshes() {
		return listFiles(FileManager.RP_COMPILED_MESH, (file) -> file.getName().endsWith(".xcmsh"));
	}

	public List<File> listLodMeshes() {
		return listFiles(FileManager.RP_COMPILED_MESH, (file) -> file.getName().endsWith(".xlmsh"));
	}

	public List<File> listAnimatedMeshes() {
		return listFiles(FileManager.RP_COMPILED_ANIMATION, (file) -> file.getName().endsWith(".xact"));
	}

	public List<File> listMaterials() {
		return listFiles(FileManager.RP_COMPILED_MATERIAL, (file) -> file.getName().endsWith(".xshmat"));
	}
}
