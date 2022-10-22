package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.json.JsonUtil;

public abstract class JsonSetConfigFile<T> extends ReloadableConfigFile<ImmutableSet<T>> {
	public JsonSetConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableSet<T> read(File configFile) throws IOException {
		return ImmutableSet.copyOf(JsonUtil.fieldAutodetectMapper().<List<T>>readValue(configFile,
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, type())));
	}

	@Override
	protected void write(ImmutableSet<T> content, File configFile) throws IOException {
		JsonUtil.fieldAutodetectMapper().writeValue(configFile, content);
	}

	@Override
	protected ImmutableSet<T> getDefaultValue() {
		return ImmutableSet.of();
	}

	protected abstract Class<T> type();
}
