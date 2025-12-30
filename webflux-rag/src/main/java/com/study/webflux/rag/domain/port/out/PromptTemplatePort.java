package com.study.webflux.rag.domain.port.out;

import com.study.webflux.rag.domain.model.rag.RetrievalContext;

public interface PromptTemplatePort {
	String buildPrompt(RetrievalContext context);

	String buildDefaultPrompt();
}
