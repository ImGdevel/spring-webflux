package com.study.webflux.rag.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.study.webflux.rag.domain.model.voice.AudioFormat;
import com.study.webflux.rag.domain.model.voice.Voice;
import com.study.webflux.rag.domain.model.voice.VoiceSettings;
import com.study.webflux.rag.domain.model.voice.VoiceStyle;
import com.study.webflux.rag.voice.config.RagVoiceProperties;

@Configuration
public class VoiceConfiguration {

	@Bean
	public Voice defaultVoice(RagVoiceProperties properties) {
		var supertone = properties.getSupertone();
		var settings = supertone.getVoiceSettings();

		return Voice.builder()
			.id(supertone.getVoiceId())
			.name("adam")
			.provider("supertone")
			.settings(new VoiceSettings(
				settings.getPitchShift(),
				settings.getPitchVariance(),
				settings.getSpeed()
			))
			.language(supertone.getLanguage())
			.style(VoiceStyle.fromString(supertone.getStyle()))
			.outputFormat(AudioFormat.fromString(supertone.getOutputFormat()))
			.build();
	}
}
