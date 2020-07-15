package de.george.g3dit.tab.archive.views.property;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import de.george.g3dit.tab.archive.views.property.JsonPropertyValueConverter.JsonStringWrapper;
import de.george.g3utils.io.G3Serializable;

public class JsonPropertyValueConverter<T extends G3Serializable> extends AbstractPropertyValueConverter<T, JsonStringWrapper> {
	private final ObjectReader reader;
	private final ObjectWriter writer;

	public static class JsonStringWrapper {
		private String value;

		public JsonStringWrapper(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof JsonStringWrapper && Objects.equals(value, ((JsonStringWrapper) obj).value);
		}

		@Override
		public String toString() {
			return value;
		}
	}

	protected JsonPropertyValueConverter(Class<T> propertyType) {
		super(propertyType, JsonStringWrapper.class);

		JsonMapper mapper = JsonMapper.builder().disable(JsonWriteFeature.QUOTE_FIELD_NAMES)
				.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES).enable(SerializationFeature.INDENT_OUTPUT)
				.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS, MapperFeature.AUTO_DETECT_GETTERS,
						MapperFeature.AUTO_DETECT_IS_GETTERS)
				.build();

		reader = mapper.readerFor(propertyType);
		writer = mapper.writerFor(propertyType);
	}

	@Override
	public JsonStringWrapper convertTo(T source) throws JsonProcessingException {
		return new JsonStringWrapper(writer.writeValueAsString(source));
	}

	@Override
	public T convertFrom(T old, JsonStringWrapper value) throws JsonProcessingException, IOException {
		return reader.readValue(value.getValue());
	}

}
