package com.study.webflux.rag.voice.controller;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.webflux.rag.voice.model.RagVoiceRequest;
import com.study.webflux.rag.voice.service.RagVoicePipelineService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/rag/voice")
public class RagVoiceController {

	private final RagVoicePipelineService pipelineService;

	public RagVoiceController(RagVoicePipelineService pipelineService) {
		this.pipelineService = pipelineService;
	}

	@PostMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> ragVoiceStream(@Valid @RequestBody RagVoiceRequest request) {
		return pipelineService.runPipeline(request);
	}

	@PostMapping(path = "/audio/wav", produces = "audio/wav")
	public Flux<DataBuffer> ragVoiceAudioWav(@Valid @RequestBody RagVoiceRequest request) {
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		return pipelineService.runPipelineAudio(request)
			.map(bufferFactory::wrap);
	}

	@PostMapping(path = "/audio/mp3", produces = "audio/mpeg")
	public Flux<DataBuffer> ragVoiceAudioMp3(@Valid @RequestBody RagVoiceRequest request) {
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		return pipelineService.runPipelineAudio(request)
			.map(bufferFactory::wrap);
	}

	@PostMapping(path = "/audio", produces = "audio/wav")
	public Flux<DataBuffer> ragVoiceAudio(@Valid @RequestBody RagVoiceRequest request) {
		return ragVoiceAudioWav(request);
	}
}
