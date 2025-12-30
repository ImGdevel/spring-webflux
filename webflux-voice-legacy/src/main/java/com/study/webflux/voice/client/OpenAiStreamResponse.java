package com.study.webflux.voice.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiStreamResponse(@JsonProperty("choices") List<OpenAiChoice> choices) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiChoice(@JsonProperty("delta") OpenAiDelta delta,
                     @JsonProperty("finish_reason") String finishReason) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiDelta(@JsonProperty("role") String role,
                    @JsonProperty("content") String content) {
}
