package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.json.JsonUtil;

public abstract class JsonListConfigFile<T> extends ReloadableConfigFile<ImmutableList<T>> {
	public JsonListConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableList<T> read(File configFile) throws IOException {
		return ImmutableList.copyOf(JsonUtil.fieldAutodetectMapper().<List<T>>readValue(configFile,
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, type())));
	}

	@Override
	protected void write(ImmutableList<T> content, File configFile) throws IOException {
		JsonUtil.fieldAutodetectMapper().writeValue(configFile, content);
	}

	@Override
	protected ImmutableList<T> getDefaultValue() {
		return ImmutableList.of();
	}

	protected abstract Class<T> type();
}
