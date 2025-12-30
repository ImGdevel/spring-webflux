package com.study.webflux.voice;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import reactor.core.publisher.Flux;

/**
 * 텍스트 → LLM → TTS 파이프라인을 SSE 스트림으로 노출하는 WebFlux 예제 컨트롤러.
 */
@Validated
@RestController
@RequestMapping("/voice")
public class SseTtsController {

	private final FakeLlmService llmService;
	private final FakeTtsService ttsService;

	public SseTtsController(FakeLlmService llmService, FakeTtsService ttsService) {
		this.llmService = llmService;
		this.ttsService = ttsService;
	}

	@PostMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> ttsStream(@Valid @RequestBody LlmRequestBody body) {
		return llmService.composeResponse(body.text())
			.flatMapMany(ttsService::synthesizeFullText);
	}

	/**
	 * SSE 데모용 요청 바디 래퍼.
	 */
	public record LlmRequestBody(
		@NotBlank(message = "text는 비어 있을 수 없습니다.")
		String text
	) {
	}
}
