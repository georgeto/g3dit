package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.json.JsonUtil;
import de.george.navmap.data.NegCircle.NegCirclePrototype;

public class NegCirclePrototypeConfigFile extends ReloadableConfigFile<ImmutableList<NegCirclePrototype>> {
	public NegCirclePrototypeConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableList<NegCirclePrototype> read(File configFile) throws IOException {
		return ImmutableList.copyOf(JsonUtil.fieldAutodetectMapper().<List<NegCirclePrototype>>readValue(configFile,
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, NegCirclePrototype.class)));
	}

	@Override
	protected ImmutableList<NegCirclePrototype> getDefaultValue() {
		return ImmutableList.of();
	}
}
