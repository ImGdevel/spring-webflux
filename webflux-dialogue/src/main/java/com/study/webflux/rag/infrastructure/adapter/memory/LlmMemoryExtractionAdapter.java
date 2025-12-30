package com.study.webflux.rag.infrastructure.adapter.memory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.webflux.rag.domain.model.conversation.ConversationTurn;
import com.study.webflux.rag.domain.model.llm.CompletionRequest;
import com.study.webflux.rag.domain.model.llm.Message;
import com.study.webflux.rag.domain.model.memory.ExtractedMemory;
import com.study.webflux.rag.domain.model.memory.MemoryExtractionContext;
import com.study.webflux.rag.domain.port.out.LlmPort;
import com.study.webflux.rag.domain.port.out.MemoryExtractionPort;

import reactor.core.publisher.Flux;

@Component
public class LlmMemoryExtractionAdapter implements MemoryExtractionPort {

	private final LlmPort llmPort;
	private final ObjectMapper objectMapper;
	private final String extractionModel;

	public LlmMemoryExtractionAdapter(
		LlmPort llmPort,
		ObjectMapper objectMapper,
		MemoryExtractionConfig config
	) {
		this.llmPort = llmPort;
		this.objectMapper = objectMapper;
		this.extractionModel = config.model();
	}

	@Override
	public Flux<ExtractedMemory> extractMemories(MemoryExtractionContext context) {
		String prompt = buildExtractionPrompt(context);

		List<Message> messages = List.of(
			Message.system(getSystemPrompt()),
			Message.user(prompt)
		);

		CompletionRequest request = CompletionRequest.withMessages(
			messages,
			extractionModel,
			false
		);

		return llmPort.complete(request)
			.flatMapMany(this::parseExtractedMemories);
	}

	private String getSystemPrompt() {
		return """
			You are a memory extraction system. Analyze conversations and extract meaningful memories.

			Extract two types of memories:
			1. EXPERIENTIAL: Personal experiences, events, activities the user has done or plans to do
			2. FACTUAL: Facts about the user (preferences, beliefs, relationships, skills)

			Rules:
			- Only extract NEW information not already in existing memories
			- If existing memory needs importance update, output it with new importance
			- Set importance (0.0-1.0): personal/emotional = higher, general facts = lower
			- Provide brief reasoning for each memory

			Output ONLY valid JSON array:
			[
			  {
			    "type": "EXPERIENTIAL",
			    "content": "clear, concise memory statement",
			    "importance": 0.8,
			    "reasoning": "why this matters"
			  }
			]

			Return empty array [] if no new memories to extract.
			""";
	}

	private String buildExtractionPrompt(MemoryExtractionContext context) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("Recent Conversations:\n");
		for (ConversationTurn turn : context.recentConversations()) {
			prompt.append("User: ").append(turn.query()).append("\n");
			if (turn.response() != null) {
				prompt.append("Assistant: ").append(turn.response()).append("\n");
			}
		}

		if (!context.existingMemories().isEmpty()) {
			prompt.append("\nExisting Memories (for deduplication):\n");
			context.existingMemories().forEach(memory -> {
				prompt.append("- [")
					.append(memory.type())
					.append(", importance: ");
				if (memory.importance() != null) {
					prompt.append(String.format("%.2f", memory.importance()));
				} else {
					prompt.append("N/A");
				}
				prompt.append("] ")
					.append(memory.content())
					.append("\n");
			});
		}

		return prompt.toString();
	}

	private Flux<ExtractedMemory> parseExtractedMemories(String jsonResponse) {
		try {
			String cleaned = jsonResponse.trim();
			if (cleaned.startsWith("```json")) {
				cleaned = cleaned.substring(7);
			}
			if (cleaned.endsWith("```")) {
				cleaned = cleaned.substring(0, cleaned.length() - 3);
			}
			cleaned = cleaned.trim();

			List<MemoryExtractionDto> dtos = objectMapper.readValue(
				cleaned,
				new TypeReference<List<MemoryExtractionDto>>() {
				}
			);

			return Flux.fromIterable(dtos)
				.map(MemoryExtractionDto::toExtractedMemory);
		} catch (Exception e) {
			return Flux.empty();
		}
	}
}
