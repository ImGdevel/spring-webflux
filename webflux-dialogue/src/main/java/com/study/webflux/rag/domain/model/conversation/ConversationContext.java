package com.study.webflux.rag.domain.model.conversation;

import java.util.List;

public record ConversationContext(
	List<ConversationTurn> turns
) {
	public ConversationContext {
		if (turns == null) {
			turns = List.of();
		}
	}

	public boolean isEmpty() {
		return turns.isEmpty();
	}

	public int size() {
		return turns.size();
	}

	public static ConversationContext empty() {
		return new ConversationContext(List.of());
	}

	public static ConversationContext of(List<ConversationTurn> turns) {
		return new ConversationContext(turns);
	}
}
