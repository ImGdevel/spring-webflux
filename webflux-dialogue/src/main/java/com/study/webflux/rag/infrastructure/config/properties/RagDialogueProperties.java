package com.study.webflux.rag.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.study.webflux.rag.infrastructure.config.constants.DialogueConstants;

@Component
@ConfigurationProperties(prefix = "rag.dialogue")
public class RagDialogueProperties {

	private OpenAi openai = new OpenAi();
	private Supertone supertone = new Supertone();
	private Qdrant qdrant = new Qdrant();
	private Memory memory = new Memory();

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

	public Qdrant getQdrant() {
		return qdrant;
	}

	public void setQdrant(Qdrant qdrant) {
		this.qdrant = qdrant;
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
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
		private String baseUrl = DialogueConstants.Supertone.BASE_URL;
		private String voiceId = DialogueConstants.Supertone.Voice.ADAM_ID;
		private String language = DialogueConstants.Supertone.Language.KOREAN;
		private String style = DialogueConstants.Supertone.Style.NEUTRAL;
		private String outputFormat = DialogueConstants.Supertone.OutputFormat.WAV;
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

	public static class Qdrant {
		private String url = "http://localhost:6333";
		private String apiKey;
		private int vectorDimension = 1536;
		private String collectionName = "user_memories";

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public int getVectorDimension() {
			return vectorDimension;
		}

		public void setVectorDimension(int vectorDimension) {
			this.vectorDimension = vectorDimension;
		}

		public String getCollectionName() {
			return collectionName;
		}

		public void setCollectionName(String collectionName) {
			this.collectionName = collectionName;
		}
	}

	public static class Memory {
		private String embeddingModel = "text-embedding-3-small";
		private String extractionModel = "gpt-4o-mini";
		private int conversationThreshold = 5;
		private float importanceBoost = 0.05f;
		private float importanceThreshold = 0.3f;

		public String getEmbeddingModel() {
			return embeddingModel;
		}

		public void setEmbeddingModel(String embeddingModel) {
			this.embeddingModel = embeddingModel;
		}

		public String getExtractionModel() {
			return extractionModel;
		}

		public void setExtractionModel(String extractionModel) {
			this.extractionModel = extractionModel;
		}

		public int getConversationThreshold() {
			return conversationThreshold;
		}

		public void setConversationThreshold(int conversationThreshold) {
			this.conversationThreshold = conversationThreshold;
		}

		public float getImportanceBoost() {
			return importanceBoost;
		}

		public void setImportanceBoost(float importanceBoost) {
			this.importanceBoost = importanceBoost;
		}

		public float getImportanceThreshold() {
			return importanceThreshold;
		}

		public void setImportanceThreshold(float importanceThreshold) {
			this.importanceThreshold = importanceThreshold;
		}
	}
}
