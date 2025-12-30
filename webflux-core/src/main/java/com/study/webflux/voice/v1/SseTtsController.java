package com.study.webflux.voice.v1;

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
 *
 * <p>
 * - 클라이언트가 텍스트를 보내면 {@link FakeLlmService} 로 LLM 응답을 만들고, <br>
 * - 그 결과 문장을 {@link FakeTtsService} 로 "오디오 청크" 문자열 스트림으로 바꿔 SSE 로 전달한다.
 * </p>
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

	/**
	 * 텍스트를 입력받아 LLM → TTS 파이프라인 결과를 SSE 로 스트리밍한다.
	 *
	 * @param body 사용자가 입력한 텍스트를 담은 요청 바디
	 * @return TTS 가 생성한 "오디오 청크" 문자열의 SSE 스트림
	 */
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
