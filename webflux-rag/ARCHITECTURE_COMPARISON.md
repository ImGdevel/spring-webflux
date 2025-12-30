# WebFlux-RAG Architecture Comparison

## Overview
이 문서는 기존 구현과 Clean Architecture 리팩토링 후의 구조를 비교합니다.

---

## 📊 구조 비교

### Before: 기존 구조 (voice/ 패키지)
```
voice/
├── controller/
│   └── RagVoiceController.java          # 컨트롤러
├── service/
│   ├── RagVoicePipelineService.java     # 파이프라인 서비스 (모든 로직)
│   ├── SentenceAssemblyService.java     # 문장 조립 (도메인 로직)
│   └── FakeRagRetrievalService.java     # Mock Retrieval
├── client/
│   ├── LlmStreamingClient.java          # LLM 클라이언트 인터페이스
│   ├── FakeLlmStreamingClient.java      # Mock 구현
│   ├── TtsStreamingClient.java          # TTS 클라이언트 인터페이스
│   └── SupertoneTtsStreamingClient.java # TTS 구현
├── model/
│   ├── RagVoiceRequest.java             # API 요청 모델
│   ├── ConversationMessage.java         # 대화 메시지
│   └── RetrievalResult.java             # 검색 결과
├── repository/
│   └── ConversationHistoryRepository.java # 대화 저장소
└── config/
    └── RagVoiceProperties.java          # 설정

**문제점:**
❌ 계층 분리 불명확 (모든 것이 voice 패키지에 혼재)
❌ 의존성 방향 위반 (서비스가 구체 클라이언트에 의존)
❌ 단일 책임 원칙 위반 (RagVoicePipelineService가 모든 역할 수행)
❌ 확장성 부족 (LLM 프로바이더 교체 어려움)
❌ 하드코딩된 프롬프트
❌ 테스트 어려움
```

### After: Clean Architecture (domain/, application/, infrastructure/)
```
webflux-rag/
├── domain/                              # 🟦 도메인 레이어 (핵심)
│   ├── model/
│   │   ├── voice/                       # Voice 도메인 모델
│   │   │   ├── Voice.java               # 불변 객체, 빌더 패턴
│   │   │   ├── VoiceSettings.java       # Value Object (Record)
│   │   │   ├── AudioFormat.java         # Enum
│   │   │   └── VoiceStyle.java          # Enum
│   │   ├── llm/                         # LLM 도메인 모델
│   │   │   ├── CompletionRequest.java   # 프로바이더 독립적
│   │   │   ├── CompletionResponse.java
│   │   │   ├── Message.java             # Record with factory
│   │   │   └── MessageRole.java         # Enum
│   │   ├── rag/                         # RAG 도메인 모델
│   │   │   ├── RetrievalContext.java
│   │   │   ├── RetrievalDocument.java
│   │   │   └── SimilarityScore.java    # Value Object
│   │   └── conversation/                # 대화 도메인 모델
│   │       ├── ConversationTurn.java    # 대화 턴
│   │       └── ConversationContext.java # 대화 컨텍스트
│   ├── port/
│   │   ├── in/                          # Inbound Port (Use Cases)
│   │   │   └── VoicePipelineUseCase.java
│   │   └── out/                         # Outbound Port (추상화)
│   │       ├── LlmPort.java             # LLM 추상화
│   │       ├── TtsPort.java             # TTS 추상화
│   │       ├── RetrievalPort.java       # Retrieval 추상화
│   │       ├── ConversationRepository.java
│   │       └── PromptTemplatePort.java
│   └── service/                         # 도메인 서비스
│       ├── SentenceAssembler.java       # 순수 도메인 로직
│       └── PromptBuilder.java           # 프롬프트 구성
│
├── application/                         # 🟩 애플리케이션 레이어
│   └── service/
│       └── VoicePipelineService.java    # Use Case 구현
│
├── infrastructure/                      # 🟨 인프라 레이어
│   ├── adapter/
│   │   ├── llm/                         # LLM 어댑터
│   │   │   ├── OpenAiLlmAdapter.java    # OpenAI 완전 구현
│   │   │   ├── ClaudeLlmAdapter.java    # 확장 가능 (스텁)
│   │   │   ├── GeminiLlmAdapter.java    # 확장 가능 (스텁)
│   │   │   ├── OpenAiConfig.java
│   │   │   └── dto/
│   │   ├── tts/                         # TTS 어댑터
│   │   │   ├── SupertoneTtsAdapter.java # Voice 외부 주입
│   │   │   └── SupertoneConfig.java
│   │   ├── retrieval/                   # Retrieval 어댑터
│   │   │   └── InMemoryRetrievalAdapter.java
│   │   └── persistence/                 # 영속성 어댑터
│   │       └── mongodb/
│   │           ├── ConversationEntity.java
│   │           ├── ConversationMongoRepository.java
│   │           └── ConversationMongoAdapter.java
│   ├── config/                          # 설정
│   │   ├── VoiceConfiguration.java      # Voice Bean 생성
│   │   ├── LlmConfiguration.java        # LLM Port Bean
│   │   └── TtsConfiguration.java        # TTS Port Bean
│   └── template/
│       └── FileBasedPromptTemplate.java # 템플릿 로더
│
└── voice/                               # 🔵 레거시 코드 (보존)
    └── (기존 구현 - 학습/비교 목적)

**개선사항:**
✅ 명확한 계층 분리 (Domain ← Application ← Infrastructure)
✅ 의존성 역전 (Domain이 중심, Infrastructure가 의존)
✅ 단일 책임 원칙 준수 (각 클래스가 하나의 역할)
✅ 개방-폐쇄 원칙 (Port로 확장, 구현 교체 가능)
✅ 프롬프트 외부화 (resources/templates/)
✅ 테스트 용이성 (Mock/Stub 주입 가능)
```

---

## 🔄 데이터 흐름 비교

### Before: 직접 의존성
```
RagVoiceController
    ↓ (직접 의존)
RagVoicePipelineService
    ↓ (직접 의존)
FakeLlmStreamingClient ← 교체 어려움
    ↓
SupertoneTtsStreamingClient ← 교체 어려움
```

### After: 의존성 역전
```
RagVoiceController
    ↓ (인터페이스 의존)
VoicePipelineUseCase (Port)
    ↑ (구현)
VoicePipelineService (Application)
    ↓ (인터페이스 의존)
LlmPort, TtsPort, RetrievalPort (Domain Ports)
    ↑ (구현)
OpenAiLlmAdapter, SupertoneTtsAdapter, ... (Infrastructure)
```

**핵심 차이:**
- Domain 계층이 중심 (의존성 없음)
- Application/Infrastructure가 Domain에 의존
- Port를 통한 추상화로 구현 교체 용이

---

## 💡 SOLID 원칙 적용

### 1. Single Responsibility Principle (SRP)
**Before:**
```java
// RagVoicePipelineService가 4가지 책임 수행
class RagVoicePipelineService {
    // 1. 대화 저장
    // 2. RAG 검색
    // 3. LLM 호출
    // 4. TTS 변환
    // 5. 스트림 조립
}
```

**After:**
```java
// 각 클래스가 단일 책임
class VoicePipelineService { /* 오케스트레이션만 */ }
class OpenAiLlmAdapter { /* LLM 통신만 */ }
class SupertoneTtsAdapter { /* TTS 통신만 */ }
class SentenceAssembler { /* 문장 조립만 */ }
class PromptBuilder { /* 프롬프트 구성만 */ }
```

### 2. Open-Closed Principle (OCP)
**Before:**
```java
// LLM 프로바이더 변경 시 RagVoicePipelineService 수정 필요
class RagVoicePipelineService {
    private FakeLlmStreamingClient llmClient; // 하드코딩
}
```

**After:**
```java
// 새 프로바이더 추가 시 기존 코드 수정 불필요
interface LlmPort { ... }

// OpenAI 구현
class OpenAiLlmAdapter implements LlmPort { ... }

// Claude 추가 (기존 코드 수정 없음)
class ClaudeLlmAdapter implements LlmPort { ... }

// Gemini 추가 (기존 코드 수정 없음)
class GeminiLlmAdapter implements LlmPort { ... }
```

### 3. Liskov Substitution Principle (LSP)
**After:**
```java
// 모든 LlmPort 구현체는 동일하게 동작
VoicePipelineService service = new VoicePipelineService(
    openAiAdapter,  // LlmPort
    // OR
    claudeAdapter,  // LlmPort
    // OR
    geminiAdapter   // LlmPort
);
```

### 4. Interface Segregation Principle (ISP)
**After:**
```java
// 각 Port는 특정 역할만 정의
interface LlmPort {
    Flux<String> streamCompletion(CompletionRequest);
}

interface TtsPort {
    Flux<byte[]> streamSynthesize(String text);
}

interface RetrievalPort {
    Mono<RetrievalContext> retrieve(String query, int topK);
}
```

### 5. Dependency Inversion Principle (DIP)
**Before:**
```java
// 고수준 모듈이 저수준 모듈에 의존
class RagVoicePipelineService {
    private FakeLlmStreamingClient llmClient; // 구체 클래스 의존
}
```

**After:**
```java
// 고수준, 저수준 모두 추상화(Port)에 의존
class VoicePipelineService {
    private final LlmPort llmPort; // 인터페이스 의존
}

class OpenAiLlmAdapter implements LlmPort { ... }
```

---

## 🔧 확장성 비교

### LLM 프로바이더 변경

**Before:**
1. `FakeLlmStreamingClient` 수정 또는 교체
2. `RagVoicePipelineService` 수정
3. 기존 코드 테스트 재수행

**After:**
1. 새 Adapter 클래스 작성 (예: `ClaudeLlmAdapter`)
2. Configuration에서 Bean 변경
3. **기존 코드 수정 불필요**

### TTS 프로바이더 변경

**Before:**
1. `SupertoneTtsStreamingClient` 수정
2. `RagVoicePipelineService` 수정 가능성

**After:**
1. 새 Adapter 작성 (예: `ElevenLabsTtsAdapter`)
2. `TtsConfiguration`에서 Bean 변경
3. **기존 코드 수정 불필요**

---

## 🧪 테스트 용이성 비교

### Before: Mock 어려움
```java
@Test
void testPipeline() {
    // 구체 클래스 의존으로 Mock 어려움
    RagVoicePipelineService service = new RagVoicePipelineService(
        fakeLlmClient,  // 교체 어려움
        ttsClient,      // 교체 어려움
        repository
    );
}
```

### After: 간편한 Mock
```java
@Test
void testPipeline() {
    // Port 인터페이스로 쉽게 Mock
    LlmPort mockLlm = mock(LlmPort.class);
    TtsPort mockTts = mock(TtsPort.class);

    VoicePipelineService service = new VoicePipelineService(
        mockLlm,
        mockTts,
        mockRetrieval,
        mockRepository,
        mockPromptTemplate,
        mockSentenceAssembler
    );

    // 각 포트 동작 검증 가능
}
```

---

## 📝 프롬프트 관리 비교

### Before: 하드코딩
```java
String prompt = """
    이전 대화 맥락:
    %s

    위 맥락을 참고하여 사용자의 질문에 자연스럽게 대답하세요.
    """.formatted(context);
```

### After: 외부화
```
resources/templates/rag-augmented-prompt.txt:
이전 대화 맥락:
{{context}}

위 맥락을 참고하여 사용자의 질문에 자연스럽게 대답하세요.
```

**장점:**
- 재컴파일 없이 수정 가능
- 다국어 지원 용이
- 버전 관리 명확

---

## 🎯 마이그레이션 전략

### Phase 6 완료 상태
```
✅ Controller: 새 구현 사용 (VoicePipelineUseCase)
✅ Application: 새 서비스 작동 (VoicePipelineService)
✅ Domain: 모든 모델 & 포트 정의
✅ Infrastructure: 모든 어댑터 구현
🔵 Legacy: 기존 voice/ 패키지 유지 (학습/비교)
```

### 현재 상태
- API 엔드포인트: 동일 (하위 호환성 100%)
- 내부 구현: Clean Architecture
- 기존 코드: 삭제하지 않고 보존

---

## 📊 성능 및 유지보수성

### 성능
- **변화 없음**: Reactive 스트리밍 패턴 유지
- Scheduler 사용: 동일 (boundedElastic)
- Backpressure 처리: 동일

### 유지보수성
- ✅ 코드 가독성 향상 (명확한 계층)
- ✅ 변경 영향 범위 축소 (Port 격리)
- ✅ 새 기능 추가 용이
- ✅ 버그 수정 범위 명확

---

## 🚀 향후 확장 시나리오

### 1. Claude LLM 추가
```java
@Component
public class ClaudeLlmAdapter implements LlmPort {
    // Claude API 호출 구현
}

// Configuration에서 변경
@Bean
public LlmPort llmPort(...) {
    return new ClaudeLlmAdapter(...);
}
```

### 2. ElevenLabs TTS 추가
```java
@Component
public class ElevenLabsTtsAdapter implements TtsPort {
    // ElevenLabs API 호출 구현
}
```

### 3. Vector DB Retrieval 추가
```java
@Component
public class PineconeRetrievalAdapter implements RetrievalPort {
    // Pinecone 벡터 검색 구현
}
```

**핵심: 기존 코드 수정 없이 확장 가능**

---

## 📚 참고 자료

- Clean Architecture Plan: `/Users/devon.woo/.claude/plans/cozy-weaving-summit.md`
- Refactoring Status: `REFACTORING_STATUS.md`
- Clean Architecture by Robert C. Martin

---

**작성일**: 2025-12-08
**목적**: 기존 구조와 Clean Architecture 비교 학습
