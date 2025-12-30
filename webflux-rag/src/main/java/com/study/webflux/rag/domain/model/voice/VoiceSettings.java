package com.study.webflux.rag.domain.model.voice;

public record VoiceSettings(
	int pitchShift,
	double pitchVariance,
	double speed
) {
	public static VoiceSettings defaultSettings() {
		return new VoiceSettings(0, 1.0, 1.1);
	}

	public VoiceSettings {
		if (pitchVariance <= 0) {
			throw new IllegalArgumentException("pitchVariance must be positive");
		}
		if (speed <= 0) {
			throw new IllegalArgumentException("speed must be positive");
		}
	}
}
