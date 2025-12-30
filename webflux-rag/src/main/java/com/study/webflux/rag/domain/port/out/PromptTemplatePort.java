package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.conversation.ConversationContext;
import com.study.webflux.rag.domain.model.rag.RetrievalContext;

public interface PromptTemplatePort {
	String buildPrompt(RetrievalContext context);

	String buildPromptWithConversation(RetrievalContext context, ConversationContext conversationContext);

	String buildDefaultPrompt();
}
