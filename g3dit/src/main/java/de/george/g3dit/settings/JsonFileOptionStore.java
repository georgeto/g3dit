package de.george.g3dit.settings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import de.george.g3dit.util.json.JsonUtil;
import de.george.g3dit.util.json.NonPrimitiveTypeResolverBuilder;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public class JsonFileOptionStore extends AbstractConcurrentOptionStore {
	private Logger logger = LoggerFactory.getLogger(JsonFileOptionStore.class);

	private ObjectMapper mapper = JsonUtil.noGetterAutodetectMapper().registerModule(JsonUtil.getExtensionModule())
			.registerModule(new ParameterNamesModule())
			.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
			.setDefaultTyping(new NonPrimitiveTypeResolverBuilder());
	private File file;

	public JsonFileOptionStore(File file) {
		this.file = file;
		if (file.exists())
			try (JsonParser parser = mapper.createParser(file)) {
				ObjectNode rootNode = parser.readValueAsTree();
				for (var option : StreamEx.of(rootNode.fields())) {
					try {
						Object value = mapper.treeToValue(option.getValue(), Object.class);
						options.put(option.getKey(), value);
					} catch (JacksonException e) {
						logger.warn("Encountered malformed option '{}' while loading the config file.", option.getKey(), e);
					}
				}

				while (true) {
					String optionName = parser.nextFieldName();
					if (optionName == null)
						break;
					parser.nextToken();

				}
			} catch (IOException | ClassCastException e) {
				// Nichts, vielleicht existiert noch keine Config datei
				logger.warn("Error while loading configuration file.", e);
			}
	}

	public JsonFileOptionStore(File file, Map<String, Object> options) {
		this.file = file;
		this.options.putAll(options);
	}

	@Override
	public boolean save() {
		synchronized (file) {
			try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					JsonGenerator gen = mapper.createGenerator(buffer, JsonEncoding.UTF8)) {
				// To avoid overwriting the old config file with something half finished, if
				// serialization fails, we first serialize the config file into a byte buffer.
				gen.writeStartObject();
				for (var option : EntryStream.of(options).sorted(Map.Entry.comparingByKey())) {
					gen.writeFieldName(option.getKey());
					mapper.writeValue(gen, option.getValue());
				}
				gen.writeEndObject();
				gen.flush();

				// Serialization was successful, now write the config file to disk.
				Files.write(file.toPath(), buffer.toByteArray());
				return true;
			} catch (IOException e) {
				logger.warn("Error while saving configuration file.", e);
				return false;
			}
		}
	}
}
