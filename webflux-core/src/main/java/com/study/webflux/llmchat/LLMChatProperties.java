package com.study.webflux.llmchat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "llmchat")
public class LLMChatProperties {

    private String apiKey;

    @NotBlank
    private String baseUrl = "https://api.openai.com/v1";

    @NotBlank
    private String model = "gpt-3.5-turbo";

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

}
