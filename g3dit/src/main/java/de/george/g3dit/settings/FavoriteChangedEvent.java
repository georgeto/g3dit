package de.george.g3dit.settings;

import java.nio.file.Path;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record FavoriteChangedEvent(Path filePath) {
}
