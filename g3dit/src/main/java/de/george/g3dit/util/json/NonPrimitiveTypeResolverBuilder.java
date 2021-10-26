package de.george.g3dit.util.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

/**
 * Changes DefaultTyping.EVERYTHING to exclude TreeNode, as otherwise readValueAsTree() does not
 * work!
 */
public class NonPrimitiveTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
	public NonPrimitiveTypeResolverBuilder() {
		super(ObjectMapper.DefaultTyping.EVERYTHING, new LaissezFaireSubTypeValidator());
		init(JsonTypeInfo.Id.CLASS, null);
		inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
	}

	@Override
	public boolean useForType(JavaType t) {
		if (_appliesFor == ObjectMapper.DefaultTyping.EVERYTHING) {
			while (t.isArrayType()) {
				t = t.getContentType();
			}
			while (t.isReferenceType()) {
				t = t.getReferencedType();
			}

			return !TreeNode.class.isAssignableFrom(t.getRawClass());
		} else
			return super.useForType(t);
	}
}
