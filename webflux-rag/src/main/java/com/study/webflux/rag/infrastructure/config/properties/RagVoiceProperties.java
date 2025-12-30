package com.study.webflux.rag.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.study.webflux.rag.infrastructure.config.constants.VoiceConstants;

@Component
@ConfigurationProperties(prefix = "rag.voice")
public class RagVoiceProperties {

	private OpenAi openai = new OpenAi();
	private Supertone supertone = new Supertone();

	public OpenAi getOpenai() {
		return openai;
	}

	public void setOpenai(OpenAi openai) {
		this.openai = openai;
	}

	public Supertone getSupertone() {
		return supertone;
	}

	public void setSupertone(Supertone supertone) {
		this.supertone = supertone;
	}

	public static class OpenAi {
		private String apiKey;
		private String baseUrl = "https://api.openai.com/v1";
		private String model = "gpt-4.1-nano";

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}
	}

	public static class Supertone {
		private String apiKey;
		private String baseUrl = VoiceConstants.Supertone.BASE_URL;
		private String voiceId = VoiceConstants.Supertone.Voice.ADAM_ID;
		private String language = VoiceConstants.Supertone.Language.KOREAN;
		private String style = VoiceConstants.Supertone.Style.NEUTRAL;
		private String outputFormat = VoiceConstants.Supertone.OutputFormat.WAV;
		private VoiceSettings voiceSettings = new VoiceSettings();

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getVoiceId() {
			return voiceId;
		}

		public void setVoiceId(String voiceId) {
			this.voiceId = voiceId;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getStyle() {
			return style;
		}

		public void setStyle(String style) {
			this.style = style;
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public VoiceSettings getVoiceSettings() {
			return voiceSettings;
		}

		public void setVoiceSettings(VoiceSettings voiceSettings) {
			this.voiceSettings = voiceSettings;
		}

		public static class VoiceSettings {
			private int pitchShift = 0;
			private double pitchVariance = 1.0;
			private double speed = 1.1;

			public int getPitchShift() {
				return pitchShift;
			}

			public void setPitchShift(int pitchShift) {
				this.pitchShift = pitchShift;
			}

			public double getPitchVariance() {
				return pitchVariance;
			}

			public void setPitchVariance(double pitchVariance) {
				this.pitchVariance = pitchVariance;
			}

			public double getSpeed() {
				return speed;
			}

			public void setSpeed(double speed) {
				this.speed = speed;
			}
		}
	}
}
