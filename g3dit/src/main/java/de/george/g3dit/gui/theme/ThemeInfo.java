package de.george.g3dit.gui.theme;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.bsideup.jabel.Desugar;

@Desugar
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record ThemeInfo(String name, String description, String resourceName, boolean dark, String lafClassName, File themeFile) {
}
