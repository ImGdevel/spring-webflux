package com.study.webflux.rag.infrastructure.config.constants;

public class DialogueConstants {

	public static class Supertone {
		public static final String BASE_URL = "https://supertoneapi.com";
		public static final String MODEL = "sona_speech_1";

		public static class Voice {
			public static final String ADAM_ID = "2c5f135cb33f49a2c8882d";
		}

		public static class Language {
			public static final String KOREAN = "ko";
			public static final String ENGLISH = "en";
			public static final String JAPANESE = "ja";
		}

		public static class Style {
			public static final String NEUTRAL = "neutral";
			public static final String HAPPY = "happy";
		}

		public static class OutputFormat {
			public static final String WAV = "wav";
			public static final String MP3 = "mp3";
		}
	}
}
