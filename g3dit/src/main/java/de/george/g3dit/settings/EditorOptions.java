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
				"EditorOptions.Path.PRIMARY_DATA_FOLDER_ALIAS", I.tr("Primäres Data-Verzeichnis Alias"));

		public static final Option<String> PRIMARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, PRIMARY_DATA_FOLDER_ALIAS, I.tr("Primäres Data-Verzeichnis"),
						I.tr("Primäres Data-Verzeichnis auswählen")),
				"EditorOptions.Path.PRIMARY_DATA_FOLDER", I.tr("Primäres Data-Verzeichnis"));

		public static final Option<String> SECONDARY_DATA_FOLDER_ALIAS = new NoHandlerOption<>("",
				"EditorOptions.Path.SECONDARY_DATA_FOLDER_ALIAS", I.tr("Sekundäres Data-Verzeichnis Alias"));

		public static final Option<String> SECONDARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, SECONDARY_DATA_FOLDER_ALIAS, I.tr("Sekundäres Data-Verzeichnis"),
						I.tr("Sekundäres Data-Verzeichnis auswählen")),
				"EditorOptions.Path.SECONDARY_DATA_FOLDER", I.tr("Sekundäres Data-Verzeichnis"));

		public static final Option<Boolean> HIDE_PROJECTS_COMPILED = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Projects_compiled ausblenden")),
				"EditorOptions.Path.HIDE_PROJECTS_COMPILED", I.tr("Projects_compiled ausblenden"));

		public static final Option<String> FILE_MANAGER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Dateimanager"),
						SwingUtils.getMultilineText(I.tr("Folgende Parameter können für das Programm angeben:"),
								I.tr("%path : Die anzuzeigende Datei bzw. das anzuzeigende Verzeichnis")),
						I.tr("Dateimanager auswählen"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.FILE_MANAGER", I.tr("Dateimanager"));

		public static final Option<String> TINY_HEXER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Tiny Hexer (wird benötigt für 'Datei->Öffnen in TinyHexer')"),
						I.tr("Tiny Hexer auswählen"), FileDialogWrapper.createFilter("mpth.exe", "exe")),
				"EditorOptions.Path.TINY_HEXER", I.tr("Tiny Hexer"));

		public static final Option<String> TINY_HEXER_SCRIPT = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Tiny Hexer Script (wenn angegeben wird Script direkt ausgeführt)"),
						I.tr("Tiny Hexer Script auswählen"), FileDialogWrapper.createFilter(I.tr("Tiny Hexer Script"), "mps")),
				"EditorOptions.Path.TINY_HEXER_SCRIPT", I.tr("Tiny Hexer Script"));

		public static final Option<String> TEXT_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Programm zum Vergleich von Textdateien"),
						SwingUtils.getMultilineText(I.tr("Folgende Parameter können für das Programm angeben:"),
								I.tr("%base : Die Ausgangsdatei"), I.tr("%mine : Die modifizierte Datei")),
						I.tr("Programm zum Vergleich von Textdateien auswählen"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.TEXT_COMPARE", I.tr("Programm zum Vergleich von Textdateien"));

		public static final Option<String> BINARY_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, I.tr("Programm zum Vergleich von Binärdateien"),
						SwingUtils.getMultilineText(I.tr("Folgende Parameter können für das Programm angeben:"),
								I.tr("%base : Die Ausgangsdatei"), I.tr("%mine : Die modifizierte Datei")),
						I.tr("Programm zum Vergleich von Binärdateien auswählen"), FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.BINARY_COMPARE", I.tr("Programm zum Vergleich von Binärdateien"));
	}

	public static abstract class Language {
		public static final Option<UiLanguage> UI_LANGUAGE = new LambdaOption<>(UiLanguage.DEFAULT,
				(parent) -> new ComboBoxOptionHandler<>(parent, I.tr("UI language"), ImmutableList.copyOf(UiLanguage.values()), false),
				"EditorOptions.Language.UI_LANGUAGE", I.tr("UI language"));

		public static final Option<String> STRINGTABLE_LANGUAGE = new LambdaOption<>("German",
				(parent) -> new ComboBoxOptionHandler<>(parent, I.tr("Stringtable-Sprache"), ImmutableList.of("German", "English",
						"Italian", "French", "Spanish", "Czech", "Hungarian", "Polish", "Russian", "TRC"), true),
				"EditorOptions.Language.STRINGTABLE_LANGUAGE", I.tr("Stringtable-Sprache"));
	}

	public static abstract class Misc {
		public static final Option<Boolean> MAKE_BACKUP = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Backup anlegen")), "EditorOptions.Misc.MAKE_BACKUP",
				I.tr("Backup anlegen"));

		public static final Option<Boolean> CLEAN_STRINGTABLE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Nicht mehr benötige Einträge aus der Stringtable entfernen")),
				"EditorOptions.Misc.CLEAN_STRINGTABLE", I.tr("Stringtable aufräumen"));

		public static final Option<Boolean> OPTIMIZE_MEMORY_USAGE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent,
						I.tr("Speicherverbrauch optimieren (Laden, Speichern und Schließen von Dateien kann länger dauern)")),
				"EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE", I.tr("Speicherverbrauch optimieren"));

		public static final Option<Boolean> IMPROVE_CHANGE_DETECTION = new LambdaOption<>(true,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Hashbasierte Änderungserkennung beim Schließen von Dateien"),
						SwingUtils.getMultilineText(I.tr("Vermeidet unnötige 'Änderungen speichern'-Dialoge beim Schließen von Dateien."),
								I.tr("Auf langsamen Systemen kann aber dafür das Laden und Schließen von Dateien länger dauern."))),
				"EditorOptions.Misc.IMPROVE_CHANGE_DETECTION", I.tr("Verbesserte Änderungserkennung"));

		public static final Option<Boolean> NAVPATH_DEBUG = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, I.tr("Debuginformationen bei der Berechnung von NavPaths anzeigen")),
				"EditorOptions.Misc.NAVPATH_DEBUG", I.tr("NavPath Debuginformationen"));
	}

	public static abstract class D3View {
		public static final Option<Color> BACKGROUND_COLOR = new LambdaOption<>(Color.DARK_GRAY,
				(parent) -> new ColorChooserOptionHandler(parent, I.tr("Hintergrundfarbe der 3D-Ansicht")),
				"EditorOptions.D3View.BACKGROUND_COLOR", I.tr("Hintergrundfarbe der 3D-Ansicht"));

		public static final Option<Float> AMBIENT_LIGHT_INTENSITY = new LambdaOption<>(0.6f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Intensität der Umgebungsbeleuchtung"), 0, 100, 100, true),
				"EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY", I.tr("Intensität der Umgebungsbeleuchtung"));

		public static final Option<Float> DIRECTIONAL_LIGHT_INTENSITY = new LambdaOption<>(1.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Intensität der gerichteten Beleuchtung"), 0, 200, 100, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY", I.tr("Intensität der gerichteten Beleuchtung"));

		public static final Option<Float> DIRECTIONAL_LIGHT_AZIMUTH = new LambdaOption<>(
				70.0f, (parent) -> new FloatSliderOptionHandler(parent, I.tr("Horizontale Rotation der gerichteten Beleuchtung"), 0, 360,
						1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH", I.tr("Horizontale Rotation der gerichteten Beleuchtung"));

		public static final Option<Float> DIRECTIONAL_LIGHT_INCLINATION = new LambdaOption<>(35.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Vertikale Rotation der gerichteten Beleuchtung"), -90, 90, 1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION", I.tr("Vertikale Rotation der gerichteten Beleuchtung"));

		public static final Option<Float> HORIZONTAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Horizontale Rotation"), 0, 360, 1, true),
				"EditorOptions.D3View.HORIZONTAL_ROTATION", I.tr("Horizontale Rotation"));

		public static final Option<Float> VERTICAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Vertikale Rotation"), -90, 90, 1, true),
				"EditorOptions.D3View.VERTICAL_ROTATION", I.tr("Vertikale Rotation"));

		public static final Option<Float> DISTANCE = new LambdaOption<>(4.0f,
				(parent) -> new FloatSliderOptionHandler(parent, I.tr("Distanz"), 10, 800, 10, true), "EditorOptions.D3View.DISTANCE",
				I.tr("Distanz"));

		public static final Option<String> SCREENSHOT_FOLDER = new LambdaOption<>("",
				(parent) -> new FolderPathOptionHandler(parent, I.tr("Screenshot Verzeichnis"), I.tr("Screenshot Verzeichnis auswählen")),
				"EditorOptions.D3View.SCREENSHOT_FOLDER", I.tr("Screenshot Verzeichnis"));
	}

	public static abstract class MainWindow {
		public static final Option<Dimension> SIZE = new NoHandlerOption<>(new Dimension(700, 500), "EditorOptions.MainWindow.SIZE",
				I.tr("Fenstergröße"));

		public static final Option<Point> LOCATION = new NoHandlerOption<>(new Point(50, 50), "EditorOptions.MainWindow.LOCATION",
				I.tr("Fensterposition"));

		public static final Option<Integer> EXTENDED_STATE = new NoHandlerOption<>(JFrame.NORMAL,
				"EditorOptions.MainWindow.EXTENDED_STATE", I.tr("Fensterstatus"));
	}

	public static abstract class MainMenu {
		public static final Option<List<String>> RECENT_FILES = new NoHandlerOption<>(Collections.emptyList(),
				"EditorOptions.MainMenu.RECENT_FILES", I.tr("Letzte Dateien"));
	}

	public static abstract class TheVoid {
		public static final Option<ThemeInfo> THEME = new LambdaOption<>(ThemeManager.getNativeTheme(), ThemeOptionHandler::new,
				"EditorOptions.TheVoid.THEME", I.tr("Theme"));

		public static final Option<Void> FILE_EXTENSIONS = new LambdaOption<>(null, FileExtensionOptionHandler::new,
				"EditorOptions.TheVoid.FILE_EXTENSIONS", I.tr("Dateiendungen"));
	}

	public static abstract class CheckManager {
		public static final Option<Set<String>> ENABLED_CHECKS = new NoHandlerOption<>(null, "EditorOptions.CheckManager.ENABLED_CHECKS",
				I.tr("Ausgewählte Checks"));
	}
}
