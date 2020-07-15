package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.collect.ImmutableList;

import de.george.g3dit.EditorContext;
import de.george.g3utils.util.IOUtils;

public class StringListConfigFile extends ReloadableConfigFile<ImmutableList<String>> {

	public StringListConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableList<String> read(File configFile) throws IOException {
		return ImmutableList.copyOf(IOUtils.readTextFile(configFile, StandardCharsets.UTF_8));
	}

	@Override
	protected ImmutableList<String> getDefaultValue() {
		return ImmutableList.of();
	}

}
