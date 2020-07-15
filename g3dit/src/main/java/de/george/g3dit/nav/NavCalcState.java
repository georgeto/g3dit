package de.george.g3dit.nav;

enum NavCalcState {
	NavZone(NavZoneStage.class),
	NegZone(NegZoneStage.class),
	NavPath(NavPathStage.class),
	PrefPath(PrefPathStage.class),
	NegCircle(NegCircleStage.class),
	InteractObject(InteractObjectStage.class);

	private final Class<? extends NavCalcStage> stage;

	private NavCalcState(Class<? extends NavCalcStage> stage) {
		this.stage = stage;
	}

	public Class<? extends NavCalcStage> getStage() {
		return stage;
	}
}
