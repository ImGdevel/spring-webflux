package com.study.webflux.rag.domain.model.voice;

public enum VoiceStyle {
	NEUTRAL("neutral"),
	HAPPY("happy"),
	SAD("sad"),
	ANGRY("angry"),
	EXCITED("excited");

	private final String value;

	VoiceStyle(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static VoiceStyle fromString(String style) {
		for (VoiceStyle vs : values()) {
			if (vs.value.equalsIgnoreCase(style)) {
				return vs;
			}
		}
		return NEUTRAL;
	}
}
