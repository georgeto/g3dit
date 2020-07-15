package de.george.g3dit.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
	public static final ObjectMapper noAutodetectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS, MapperFeature.AUTO_DETECT_GETTERS,
				MapperFeature.AUTO_DETECT_IS_GETTERS);
		return mapper;
	}

	public static final ObjectMapper fieldAutodetectMapper() {
		return new ObjectMapper().disable(MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
	}
}
