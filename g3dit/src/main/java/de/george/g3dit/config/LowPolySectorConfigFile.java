package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.LowPolyGenerator.LowPolySector;
import de.george.g3dit.util.json.JsonUtil;

public class LowPolySectorConfigFile extends ReloadableConfigFile<ImmutableList<LowPolySector>> {
	public LowPolySectorConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableList<LowPolySector> read(File configFile) throws IOException {
		return ImmutableList.copyOf(JsonUtil.fieldAutodetectMapper().<List<LowPolySector>>readValue(configFile,
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, LowPolySector.class)));
	}

	@Override
	protected ImmutableList<LowPolySector> getDefaultValue() {
		return ImmutableList.of();
	}
}
