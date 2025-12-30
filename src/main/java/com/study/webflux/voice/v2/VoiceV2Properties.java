package com.study.webflux.voice.v2;

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

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
}

