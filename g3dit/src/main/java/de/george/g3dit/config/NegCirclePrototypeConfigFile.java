package de.george.g3dit.config;

import de.george.g3dit.EditorContext;
import de.george.navmap.data.NegCircle.NegCirclePrototype;

public class NegCirclePrototypeConfigFile extends JsonListConfigFile<NegCirclePrototype> {
	public NegCirclePrototypeConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected Class<NegCirclePrototype> type() {
		return NegCirclePrototype.class;
	}
}
