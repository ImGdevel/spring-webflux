package com.study.webflux.rag.domain.model.voice;

public class Voice {
	private final String id;
	private final String name;
	private final String provider;
	private final VoiceSettings settings;
	private final String language;
	private final VoiceStyle style;
	private final AudioFormat outputFormat;

	private Voice(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.provider = builder.provider;
		this.settings = builder.settings;
		this.language = builder.language;
		this.style = builder.style;
		this.outputFormat = builder.outputFormat;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProvider() {
		return provider;
	}

	public VoiceSettings getSettings() {
		return settings;
	}

	public String getLanguage() {
		return language;
	}

	public VoiceStyle getStyle() {
		return style;
	}

	public AudioFormat getOutputFormat() {
		return outputFormat;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String id;
		private String name;
		private String provider;
		private VoiceSettings settings = VoiceSettings.defaultSettings();
		private String language = "ko";
		private VoiceStyle style = VoiceStyle.NEUTRAL;
		private AudioFormat outputFormat = AudioFormat.WAV;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public Builder settings(VoiceSettings settings) {
			this.settings = settings;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public Builder style(VoiceStyle style) {
			this.style = style;
			return this;
		}

		public Builder outputFormat(AudioFormat outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

		public Voice build() {
			if (id == null || id.isBlank()) {
				throw new IllegalStateException("Voice id is required");
			}
			if (name == null || name.isBlank()) {
				throw new IllegalStateException("Voice name is required");
			}
			if (provider == null || provider.isBlank()) {
				throw new IllegalStateException("Voice provider is required");
			}
			return new Voice(this);
		}
	}
}
