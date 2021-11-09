package de.george.g3dit.util;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.teamunify.i18n.I;

import de.george.g3utils.util.Misc;
import net.tomahawk.ExtensionsFilter;
import net.tomahawk.XFileDialog;

public class FileDialogWrapper {
	public static final ExtensionsFilter NO_FILTER = createFilter(I.tr("All"), "*");
	public static final ExtensionsFilter LRENTDAT_FILTER = createFilter("Lrentdat", "lrentdat");
	public static final ExtensionsFilter NODE_FILTER = createFilter("Node", "node");
	public static final ExtensionsFilter ARCHIVE_FILTER = createFilter(I.tr("World data"), "lrentdat", "node");
	public static final ExtensionsFilter TEMPLATE_FILTER = createFilter("Template", "tple");
	public static final ExtensionsFilter SECDAT_FILTER = createFilter("Secdat", "secdat");
	public static final ExtensionsFilter EFFECT_MAP_FILTER = createFilter("EffectMap", "efm");
	public static final ExtensionsFilter COMBINED_FILTER = createFilter("", "lrentdat", "node", "tple", "secdat", "efm", "xcmsh", "xact",
			"xlmsh");
	public static final ExtensionsFilter XNAV_FILTER = createFilter("NavigationMap", "xnav");
	public static final ExtensionsFilter MESH_FILTER = createFilter("Mesh", "xcmsh", "xact", "xlmsh");
	public static final ExtensionsFilter COLLISION_MESH_FILTER = createFilter("Collision mesh", "xnvmsh");
	public static final ExtensionsFilter CSV_FILTER = createFilter("csv", "csv");
	public static final ExtensionsFilter JSON_FILTER = createFilter("json", "json");
	public static final ExtensionsFilter TXT_FILTER = createFilter("txt", "txt");

	public static ExtensionsFilter createFilter(String title, String... ext) {
		return new ExtensionsFilter(title, Misc.asList(ext));
	}

	private static ImmutableList<String> places = ImmutableList.of();

	public static void setPlaces(List<String> places) {
		FileDialogWrapper.places = ImmutableList.copyOf(places);
	}

	public static File openFile(String title, Component parent, ExtensionsFilter... filters) {
		XFileDialog fileDialog = createFileDialog(parent, title, filters);
		String path = fileDialog.getFile();
		return parseFile(fileDialog.getDirectory(), path, true);
	}

	public static List<File> openFiles(String title, Component parent, ExtensionsFilter... filters) {
		XFileDialog fileDialog = createFileDialog(parent, title, filters);
		String[] paths = fileDialog.getFiles();

		if (paths == null) {
			return Collections.emptyList();
		}

		List<File> files = new ArrayList<>();
		for (String path : paths) {
			File file = parseFile(fileDialog.getDirectory(), path, true);
			if (file != null) {
				files.add(file);
			}
		}
		return files;
	}

	public static File chooseDirectory(String title, Component parent) {
		XFileDialog fileDialog = new XFileDialog(parent);
		fileDialog.setTitle(title);
		places.forEach(fileDialog::addPlace);
		return parseFile(fileDialog.getFolder(), "", true);
	}

	public static File saveFile(String title, Component parent, ExtensionsFilter... filters) {
		return saveFile(title, null, null, parent, filters);
	}

	public static File saveFile(String title, String defaultFilename, Component parent, ExtensionsFilter... filters) {
		return saveFile(title, defaultFilename, Files.getFileExtension(defaultFilename), parent, filters);
	}

	public static File saveFile(String title, String defaultFilename, String defaultExtension, Component parent,
			ExtensionsFilter... filters) {
		XFileDialog fileDialog = createFileDialog(parent, title, filters);
		if (defaultFilename != null) {
			fileDialog.setFilename(defaultFilename);
		}
		if (defaultExtension != null) {
			fileDialog.setDefaultExtension(defaultExtension);
		}

		String filename = fileDialog.getSaveFile();
		return parseFile(fileDialog.getDirectory(), filename, false);
	}

	private static XFileDialog createFileDialog(Component parent, String title, ExtensionsFilter[] filters) {
		XFileDialog fileDialog = new XFileDialog(parent);
		fileDialog.setTitle(title);
		fileDialog.resetFilters();
		fileDialog.addFilters(filters);
		places.forEach(fileDialog::addPlace);
		return fileDialog;
	}

	/**
	 * Wertet das Ergebnis von XFileDialog aus
	 *
	 * @param path Ordner in dem sich die Datei befindet
	 * @param filename Name der Datei
	 * @param exist Muss die Datei existieren (true) oder ist es egal (false)
	 * @return Datei oder <code>null</code> wenn keine Datei ermittelt werden konnte
	 */
	private static File parseFile(String path, String filename, boolean exist) {
		if (path == null || filename == null) {
			return null;
		}
		File file = new File(path, filename);
		if (exist && !file.exists()) {
			return null;
		}
		return file;
	}
}
