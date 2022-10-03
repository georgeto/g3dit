package de.george.g3dit.util;

import static j2html.TagCreator.text;

import java.awt.Color;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import de.george.g3dit.check.EntityDescriptor;
import j2html.TagCreator;
import one.util.streamex.StreamEx;

public class HtmlCreator {
	private HtmlCreator() {}

	public static String renderLink(String content, String href) {
		return TagCreator.a(text(content)).withHref(href).render();
	}

	public static String renderEntity(EntityDescriptor entity) {
		String entityIdentifier = String.format("%s (%s, %s #%d)", entity.getDisplayName(), entity.getGuid(),
				entity.getFile().getPath().getName(), entity.getIndex());
		return TagCreator.a(entityIdentifier).withHref(UriUtil.encodeEntity(entity)).render();
	}

	public static String renderEntityShort(EntityDescriptor entity) {
		return renderLink(entity.getDisplayName(), UriUtil.encodeEntity(entity));
	}

	public static final String LINE_SEPERATOR = "<br>";

	public static String renderList(Iterable<String> lines) {
		return StreamEx.of(lines.iterator()).joining(LINE_SEPERATOR);
	}

	public static String renderList(String... lines) {
		return StreamEx.of(lines).joining(LINE_SEPERATOR);
	}

	public static Collector<CharSequence, ?, String> collectList() {
		return Collectors.joining(LINE_SEPERATOR);
	}

	public static String formatColorAsHex(Color color) {
		return String.format("#%06x", color.getRGB() & 0xffffff);
	}
}
