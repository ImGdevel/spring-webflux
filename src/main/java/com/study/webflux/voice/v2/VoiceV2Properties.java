package com.study.webflux.voice.v2;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * voice v2 파이프라인 관련 설정을 보관하는 프로퍼티.
 */
@ConfigurationProperties(prefix = "voice.v2")
public class VoiceV2Properties {

	/**
	 * 클라이언트에게 전달할 오디오 청크 크기(바이트).
	 */
	private int chunkSize = 1024;

	/**
	 * 외부 LLM 스트리밍 API 호출 타임아웃.
	 */
	private Duration llmTimeout = Duration.ofSeconds(5);

	/**
	 * 외부 TTS 스트리밍 API 호출 타임아웃.
	 */
	private Duration ttsTimeout = Duration.ofSeconds(5);

	/**
	 * LLM 호출 실패 시 재시도 횟수.
	 */
	private int llmRetryAttempts = 0;

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public Duration getLlmTimeout() {
		return llmTimeout;
	}

	public void setLlmTimeout(Duration llmTimeout) {
		this.llmTimeout = llmTimeout;
	}

	public Duration getTtsTimeout() {
		return ttsTimeout;
	}

	public void setTtsTimeout(Duration ttsTimeout) {
		this.ttsTimeout = ttsTimeout;
	}

	public int getLlmRetryAttempts() {
		return llmRetryAttempts;
	}

	public void setLlmRetryAttempts(int llmRetryAttempts) {
		this.llmRetryAttempts = llmRetryAttempts;
	}
}
