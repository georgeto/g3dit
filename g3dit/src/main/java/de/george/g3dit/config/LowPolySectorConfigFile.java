package de.george.g3dit.config;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.LowPolyGenerator.LowPolySector;

public class LowPolySectorConfigFile extends JsonListConfigFile<LowPolySector> {
	public LowPolySectorConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected Class<LowPolySector> type() {
		return LowPolySector.class;
	}
}
