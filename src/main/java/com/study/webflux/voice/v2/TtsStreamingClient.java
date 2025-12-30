package com.study.webflux.voice.v2;

import reactor.core.publisher.Flux;

/**
 * 외부 TTS 스트리밍 API 를 추상화한 클라이언트 인터페이스.
 *
 * <p>
 * 실제 구현에서는 문장을 전송하고, 응답으로 바이너리 오디오 청크 스트림을 받게 된다.
 * </p>
 */
public interface TtsStreamingClient {

	/**
	 * 한 문장을 TTS 서비스로 전송하고, 바이너리 오디오 청크 스트림을 받는다.
	 *
	 * @param sentence TTS 로 변환할 문장
	 * @return 오디오 바이너리 청크 스트림
	 */
	Flux<byte[]> streamAudio(String sentence);
}

