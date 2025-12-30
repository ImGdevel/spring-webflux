package com.study.webflux.rag.domain.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.study.webflux.rag.domain.model.rag.RetrievalContext;
import com.study.webflux.rag.domain.port.out.PromptTemplatePort;
import com.study.webflux.rag.infrastructure.template.FileBasedPromptTemplate;

@Component
public class PromptBuilder implements PromptTemplatePort {

	private final FileBasedPromptTemplate templateLoader;

	public PromptBuilder(FileBasedPromptTemplate templateLoader) {
		this.templateLoader = templateLoader;
	}

	@Override
	public String buildPrompt(RetrievalContext context) {
		if (context.isEmpty()) {
			return buildDefaultPrompt();
		}

		String contextText = context.documents().stream()
			.map(doc -> doc.content())
			.collect(Collectors.joining("\n"));

		return templateLoader.load("rag-augmented-prompt", Map.of("context", contextText));
	}

	@Override
	public String buildDefaultPrompt() {
		return templateLoader.load("default-prompt");
	}
}
