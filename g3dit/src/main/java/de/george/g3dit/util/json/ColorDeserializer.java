package de.george.g3dit.util.json;

import java.awt.Color;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class ColorDeserializer extends JsonDeserializer<Color> {
	@Override
	public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		TreeNode root = p.getCodec().readTree(p);
		TextNode rgba = (TextNode) root.get("argb");
		return new Color(Integer.parseUnsignedInt(rgba.textValue(), 16), true);
	}
}
