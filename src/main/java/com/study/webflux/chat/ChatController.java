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
 * Sinks 기반으로 채팅 메시지를 브로드캐스트하고
 * SSE 스트림으로 구독하게 되는 WebFlux 실습 컨트롤러.
 *
 * <p>
 * - {@link #publish(ChatMessageRequest)}: HTTP POST 로 들어온 채팅 메시지를 발행하고 바로 되돌려준다. <br>
 * - {@link #stream()}: 지금까지 발행된 채팅 메시지를 구독하는 SSE 스트림 엔드포인트.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	/**
	 * 채팅 메시지를 발행하는 POST 엔드포인트.
	 *
	 * @param request 사용자명과 메시지 내용을 담은 요청 바디
	 * @return 브로드캐스트된 최종 {@link ChatMessage}
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/messages")
	public Mono<ChatMessage> publish(@Valid @RequestBody ChatMessageRequest request) {
		return chatService.publish(request);
	}

	/**
	 * 발행된 채팅 메시지를 실시간으로 구독하는 SSE 스트림.
	 *
	 * @return 새로 발행되는 모든 {@link ChatMessage} 의 스트림
	 */
	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ChatMessage> stream() {
		return chatService.stream();
	}
}
