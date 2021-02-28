package de.george.g3dit;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ObjectArrays;
import com.google.common.io.Files;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.cache.EntityCache.EntityCacheEntry;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.jme.EntityRenderer;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.AssetResolver.AbstractAsset;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Holder;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class EditorCli {
	private static final Logger logger = LoggerFactory.getLogger(EditorCli.class);

	public static final int EXIT_CODE_OK = 0;
	public static final int EXIT_CODE_ERROR = 1;

	private static final class ArgumentParserHolder {
		private static final ArgumentParser INSTANCE = createParser();
	}

	private static final ArgumentParser getParser() {
		return ArgumentParserHolder.INSTANCE;
	}

	private static final ArgumentParser createParser() {
		ArgumentParser parser = ArgumentParsers.newFor("g3dit").build().defaultHelp(true).description("Editor for Gothic 3");
		Subparsers subparsers = parser.addSubparsers().title("subcommands").description("valid subcommands");

		Subparser openParser = subparsers.addParser("open").help("open files").defaultHelp(true).setDefault("subparser_handler",
				(Function<EditorCli, Boolean>) EditorCli::handleOpen);
		openParser.addArgument("file").metavar("file").nargs("*").help("Files to open");

		Subparser diffParser = subparsers.addParser("diff").help("diff files").defaultHelp(true).setDefault("subparser_handler",
				(Function<EditorCli, Boolean>) EditorCli::handleDiff);
		diffParser.description("diff files");
		diffParser.addArgument("base").required(true);
		diffParser.addArgument("mine").required(true);

		Subparser cacheParser = subparsers.addParser("cache").help("control cache").defaultHelp(true).setDefault("subparser_handler",
				(Function<EditorCli, Boolean>) EditorCli::handleCache);
		cacheParser.description("control cache");
		cacheParser.addArgument("--update").choices("all", "template", "entity").required(true);

		Subparser renderParser = subparsers.addParser("render").help("render templates and meshes").defaultHelp(true)
				.setDefault("subparser_handler", (Function<EditorCli, Boolean>) EditorCli::handleRender);
		renderParser.description("render templates and meshes");
		renderParser.addArgument("-o", "--out").required(true);
		renderParser.addArgument("-w", "--width").type(int.class).setDefault(1920);
		renderParser.addArgument("-H", "--height").type(int.class).setDefault(1080);
		renderParser.addArgument("-rh", "--rot-horiz").type(float.class).help("horizontal rotation of the camera");
		renderParser.addArgument("-rv", "--rot-vert").type(float.class).help("vertical rotation of the camera");
		MutuallyExclusiveGroup renderDistanceGroup = renderParser.addMutuallyExclusiveGroup("distance");
		renderDistanceGroup.addArgument("-d", "--distance").type(float.class).help("distance of the camera from the object");
		renderDistanceGroup.addArgument("-dr", "--distance-relative").type(float.class)
				.help("distance of the camera from the object in relation to its size");
		renderParser.addArgument("-orx", "--obj-rot-x").type(float.class).help("rotation of the object on the x axis").setDefault(0);
		renderParser.addArgument("-ory", "--obj-rot-y").type(float.class).help("rotation of the object on the y axis").setDefault(0);
		renderParser.addArgument("-orz", "--obj-rot-z").type(float.class).help("rotation of the object on the z axis").setDefault(0);
		renderParser.addArgument("-lri", "--light-rot-inclination").type(float.class);
		renderParser.addArgument("-lra", "--light-rot-azimuth").type(float.class);
		renderParser.addArgument("-ms", "--material-switch").type(int.class).setDefault(0);
		MutuallyExclusiveGroup renderSourceGroup = renderParser.addMutuallyExclusiveGroup("source").required(true);
		renderSourceGroup.addArgument("-s", "--source").help("name of template or mesh (xact/xcmsh)");
		renderSourceGroup.addArgument("-sg", "--source-guid").help("guid of entity or template (must be in cache)");
		renderSourceGroup.addArgument("-sf", "--source-file").help("file path of template or mesh");

		Subparser assetParser = subparsers.addParser("asset").help("show asset info for templates and meshes").defaultHelp(true)
				.setDefault("subparser_handler", (Function<EditorCli, Boolean>) EditorCli::handleAsset);
		assetParser.addArgument("-ms", "--material-switch").type(int.class).setDefault(0);
		MutuallyExclusiveGroup assetSourceGroup = assetParser.addMutuallyExclusiveGroup("source").required(true);
		assetSourceGroup.addArgument("-s", "--source").help("name of template, mesh (xact/xcmsh) or material (xshmat)");
		assetSourceGroup.addArgument("-sg", "--source-guid").help("guid of entity or template (must be in cache)");
		assetSourceGroup.addArgument("-sf", "--source-file").help("file path of template or mesh");

		Subparser lookupParser = subparsers.addParser("lookup").help("lookup entities and templates").defaultHelp(true)
				.setDefault("subparser_handler", (Function<EditorCli, Boolean>) EditorCli::handleLookup);
		MutuallyExclusiveGroup lookupIdentifierGroup = lookupParser.addMutuallyExclusiveGroup("identifier").required(true);
		lookupIdentifierGroup.addArgument("-g", "--guid").help("Lookup entity or template by guid");
		lookupIdentifierGroup.addArgument("-t", "--template").help("Lookup template guid by name");
		lookupIdentifierGroup.addArgument("-e", "--entity").help("Lookup entity guid by name");
		return parser;
	}

	private Editor editor;
	private File workingDir;
	private Namespace parseResult;
	private PrintWriter writer;

	public EditorCli(Editor editor, File workingDir, PrintWriter writer) {
		this.editor = editor;
		this.workingDir = workingDir;
		this.writer = writer;
	}

	public boolean processCommandLine(String[] args, boolean onlyParse) {
		ArgumentParser parser = getParser();
		try {
			if (args.length == 0) {
				// Show help when in console mode.
				if (isInConsoleMode()) {
					parser.printHelp(writer);
					return false;
				} else {
					// Do nothing if no args are passed.
					return true;
				}
			}

			// Allow opening of files without specifying the open subcommand for backwards
			// compatiblity.
			if (allValidFiles(args)) {
				args = ObjectArrays.concat("open", args);
			}

			parseResult = parser.parseArgs(args);
			if (onlyParse) {
				return true;
			}
			return parseResult.<Function<EditorCli, Boolean>>get("subparser_handler").apply(this);
		} catch (HelpScreenException e) {
			// Just exit.
			return false;
		} catch (ArgumentParserException e) {
			// Log error and exit.
			logger.error("Parsing failed.  Reason: {}", e.getMessage());
			parser.handleError(e, writer);

			return false;
		} finally {
			writer.flush();
		}
	}

	public static final boolean isInConsoleMode() {
		return Objects.equals(System.getProperty("g3dit.mode"), "console");
	}

	private boolean handleOpen() {
		for (String filePath : parseResult.<String>getList("file")) {
			File file = parseFileArg(filePath);
			if (file.exists() && file.isFile()) {
				editor.openFile(file);
			}
		}
		return true;
	}

	private boolean handleDiff() {
		File baseFile = parseFileArg(parseResult.getString("base"));
		File mineFile = parseFileArg(parseResult.getString("mine"));
		editor.diffFiles(baseFile, mineFile);
		return true;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean handleCache() {
		List<Class> caches = new ArrayList<>();
		switch (parseResult.getString("update")) {
			case "all":
				caches.add(TemplateCache.class);
				caches.add(EntityCache.class);
				break;
			case "template":
				caches.add(TemplateCache.class);
				break;
			case "entity":
				caches.add(EntityCache.class);
				break;
		}
		for (Class cache : caches) {
			writer.printf("Update %s...", cache.getSimpleName()).flush();
			editor.getCacheManager().createCache(cache);
			writer.println(" done.");
		}
		return true;
	}

	private void setupScene(EntityRenderer r) {
		// Apply default settings
		r.applySettings();

		r.setResolution(parseResult.getInt("width"), parseResult.getInt("height"));

		if (hasArg("rot_horiz")) {
			r.appTask(app -> app.setHorizontalRotationDeg(parseResult.getFloat("rot_horiz")));
		}

		if (hasArg("rot_vert")) {
			r.appTask(app -> app.setVerticalRotationDeg(parseResult.getFloat("rot_vert")));
		}

		if (hasArg("distance")) {
			r.appTask(app -> app.setAbsoluteDistance(parseResult.getFloat("distance")));
		}

		if (hasArg("distance_relative")) {
			r.appTask(app -> app.setRelativeDistance(parseResult.getFloat("distance_relative")));
		}

		if (hasArg("obj_rot_x") && hasArg("obj_rot_y") && hasArg("obj_rot_z")) {
			r.appTask(app -> app.setObjectRotationDeg(parseResult.getFloat("obj_rot_x"), parseResult.getFloat("obj_rot_y"),
					parseResult.getFloat("obj_rot_z")));
		}

		if (hasArg("light_rot_inclination") || hasArg("light_rot_azimuth")) {
			float inclination = Optional.ofNullable(parseResult.getFloat("light_rot_inclination"))
					.orElseGet(() -> editor.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION));
			float azimuth = Optional.ofNullable(parseResult.getFloat("light_rot_azimuth"))
					.orElseGet(() -> editor.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH));
			r.appTask(app -> app.setLightDirection(inclination, azimuth));
		}
	}

	private boolean loadTemplate(Optional<TemplateCacheEntry> tpleCacheEntry, String sourceType, String description,
			Consumer<eCEntity> returnEntity) {
		if (tpleCacheEntry.isPresent()) {
			return loadTemplate(tpleCacheEntry.get().getFile(), returnEntity);
		} else {
			writer.printf("Unable to find template with %s %s.", sourceType, description).println();
			return false;
		}
	}

	public boolean loadTemplate(File tpleFile, Consumer<eCEntity> returnEntity) {
		Optional<TemplateFile> tple = FileUtil.openTemplateSafe(tpleFile);
		if (tple.isPresent()) {
			returnEntity.accept(tple.get().getReferenceHeader());
			return true;
		} else {
			writer.printf("Unable to load template %s.", tpleFile.getName()).println();
			return false;
		}
	}

	private boolean getSource(Consumer<String> returnMesh, Consumer<String> returnMaterial, Consumer<eCEntity> returnEntity) {
		if (hasArg("source")) {
			String sourceName = parseResult.getString("source");
			String sourceNameExt = Files.getFileExtension(sourceName);
			switch (sourceNameExt.toLowerCase()) {
				case "xcmsh":
				case "xact":
					returnMesh.accept(sourceName);
					return true;
				case "xshmat":
					returnMaterial.accept(sourceName);
					return true;
				case "tple": {
					// .tple -> fileName
					Optional<TemplateCacheEntry> tpleCacheEntry = Caches.template(editor).getAllEntities()
							.filter(t -> t.getFile().getName().equalsIgnoreCase(sourceName)).findFirst();
					return loadTemplate(tpleCacheEntry, "file name ", sourceName, returnEntity);
				}
				case "": {
					// no extension -> template
					Optional<TemplateCacheEntry> tpleCacheEntry = Caches.template(editor).getEntryByName(sourceName);
					return loadTemplate(tpleCacheEntry, "name", sourceName, returnEntity);
				}
				default:
					writer.printf("Unsupported file extension %s.", sourceNameExt).println();
					return false;
			}
		} else if (hasArg("source_guid")) {
			String guid = getGuidArg("source_guid");
			if (guid == null) {
				return false;
			}

			Optional<TemplateCacheEntry> tpleCacheEntry = Caches.template(editor).getEntryByGuid(guid);
			if (tpleCacheEntry.isPresent()) {
				return loadTemplate(tpleCacheEntry, "guid", guid, returnEntity);
			} else {
				Optional<FileDescriptor> entityFile = Caches.entity(editor).getFile(guid);
				if (entityFile.isPresent()) {
					Optional<eCEntity> entity = FileUtil.openArchiveSafe(entityFile.get().getPath(), false, false)
							.flatMap(a -> a.getEntityByGuid(guid));
					if (entity.isPresent()) {
						returnEntity.accept(entity.get());
						return true;
					} else {
						writer.printf("Unable to load entity from file %s.", entityFile.get().getPath().getName()).println();
						return false;
					}
				} else {
					writer.printf("Unable to find template or entity with guid %s.", guid).println();
					return false;
				}
			}
		} else {
			File sourceFile = parseFileArg(parseResult.getString("source_file"));
			if (!sourceFile.exists()) {
				writer.printf("File '%s' does not exist.", sourceFile.getAbsolutePath()).println();
				return false;
			}

			String sourceFileExt = Files.getFileExtension(sourceFile.getName());
			switch (sourceFileExt.toLowerCase()) {
				case "xcmsh":
				case "xact":
					returnMesh.accept(sourceFile.getAbsolutePath());
					return true;
				case "xshmat":
					returnMaterial.accept(sourceFile.getAbsolutePath());
					return true;
				case "tple": {
					return loadTemplate(sourceFile, returnEntity);
				}
				default:
					writer.printf("Unsupported file extension '%s'.", sourceFileExt).println();
					return false;
			}
		}
	}

	public String getGuidArg(String argName) {
		String guid = GuidUtil.parseGuid(parseResult.getString(argName));
		if (guid == null) {
			writer.printf("%s is not a valid guid.", parseResult.getString("source_guid")).println();
		}
		return guid;
	}

	private boolean handleRender() {
		File outFile = parseFileArg(parseResult.getString("out"));
		String format = Files.getFileExtension(outFile.getName());
		if (!format.equals("png") && !format.equals("jpg")) {
			format = "png";
		}

		Holder<String> meshHolder = new Holder<>();
		Holder<String> materialHolder = new Holder<>();
		Holder<eCEntity> entityHolder = new Holder<>();
		boolean success = false;
		if (getSource(meshHolder::hold, materialHolder::hold, entityHolder::hold)) {
			Consumer<EntityRenderer> objectProvider = null;
			if (meshHolder.held() != null) {
				objectProvider = r -> r.showMesh(meshHolder.held(), getMaterialSwitch());
			} else if (entityHolder.held() != null) {
				objectProvider = r -> r.showEntity(entityHolder.held());
			}

			if (objectProvider != null) {
				EntityRenderer.getInstance(editor).renderScene(outFile, format, this::setupScene, objectProvider);
			}
		}

		return success;
	}

	private boolean handleAsset() {
		Holder<String> meshHolder = new Holder<>();
		Holder<String> materialHolder = new Holder<>();
		Holder<eCEntity> entityHolder = new Holder<>();

		if (getSource(meshHolder::hold, materialHolder::hold, entityHolder::hold)) {
			AssetResolver assetResolver = AssetResolver.with(editor).supportAbsolutePaths().build();
			List<AbstractAsset> resolvedAssets = new ArrayList<>();
			if (meshHolder.held() != null) {
				resolvedAssets.add(assetResolver.resolveMesh(meshHolder.held(), getMaterialSwitch()));
			} else if (materialHolder.held() != null) {
				resolvedAssets.add(assetResolver.resolveMaterial(materialHolder.held(), getMaterialSwitch()));
			} else if (entityHolder.held() != null) {
				resolvedAssets.addAll(assetResolver.resolveEntity(entityHolder.held()));
			}

			for (int i = 0; i < resolvedAssets.size(); i++) {
				writer.println(resolvedAssets.get(i).print());
				if (i < resolvedAssets.size()) {
					writer.println();
					writer.println();
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean handleLookup() {
		if (hasArg("guid")) {
			String guid = getGuidArg("guid");
			if (guid == null) {
				return false;
			}

			Optional<TemplateCacheEntry> tple = Caches.template(editor).getEntryByGuid(guid, true);
			if (tple.isPresent()) {
				writer.printf("Template: %s", tple.get().getName()).println();
				return true;
			} else {
				Optional<EntityCacheEntry> entity = Caches.entity(editor).getEntry(guid);
				if (entity.isPresent()) {
					writer.printf("Entity: %s in file %s", entity.get().getName(), entity.get().getFile().getPath().getName()).println();
					return true;
				} else {
					return false;
				}
			}
		} else if (hasArg("template")) {
			Optional<TemplateCacheEntry> tple = Caches.template(editor).getEntryByName(parseResult.getString("template"));
			if (tple.isPresent()) {
				writer.println(tple.get().getGuid());
				return true;
			} else {
				return false;
			}
		} else if (hasArg("entity")) {
			Optional<String> entity = Caches.entity(editor).getGuidByUniqueName(parseResult.getString("entity"));
			if (entity.isPresent()) {
				writer.println(entity.get());
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean hasArg(String dest) {
		return parseResult.getAttrs().get(dest) != null;
	}

	private boolean allValidFiles(String[] args) {
		for (String filePath : args) {
			File file = parseFileArg(filePath);
			if (!file.exists() || !file.isFile()) {
				return false;
			}
		}
		return args.length > 0;
	}

	private File parseFileArg(String filePath) {
		File file = new File(filePath);
		if (workingDir != null && !file.isAbsolute()) {
			file = new File(workingDir, filePath);
		}
		return file;
	}

	private int getMaterialSwitch() {
		return parseResult.get("material_switch");
	}
}
