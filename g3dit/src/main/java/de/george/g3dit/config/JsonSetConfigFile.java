package de.george.g3dit.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.json.JsonUtil;
import de.george.g3utils.util.ReflectionUtils;

public abstract class JsonSetConfigFile<T> extends ReloadableConfigFile<ImmutableSet<T>> {
	private final Function<T, T> methodClone = ReflectionUtils.toFunctionalInterface(Function.class,
			ReflectionUtils.getMethod(type(), "clone"));

	public JsonSetConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableSet<T> read(Path configFile) throws IOException {
		return ImmutableSet.copyOf(JsonUtil.fieldAutodetectMapper().<List<T>>readValue(configFile.toFile(),
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, type())));
	}

	@Override
	protected void write(ImmutableSet<T> content, Path configFile) throws IOException {
		JsonUtil.fieldAutodetectMapper().writeValue(configFile.toFile(), content);
	}

	@Override
	protected ImmutableSet<T> getDefaultValue() {
		return ImmutableSet.of();
	}

	public ImmutableSet<T> getClonedContent() {
		return getContent().stream().map(methodClone).collect(ImmutableSet.toImmutableSet());
	}

	public abstract Class<T> type();
}
