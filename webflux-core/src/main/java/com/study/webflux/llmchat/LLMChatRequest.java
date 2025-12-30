package com.study.webflux.llmchat;

import jakarta.validation.constraints.NotBlank;

public record LLMChatRequest(@NotBlank String prompt) {
}
