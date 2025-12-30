package com.study.webflux.voice.v2;

import java.util.List;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * LLM 토큰 스트림을 문장 단위 스트림으로 조립하는 서비스.
 */
@Service
public class SentenceAssemblyService {

	/**
	 * 토큰 스트림을 받아 문장 단위로 묶는다.
	 *
	 * @param tokenStream LLM 이 뿌려주는 텍스트 조각/토큰 스트림
	 * @return 문장 단위로 합쳐진 스트림
	 */
	public Flux<String> assemble(Flux<String> tokenStream) {
		return tokenStream
			.bufferUntil(this::isSentenceEnd)
			.filter(list -> !list.isEmpty())
			.map(this::joinTokensToSentence);
	}

	private boolean isSentenceEnd(String token) {
		if (token == null || token.isEmpty()) {
			return false;
		}
		String trimmed = token.trim();
		return trimmed.endsWith(".")
			|| trimmed.endsWith("!")
			|| trimmed.endsWith("?")
			|| trimmed.endsWith("다.");
	}

	private String joinTokensToSentence(List<String> tokens) {
		return String.join("", tokens).trim();
	}
}

