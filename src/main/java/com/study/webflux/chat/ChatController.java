package com.study.webflux.chat;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Sinks 기반으로 채팅 메시지를 브로드캐스트하고 SSE 스트림으로 구독하게 되는 WebFlux 실습 컨트롤러.
 */
@Validated
@RestController
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/messages")
	public Mono<ChatMessage> publish(@Valid @RequestBody ChatMessageRequest request) {
		return chatService.publish(request);
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ChatMessage> stream() {
		return chatService.stream();
	}
}
