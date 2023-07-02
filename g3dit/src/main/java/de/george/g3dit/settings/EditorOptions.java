package de.george.g3dit.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import com.google.common.collect.ImmutableList;
import com.teamunify.i18n.I;

import de.george.g3dit.Editor.UiLanguage;
import de.george.g3dit.gui.theme.ThemeInfo;
import de.george.g3dit.gui.theme.ThemeManager;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.gui.SwingUtils;

public abstract class EditorOptions {
	public static abstract class Path {
		public static final Option<String> PRIMARY_DATA_FOLDER_ALIAS = new NoHandlerOption<>("",
				"EditorOptions.Path.PRIMARY_DATA_FOLDER_ALIAS", I.tr("Alias for primary data directory"));

		public static final Option<String> PRIMARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, PRIMARY_DATA_FOLDER_ALIAS, I.tr("Primary data directory"),
						I.tr("Select primary data directory")),
				"EditorOptions.Path.PRIMARY_DATA_FOLDER", I.tr("Primary data directory"));

		public static final Option<String> SECONDARY_DATA_FOLDER_ALIAS = new NoHandlerOption<>("",
				"EditorOptions.Path.SECONDARY_DATA_FOLDER_ALIAS", I.tr("Alias for secondary data directory"));

		public static final Option<String> SECONDARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, SECONDARY_DATA_FOLDER_ALIAS, I.tr("Secondary data directory"),
						I.tr("Select secondary data directory")),
				"EditorOptions.Path.SECONDARY_DATA_FOLDER", I.tr("Secondary data directory"));

		public static final Option<Boolean> HIDE_PROJECTS_COMPILED = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Hide Projects_compiled")), "EditorOptions.Path.HIDE_PROJECTS_COMPILED",
				I.tr("Hide Projects_compiled"));

		public static final Option<String> FILE_MANAGER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("File manager"),
						SwingUtils.getMultilineText(I.tr("The following parameters can be specified for the program:"),
								I.tr("%path : The file or directory to be shown")),
						I.tr("Select file manager"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.FILE_MANAGER", I.tr("File manager"));

		public static final Option<String> TINY_HEXER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Tiny Hexer (needed for 'File->Open in TinyHexer')"),
						I.tr("Select Tiny Hexer"), FileDialogWrapper.createFilter("mpth.exe", "exe")),
				"EditorOptions.Path.TINY_HEXER", I.tr("Tiny Hexer"));

		public static final Option<String> TINY_HEXER_SCRIPT = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Tiny Hexer Script (if specified the script is executed directly)"),
						I.tr("Select Tiny Hexer Script"), FileDialogWrapper.createFilter(I.tr("Tiny Hexer Script"), "mps")),
				"EditorOptions.Path.TINY_HEXER_SCRIPT", I.tr("Tiny Hexer Script"));

		public static final Option<String> TEXT_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Program for comparing text files"),
						SwingUtils.getMultilineText(I.tr("The following parameters can be specified for the program:"),
								I.tr("%base : The original file"), I.tr("%mine : The modified file")),
						I.tr("Select program for comparing text files"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.TEXT_COMPARE", I.tr("Program for comparing text files"));

		public static final Option<String> BINARY_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Program for comparing binary files"),
						SwingUtils.getMultilineText(I.tr("The following parameters can be specified for the program:"),
								I.tr("%base : The original file"), I.tr("%mine : The modified file")),
						I.tr("Select program for comparing binary files"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.BINARY_COMPARE", I.tr("Program for comparing binary files"));
	}

	public static abstract class Language {
		public static final Option<UiLanguage> UI_LANGUAGE = new LambdaOption<>(UiLanguage.DEFAULT,
				(parent) -> new ComboBoxOptionHandler<>(parent, I.tr("UI language"), ImmutableList.copyOf(UiLanguage.values()), false),
				"EditorOptions.Language.UI_LANGUAGE", I.tr("UI language"));

		public static final Option<String> STRINGTABLE_LANGUAGE = new LambdaOption<>("German",
				(parent) -> new ComboBoxOptionHandler<>(parent, I.tr("Stringtable language"), ImmutableList.of("German", "English",
						"Italian", "French", "Spanish", "Czech", "Hungarian", "Polish", "Russian", "TRC"), true),
				"EditorOptions.Language.STRINGTABLE_LANGUAGE", I.tr("Stringtable language"));
	}

	public static abstract class Misc {
		public static final Option<Boolean> MAKE_BACKUP = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Make backup")), "EditorOptions.Misc.MAKE_BACKUP", I.tr("Make backup"));

		public static final Option<Boolean> CLEAN_STRINGTABLE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Removed unused entries from the stringtable")),
				"EditorOptions.Misc.CLEAN_STRINGTABLE", I.tr("Clean up string table"));

		public static final Option<Boolean> OPTIMIZE_MEMORY_USAGE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent,
						I.tr("Optimize memory consumption (loading, saving and closing files may take longer)")),
				"EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE", I.tr("Optimize memory consumption"));

		public static final Option<Boolean> IMPROVE_CHANGE_DETECTION = new LambdaOption<>(true,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Hash-based change detection when closing files"),
						SwingUtils.getMultilineText(I.tr("Avoids unnecessary 'Save changes' dialogs when closing files."),
								I.tr("On slow systems, however, loading and closing files can take longer."))),
				"EditorOptions.Misc.IMPROVE_CHANGE_DETECTION", I.tr("Improved change detection"));

		public static final Option<Boolean> NAVPATH_DEBUG = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Show debug information when calculating NavPaths")),
				"EditorOptions.Misc.NAVPATH_DEBUG", I.tr("NavPath debug information"));

		public static final Option<Boolean> DEVELOPER_MODE = new LambdaOption<>(false, (parent) -> new BooleanOptionHandler(parent,
				I.tr("Enable developer mode"), I.tr("Show debug developer options and menus.")), "EditorOptions.Misc.DEVELOPER_MODE",
				I.tr("Developer mode"));
	}

	public static abstract class D3View {
		public static final Option<Color> BACKGROUND_COLOR = new LambdaOption<>(Color.DARK_GRAY,
				(parent) -> new ColorChooserOptionHandler(parent, I.tr("Background color of the 3D view")),
				"EditorOptions.D3View.BACKGROUND_COLOR", I.tr("Background color of the 3D view"));

		public static final Option<Float> AMBIENT_LIGHT_INTENSITY = new LambdaOption<>(0.6f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Intensity of ambient lighting"), 0, 100, 100, true),
				"EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY", I.tr("Intensity of ambient lighting"));

		public static final Option<Float> DIRECTIONAL_LIGHT_INTENSITY = new LambdaOption<>(1.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Intensity of directional lighting"), 0, 200, 100, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY", I.tr("Intensity of directional lighting"));

		public static final Option<Float> DIRECTIONAL_LIGHT_AZIMUTH = new LambdaOption<>(70.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Horizontal rotation of directional lighting"), 0, 360, 1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH", I.tr("Horizontal rotation of directional lighting"));

		public static final Option<Float> DIRECTIONAL_LIGHT_INCLINATION = new LambdaOption<>(35.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Vertical rotation of directional lighting"), -90, 90, 1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION", I.tr("Vertical rotation of directional lighting"));

		public static final Option<Float> HORIZONTAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Horizontal rotation"), 0, 360, 1, true),
				"EditorOptions.D3View.HORIZONTAL_ROTATION", I.tr("Horizontal rotation"));

		public static final Option<Float> VERTICAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Vertical rotation"), -90, 90, 1, true),
				"EditorOptions.D3View.VERTICAL_ROTATION", I.tr("Vertical rotation"));

		public static final Option<Float> DISTANCE = new LambdaOption<>(4.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Distance"), 10, 800, 10, true), "EditorOptions.D3View.DISTANCE",
				I.tr("Distance"));

		public static final Option<String> SCREENSHOT_FOLDER = new LambdaOption<>("",
				(parent) -> new FolderPathOptionHandler(parent, I.tr("Screenshot directory"), I.tr("Select screenshot directory")),
				"EditorOptions.D3View.SCREENSHOT_FOLDER", I.tr("Screenshot directory"));
	}

	public static abstract class MainWindow {
		public static final Option<Dimension> SIZE = new NoHandlerOption<>(new Dimension(700, 500), "EditorOptions.MainWindow.SIZE",
				I.tr("Window size"));

		public static final Option<Point> LOCATION = new NoHandlerOption<>(new Point(50, 50), "EditorOptions.MainWindow.LOCATION",
				I.tr("Window position"));

		public static final Option<Integer> EXTENDED_STATE = new NoHandlerOption<>(JFrame.NORMAL,
				"EditorOptions.MainWindow.EXTENDED_STATE", I.tr("Window status"));
	}

	public static abstract class MainMenu {
		public static final Option<List<String>> RECENT_FILES = new NoHandlerOption<>(Collections.emptyList(),
				"EditorOptions.MainMenu.RECENT_FILES", I.tr("Recent files"));
	}

	public static abstract class TheVoid {
		public static final Option<ThemeInfo> THEME = new LambdaOption<>(ThemeManager.getNativeTheme(), ThemeOptionHandler::new,
				"EditorOptions.TheVoid.THEME", I.tr("Theme"));

		public static final Option<Void> FILE_EXTENSIONS = new LambdaOption<>(null, FileExtensionOptionHandler::new,
				"EditorOptions.TheVoid.FILE_EXTENSIONS", I.tr("File extensions"));
	}

	public static abstract class CheckManager {
		public static final Option<Set<String>> ENABLED_CHECKS = new NoHandlerOption<>(null, "EditorOptions.CheckManager.ENABLED_CHECKS",
				I.tr("Selected checks"));
	}
}
