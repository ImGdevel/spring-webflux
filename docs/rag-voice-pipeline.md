# RAG Dialogue Pipeline

## Architecture

```
Text Query → RAG Retrieval → LLM (with context) → Sentence Assembly → TTS → Audio Stream
```

## Flow

1. **Request**: Client sends text query via POST /rag/dialogue/sse
2. **Save**: Store query in conversation history
3. **Retrieve**: Find top 3 similar past conversations (keyword matching)
4. **Augment**: Build prompt with retrieved context
5. **LLM**: Stream response tokens
6. **Assembly**: Buffer tokens into sentences
7. **TTS**: Convert sentences to audio chunks
8. **Response**: Stream Base64-encoded audio via SSE

## Components

### Controller
- **DialogueController**: POST /rag/dialogue/sse → SSE audio stream

### Services
- **DialoguePipelineService**: Orchestrates save → retrieve → LLM → TTS
- **FakeRagRetrievalService**: Keyword-based top-3 search
- **SentenceAssemblyService**: Buffers tokens to sentences

### Repository
- **ConversationHistoryRepository**: ConcurrentHashMap storage

### Clients
- **FakeLlmStreamingClient**: Simulated LLM (300ms delay)
- **FakeTtsStreamingClient**: Simulated TTS (5 chunks, 200ms delay)

## RAG Strategy

Simple keyword matching (no embeddings):
- Tokenize: `query.toLowerCase().split("\\s+")`
- Score: Word overlap count (intersection size)
- Return: Top 3 by score

## Example

Input: "WebFlux란 무엇인가?"

1. Save to history
2. Retrieve: ["WebFlux는 리액티브 프레임워크", "Flux 사용법", ...]
3. Augmented prompt: "Context: [retrieved]\nQuestion: WebFlux란 무엇인가?"
4. LLM → Sentences → TTS → Base64 audio stream
