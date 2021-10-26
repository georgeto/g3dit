package de.george.g3dit.util.json;

import java.awt.Color;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonUtil {
	public static final ObjectMapper noAutodetectMapper() {
		return JsonMapper.builder().disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
				MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS).build();
	}

	public static final ObjectMapper noGetterAutodetectMapper() {
		return JsonMapper.builder().disable(MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS).build();
	}

	public static final ObjectMapper fieldAutodetectMapper() {
		return JsonMapper.builder().disable(MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS).build();
	}

	public static final Module getExtensionModule() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(Color.class, new ColorSerializer());
		module.addDeserializer(Color.class, new ColorDeserializer());
		return module;
	}
}
