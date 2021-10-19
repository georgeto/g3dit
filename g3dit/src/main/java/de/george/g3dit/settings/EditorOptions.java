package de.george.g3dit.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import com.google.common.collect.ImmutableList;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.gui.SwingUtils;

public abstract class EditorOptions {
	public static abstract class Path {
		public static final Option<String> PRIMARY_DATA_FOLDER_ALIAS = new NoHandlerOption<>("",
				"EditorOptions.Path.PRIMARY_DATA_FOLDER_ALIAS", "Primäres Data-Verzeichnis Alias");

		public static final Option<String> PRIMARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, PRIMARY_DATA_FOLDER_ALIAS, "Primäres Data-Verzeichnis",
						"Primäres Data-Verzeichnis auswählen"),
				"EditorOptions.Path.PRIMARY_DATA_FOLDER", "Primäres Data-Verzeichnis");

		public static final Option<String> SECONDARY_DATA_FOLDER_ALIAS = new NoHandlerOption<>("",
				"EditorOptions.Path.SECONDARY_DATA_FOLDER_ALIAS", "Sekundäres Data-Verzeichnis Alias");

		public static final Option<String> SECONDARY_DATA_FOLDER = new LambdaOption<>("",
				(parent) -> new AliasFolderPathOptionHandler(parent, SECONDARY_DATA_FOLDER_ALIAS, "Sekundäres Data-Verzeichnis",
						"Sekundäres Data-Verzeichnis auswählen"),
				"EditorOptions.Path.SECONDARY_DATA_FOLDER", "Sekundäres Data-Verzeichnis");

		public static final Option<Boolean> HIDE_PROJECTS_COMPILED = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, "Projects_compiled ausblenden"), "EditorOptions.Path.HIDE_PROJECTS_COMPILED",
				"Projects_compiled ausblenden");

		public static final Option<String> FILE_MANAGER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, "Dateimanager",
						SwingUtils.getMultilineText("Folgende Parameter können für das Programm angeben:",
								"%path : Die anzuzeigende Datei bzw. das anzuzeigende Verzeichnis"),
						"Dateimanager auswählen", FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.FILE_MANAGER", "Dateimanager");

		public static final Option<String> TINY_HEXER = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, "Tiny Hexer (wird benötigt für 'Datei->Öffnen in TinyHexer')",
						"Tiny Hexer auswählen", FileDialogWrapper.createFilter("mpth.exe", "exe")),
				"EditorOptions.Path.TINY_HEXER", "Tiny Hexer");

		public static final Option<String> TINY_HEXER_SCRIPT = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, "Tiny Hexer Script (wenn angegeben wird Script direkt ausgeführt)",
						"Tiny Hexer Script auswählen", FileDialogWrapper.createFilter("Tiny Hexer Script", "mps")),
				"EditorOptions.Path.TINY_HEXER_SCRIPT", "Tiny Hexer Script");

		public static final Option<String> TEXT_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, "Programm zum Vergleich von Textdateien",
						SwingUtils.getMultilineText("Folgende Parameter können für das Programm angeben:", "%base : Die Ausgangsdatei",
								"%mine : Die modifizierte Datei"),
						"Programm zum Vergleich von Textdateien auswählen", FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.TEXT_COMPARE", "Programm zum Vergleich von Textdateien");

		public static final Option<String> BINARY_COMPARE = new LambdaOption<>("",
				(parent) -> new FilePathOptionHandler(parent, "Programm zum Vergleich von Binärdateien",
						SwingUtils.getMultilineText("Folgende Parameter können für das Programm angeben:", "%base : Die Ausgangsdatei",
								"%mine : Die modifizierte Datei"),
						"Programm zum Vergleich von Binärdateien auswählen", FileDialogWrapper.createFilter("", "exe")),
				"EditorOptions.Path.BINARY_COMPARE", "Programm zum Vergleich von Binärdateien");
	}

	public static abstract class Language {
		public static final Option<String> STRINGTABLE_LANGUAGE = new LambdaOption<>("German",
				(parent) -> new ComboBoxOptionHandler<>(parent, "Stringtable-Sprache", ImmutableList.of("German", "English", "Italian",
						"French", "Spanish", "Czech", "Hungarian", "Polish", "Russian", "TRC"), true),
				"EditorOptions.Language.STRINGTABLE_LANGUAGE", "Stringtable Sprache");
	}

	public static abstract class Misc {
		public static final Option<Boolean> MAKE_BACKUP = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, "Backup anlegen"), "EditorOptions.Misc.MAKE_BACKUP", "Backup anlegen");

		public static final Option<Boolean> CLEAN_STRINGTABLE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, "Nicht mehr benötige Einträge aus der Stringtable entfernen"),
				"EditorOptions.Misc.CLEAN_STRINGTABLE", "Stringtable aufräumen");

		public static final Option<Boolean> OPTIMIZE_MEMORY_USAGE = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent,
						"Speicherverbrauch optimieren (Laden, Speichern und Schließen von Dateien kann länger dauern)"),
				"EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE", "Speicherverbrauch optimieren");

		public static final Option<Boolean> IMPROVE_CHANGE_DETECTION = new LambdaOption<>(true,
				(parent) -> new BooleanOptionHandler(parent, "Hashbasierte Änderungserkennung beim Schließen von Dateien",
						SwingUtils.getMultilineText("Vermeidet unnötige 'Änderungen speichern'-Dialoge beim Schließen von Dateien,",
								"auf langsamen Systemen kann aber dafür das Laden und Schließen von Dateien länger dauern.")),
				"EditorOptions.Misc.IMPROVE_CHANGE_DETECTION", "Verbesserte Änderungserkennung");

		public static final Option<Boolean> NAVPATH_DEBUG = new LambdaOption<>(false,
				(parent) -> new BooleanOptionHandler(parent, "Debuginformationen bei der Berechnung von NavPaths anzeigen"),
				"EditorOptions.Misc.NAVPATH_DEBUG", "NavPath Debuginformationen");
	}

	public static abstract class D3View {
		public static final Option<Color> BACKGROUND_COLOR = new LambdaOption<>(Color.DARK_GRAY,
				(parent) -> new ColorChooserOptionHandler(parent, "Hintergrundfarbe der 3D-Ansicht"),
				"EditorOptions.D3View.BACKGROUND_COLOR", "Hintergrundfarbe der 3D-Ansicht");

		public static final Option<Float> AMBIENT_LIGHT_INTENSITY = new LambdaOption<>(0.6f,
				(parent) -> new FloatSliderOptionHandler(parent, "Intensität der Umgebungsbeleuchtung", 0, 100, 100, true),
				"EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY", "Intensität der Umgebungsbeleuchtung");

		public static final Option<Float> DIRECTIONAL_LIGHT_INTENSITY = new LambdaOption<>(1.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Intensität der gerichteten Beleuchtung", 0, 200, 100, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY", "Intensität der gerichteten Beleuchtung");

		public static final Option<Float> DIRECTIONAL_LIGHT_AZIMUTH = new LambdaOption<>(70.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Horizontale Rotation der gerichteten Beleuchtung", 0, 360, 1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH", "Horizontale Rotation der gerichteten Beleuchtung");

		public static final Option<Float> DIRECTIONAL_LIGHT_INCLINATION = new LambdaOption<>(35.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Vertikale Rotation der gerichteten Beleuchtung", -90, 90, 1, true),
				"EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION", "Vertikale Rotation der gerichteten Beleuchtung");

		public static final Option<Float> HORIZONTAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Horizontale Rotation", 0, 360, 1, true),
				"EditorOptions.D3View.HORIZONTAL_ROTATION", "Horizontale Rotation");

		public static final Option<Float> VERTICAL_ROTATION = new LambdaOption<>(0.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Vertikale Rotation", -90, 90, 1, true),
				"EditorOptions.D3View.VERTICAL_ROTATION", "Vertikale Rotation");

		public static final Option<Float> DISTANCE = new LambdaOption<>(4.0f,
				(parent) -> new FloatSliderOptionHandler(parent, "Distanz", 10, 800, 10, true), "EditorOptions.D3View.DISTANCE",
				"Distanz");

		public static final Option<String> SCREENSHOT_FOLDER = new LambdaOption<>("",
				(parent) -> new FolderPathOptionHandler(parent, "Screenshot Verzeichnis", "Screenshot Verzeichnis auswählen"),
				"EditorOptions.D3View.SCREENSHOT_FOLDER", "Screenshot Verzeichnis");
	}

	public static abstract class MainWindow {
		public static final Option<Dimension> SIZE = new NoHandlerOption<>(new Dimension(700, 500), "EditorOptions.MainWindow.SIZE",
				"Fenstergröße");

		public static final Option<Point> LOCATION = new NoHandlerOption<>(new Point(50, 50), "EditorOptions.MainWindow.LOCATION",
				"Fensterposition");

		public static final Option<Integer> EXTENDED_STATE = new NoHandlerOption<>(JFrame.NORMAL,
				"EditorOptions.MainWindow.EXTENDED_STATE", "Fensterstatus");
	}

	public static abstract class MainMenu {
		public static final Option<List<String>> RECENT_FILES = new NoHandlerOption<>(Collections.emptyList(),
				"EditorOptions.MainMenu.RECENT_FILES", "Letzte Dateien");
	}

	public static abstract class TheVoid {
		public static final Option<Void> FILE_EXTENSIONS = new LambdaOption<>(null, FileExtensionOptionHandler::new,
				"EditorOptions.TheVoid.FILE_EXTENSIONS", "Dateiendungen");
	}

	public static abstract class CheckManager {
		public static final Option<Set<String>> ENABLED_CHECKS = new NoHandlerOption<>(null, "EditorOptions.CheckManager.ENABLED_CHECKS",
				"Ausgewählte Checks");
	}
}
