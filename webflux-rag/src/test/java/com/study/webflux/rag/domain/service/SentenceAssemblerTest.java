package com.study.webflux.rag.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class SentenceAssemblerTest {

	private SentenceAssembler sentenceAssembler;

	@BeforeEach
	void setUp() {
		sentenceAssembler = new SentenceAssembler();
	}

	@Test
	void assemble_shouldCombineTokensIntoSentences() {
		Flux<String> tokens = Flux.just("Hello", " ", "world", ".");

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("Hello world.")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleMultipleSentences() {
		Flux<String> tokens = Flux.just(
			"First", " sentence", ".",
			" Second", " sentence", "!"
		);

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("First sentence.")
			.expectNext("Second sentence!")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleKoreanSentences() {
		Flux<String> tokens = Flux.just(
			"안녕", "하세요", ".",
			" 반갑", "습니", "다", "."
		);

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("안녕하세요.")
			.expectNext("반갑습니다.")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleQuestionMarks() {
		Flux<String> tokens = Flux.just("How", " are", " you", "?");

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("How are you?")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleExclamationMarks() {
		Flux<String> tokens = Flux.just("Great", " news", "!");

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("Great news!")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleTokensWithSpaces() {
		Flux<String> tokens = Flux.just("Hello", " ", "world", ".");

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("Hello world.")
			.verifyComplete();
	}

	@Test
	void assemble_shouldHandleMixedPunctuation() {
		Flux<String> tokens = Flux.just(
			"First", ".",
			" Second", "!",
			" Third", "?"
		);

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.expectNext("First.")
			.expectNext("Second!")
			.expectNext("Third?")
			.verifyComplete();
	}

	@Test
	void assemble_withEmptyFlux_shouldReturnEmpty() {
		Flux<String> tokens = Flux.empty();

		Flux<String> result = sentenceAssembler.assemble(tokens);

		StepVerifier.create(result)
			.verifyComplete();
	}
}
