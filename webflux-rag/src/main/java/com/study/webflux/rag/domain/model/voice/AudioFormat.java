package com.study.webflux.rag.domain.model.voice;

public enum AudioFormat {
	WAV("audio/wav"),
	MP3("audio/mpeg"),
	PCM("audio/pcm");

	private final String mediaType;

	AudioFormat(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public static AudioFormat fromString(String format) {
		return valueOf(format.toUpperCase());
	}
}
