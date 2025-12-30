package com.study.webflux.voice.v2;

/**
 * 음성 파이프라인 처리 중 발생한 예외를 감싸는 런타임 예외.
 */
public class VoicePipelineException extends RuntimeException {

	public VoicePipelineException(String message) {
		super(message);
	}

	public VoicePipelineException(String message, Throwable cause) {
		super(message, cause);
	}
}

