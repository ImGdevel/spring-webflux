package com.study.webflux.rag.domain.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.study.webflux.rag.domain.model.conversation.ConversationContext;
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
	public String buildPromptWithConversation(RetrievalContext context, ConversationContext conversationContext) {
		String contextText = context.isEmpty() ? "" : context.documents().stream()
			.map(doc -> doc.content())
			.collect(Collectors.joining("\n"));

		String conversationHistory = buildConversationHistory(conversationContext);

		return templateLoader.load("rag-conversation-prompt", Map.of(
			"context", contextText,
			"conversation", conversationHistory
		));
	}

	@Override
	public String buildDefaultPrompt() {
		return templateLoader.load("default-prompt");
	}

	private String buildConversationHistory(ConversationContext conversationContext) {
		if (conversationContext.isEmpty()) {
			return "";
		}

		return conversationContext.turns().stream()
			.filter(turn -> turn.response() != null)
			.map(turn -> String.format("User: %s\nAssistant: %s", turn.query(), turn.response()))
			.collect(Collectors.joining("\n\n"));
	}
}
