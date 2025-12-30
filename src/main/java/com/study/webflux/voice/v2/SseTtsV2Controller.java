package com.study.webflux.voice.v2;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

/**
 * v2 음성 파이프라인을 SSE 로 노출하는 컨트롤러.
 *
 * <p>
 * - 클라이언트로부터 {@link VoiceV2Request} 를 받고<br>
 * - {@link VoicePipelineV2Service} 를 통해 LLM → TTS 파이프라인을 실행한 뒤<br>
 * - 설정(`voice.v2.chunk-size`)에서 정의한 크기만큼 묶인 TTS 바이너리 청크를 Base64 문자열로 인코딩해 SSE 스트림으로 반환한다.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/voice/v2")
public class SseTtsV2Controller {

	private final VoicePipelineV2Service pipelineService;

	public SseTtsV2Controller(VoicePipelineV2Service pipelineService) {
		this.pipelineService = pipelineService;
	}

	/**
	 * v2 음성 파이프라인 엔드포인트.
	 *
	 * @param request 사용자 텍스트와 요청 시간을 담은 DTO
	 * @return TTS 바이너리 청크를 Base64 로 인코딩한 SSE 스트림
	 */
	@PostMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> ttsStream(
		@Valid @RequestBody VoiceV2Request request
	) {
		return pipelineService.runPipelineBase64(request);
	}
}
