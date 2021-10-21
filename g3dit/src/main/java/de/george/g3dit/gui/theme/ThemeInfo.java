package de.george.g3dit.gui.theme;

import java.io.File;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record ThemeInfo(String name, String description, String resourceName, boolean dark, String lafClassName, File themeFile) {
}
