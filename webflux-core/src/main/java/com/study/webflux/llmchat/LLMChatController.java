package com.study.webflux.llmchat;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/llmchat")
public class LLMChatController {

    private final LLMChatService service;

    public LLMChatController(LLMChatService service) {
        this.service = service;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody LLMChatRequest request) {
        return service.streamChat(request);
    }
}
