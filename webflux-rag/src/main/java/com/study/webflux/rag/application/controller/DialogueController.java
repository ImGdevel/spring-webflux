package com.study.webflux.rag.application.controller;

import com.study.webflux.rag.application.dto.RagDialogueRequest;
import com.study.webflux.rag.domain.port.in.DialoguePipelineUseCase;
import jakarta.validation.Valid;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/rag/dialogue")
public class DialogueController {

	private final DialoguePipelineUseCase dialoguePipelineUseCase;

	public DialogueController(DialoguePipelineUseCase dialoguePipelineUseCase) {
		this.dialoguePipelineUseCase = dialoguePipelineUseCase;
	}

	@PostMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> ragDialogueStream(@Valid @RequestBody RagDialogueRequest request) {
		return dialoguePipelineUseCase.executeStreaming(request.text());
	}

	@PostMapping(path = "/audio/wav", produces = "audio/wav")
	public Flux<DataBuffer> ragDialogueAudioWav(@Valid @RequestBody RagDialogueRequest request) {
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		return dialoguePipelineUseCase.executeAudioStreaming(request.text())
			.map(bufferFactory::wrap);
	}

	@PostMapping(path = "/audio/mp3", produces = "audio/mpeg")
	public Flux<DataBuffer> ragDialogueAudioMp3(@Valid @RequestBody RagDialogueRequest request) {
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		return dialoguePipelineUseCase.executeAudioStreaming(request.text())
			.map(bufferFactory::wrap);
	}

	@PostMapping(path = "/audio", produces = "audio/wav")
	public Flux<DataBuffer> ragDialogueAudio(@Valid @RequestBody RagDialogueRequest request) {
		return ragDialogueAudioWav(request);
	}
}
