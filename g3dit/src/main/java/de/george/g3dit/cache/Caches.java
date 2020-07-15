package de.george.g3dit.cache;

import de.george.g3dit.EditorContext;

public interface Caches {
	public static EntityCache entity(EditorContext ctx) {
		return ctx.getCacheManager().getCache(EntityCache.class);
	}

	public static LightCache light(EditorContext ctx) {
		return ctx.getCacheManager().getCache(LightCache.class);
	}

	public static NavCache nav(EditorContext ctx) {
		return ctx.getCacheManager().getCache(NavCache.class);
	}

	public static StringtableCache stringtable(EditorContext ctx) {
		return ctx.getCacheManager().getCache(StringtableCache.class);
	}

	public static TemplateCache template(EditorContext ctx) {
		return ctx.getCacheManager().getCache(TemplateCache.class);
	}
}
