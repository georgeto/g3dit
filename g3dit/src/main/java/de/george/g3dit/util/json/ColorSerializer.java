package de.george.g3dit.util.json;

import java.awt.Color;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class ColorSerializer extends JsonSerializer<Color> {
	@Override
	public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartObject();
		gen.writeFieldName("argb");
		gen.writeString(Integer.toHexString(value.getRGB()));
		gen.writeEndObject();
	}

	@Override
	public void serializeWithType(Color value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
			throws IOException {
		WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT));
		gen.writeFieldName("argb");
		gen.writeString(Integer.toHexString(value.getRGB()));
		typeSer.writeTypeSuffix(gen, typeIdDef);
	}
}
