package de.george.g3dit.config;

import de.george.g3dit.EditorContext;

public interface ConfigFiles {
	public static NegCirclePrototypeConfigFile negCirclePrototypes(EditorContext ctx) {
		return ctx.getFileManager().getConfigFile("NegCirclePrototypes.json", NegCirclePrototypeConfigFile.class);
	}

	public static GuidWithCommentConfigFile negCirclesWithoutObject(EditorContext ctx) {
		return ctx.getFileManager().getConfigFile("NegCirclesWithoutObject.json", GuidWithCommentConfigFile.class);
	}

	public static GuidWithCommentConfigFile objectsWithoutNegCircles(EditorContext ctx) {
		return ctx.getFileManager().getConfigFile("ObjectsWithoutNegCircle.json", GuidWithCommentConfigFile.class);
	}

	public static LowPolySectorConfigFile lowPolySectors(EditorContext ctx) {
		return ctx.getFileManager().getConfigFile("LowPolySectors.json", LowPolySectorConfigFile.class);
	}

	public static GuidWithCommentConfigFile objectsWithoutLowPoly(EditorContext ctx) {
		return ctx.getFileManager().getConfigFile("ObjectsWithoutLowPoly.json", GuidWithCommentConfigFile.class);
	}
}
