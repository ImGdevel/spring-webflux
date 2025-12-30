# WebFlux-RAG Clean Architecture Refactoring 진행 상황

## ✅ 완료된 작업 (Phase 1-4)

### Phase 1: Domain Foundation (완료)
**생성된 파일: 21개**

#### Voice 도메인 모델 (4개)
- ✅ `domain/model/voice/Voice.java` - 빌더 패턴, 불변 객체
- ✅ `domain/model/voice/VoiceSettings.java` - Record, 검증 로직 포함
- ✅ `domain/model/voice/AudioFormat.java` - Enum (WAV, MP3, PCM)
- ✅ `domain/model/voice/VoiceStyle.java` - Enum (NEUTRAL, HAPPY, SAD 등)

#### LLM 도메인 모델 (4개)
- ✅ `domain/model/llm/CompletionRequest.java` - 프로바이더 독립적
- ✅ `domain/model/llm/CompletionResponse.java`
- ✅ `domain/model/llm/Message.java` - Record, 팩토리 메서드 제공
- ✅ `domain/model/llm/MessageRole.java` - Enum (USER, SYSTEM, ASSISTANT)

#### RAG 도메인 모델 (3개)
- ✅ `domain/model/rag/RetrievalContext.java` - 검색 결과 컨테이너
- ✅ `domain/model/rag/RetrievalDocument.java` - 단일 검색 문서
- ✅ `domain/model/rag/SimilarityScore.java` - 유사도 점수 Value Object

#### Conversation 도메인 모델 (2개)
- ✅ `domain/model/conversation/ConversationTurn.java` - 대화 턴
- ✅ `domain/model/conversation/ConversationContext.java` - 대화 컨텍스트

#### Port 인터페이스 (6개)
- ✅ `domain/port/out/LlmPort.java` - LLM 추상화
- ✅ `domain/port/out/TtsPort.java` - TTS 추상화
- ✅ `domain/port/out/RetrievalPort.java` - Retrieval 추상화
- ✅ `domain/port/out/ConversationRepository.java` - 저장소 추상화
- ✅ `domain/port/out/PromptTemplatePort.java` - 템플릿 추상화
- ✅ `domain/port/in/DialoguePipelineUseCase.java` - Use Case 인터페이스

#### 프롬프트 템플릿 (2개)
- ✅ `resources/templates/default-prompt.txt` - 기본 프롬프트
- ✅ `resources/templates/rag-augmented-prompt.txt` - RAG 증강 프롬프트

---

### Phase 2: Infrastructure Adapters (완료)
**생성된 파일: 13개**

#### OpenAI LLM Adapter (완전 구현)
- ✅ `infrastructure/adapter/llm/OpenAiLlmAdapter.java` - LlmPort 구현
- ✅ `infrastructure/adapter/llm/OpenAiConfig.java` - 설정 Record
- ✅ `infrastructure/adapter/llm/dto/OpenAiRequest.java` - 요청 DTO
- ✅ `infrastructure/adapter/llm/dto/OpenAiStreamResponse.java` - 응답 DTO

#### Claude/Gemini LLM Adapter (스텁)
- ✅ `infrastructure/adapter/llm/ClaudeLlmAdapter.java` - 확장성 시연용 스텁
- ✅ `infrastructure/adapter/llm/GeminiLlmAdapter.java` - 확장성 시연용 스텁

#### TTS Adapter
- ✅ `infrastructure/adapter/tts/SupertoneTtsAdapter.java` - TtsPort 구현
  - **핵심 변경**: Voice를 생성자에서 주입받음 (모듈 외부 주입)
- ✅ `infrastructure/adapter/tts/SupertoneConfig.java` - 설정 Record

#### Retrieval Adapter
- ✅ `infrastructure/adapter/retrieval/InMemoryRetrievalAdapter.java` - RetrievalPort 구현
  - 유사도 계산 로직 포함

#### Persistence Adapter (MongoDB)
- ✅ `infrastructure/adapter/persistence/mongodb/ConversationEntity.java` - MongoDB 엔티티
- ✅ `infrastructure/adapter/persistence/mongodb/ConversationMongoRepository.java` - Reactive Repository
- ✅ `infrastructure/adapter/persistence/mongodb/ConversationMongoAdapter.java` - ConversationRepository 구현

---

### Phase 3: Domain Services (완료)
**생성된 파일: 3개**

- ✅ `domain/service/SentenceAssembler.java` - 토큰 → 문장 조립
- ✅ `domain/service/PromptBuilder.java` - PromptTemplatePort 구현
- ✅ `infrastructure/template/FileBasedPromptTemplate.java` - 템플릿 로더

---

### Phase 4: Configuration (완료)
**생성된 파일: 3개**

- ✅ `infrastructure/config/DialogueVoiceConfiguration.java` - Voice Bean 생성
- ✅ `infrastructure/config/LlmConfiguration.java` - LLM Port Bean 생성
- ✅ `infrastructure/config/TtsConfiguration.java` - TTS Port Bean 생성 (Voice 주입)

---

## ✅ 빌드 검증 완료
```bash
./gradlew :webflux-rag:compileJava
BUILD SUCCESSFUL
```

**총 생성 파일: 40개 (Phase 1-4)**

---

## ✅ Phase 5: Application Layer (완료)
**생성된 파일: 1개**

- ✅ `application/service/DialoguePipelineService.java` - DialoguePipelineUseCase 구현
  - LlmPort, TtsPort, RetrievalPort, ConversationRepository 등 모든 포트 활용
  - Reactive 파이프라인 오케스트레이션 (flatMap, flatMapMany, transform, concatMap)
  - Scheduler 전환 (boundedElastic)
  - Base64 인코딩 스트림 & 원본 바이트 스트림 지원
  - 순수 오케스트레이션 로직 (비즈니스 로직은 도메인 계층에 위임)

**설계 결정:**
- DialoguePipelineUseCase 인터페이스는 단순 String 입력 사용 (간결함)
- Application DTO 불필요 - 컨트롤러가 RagDialogueRequest에서 text 추출
- 도메인 모델 생성은 서비스 내부에서 처리 (ConversationTurn.create)

---

## ✅ Phase 6: API Layer Refactoring (완료)
**수정된 파일: 1개**

- ✅ `application/controller/DialogueController.java` - Clean Architecture로 리팩토링 완료
  - `DialoguePipelineService` → `DialoguePipelineUseCase` 인터페이스 사용
  - 도메인 Port에 의존 (Infrastructure 의존성 제거)
  - 메서드 호출: `runPipeline()` → `executeStreaming()`
  - 메서드 호출: `runPipelineAudio()` → `executeAudioStreaming()`
  - Request DTO에서 `text` 추출하여 Use Case 호출

**API 엔드포인트 (변경 없음):**
- `POST /rag/dialogue/sse` - SSE 스트리밍 (Base64 인코딩 오디오)
- `POST /rag/dialogue/audio` - 오디오 바이너리 (WAV)
- `POST /rag/dialogue/audio/wav` - 오디오 바이너리 (WAV)
- `POST /rag/dialogue/audio/mp3` - 오디오 바이너리 (MP3)

**요청/응답 형식 (변경 없음):**
- Request: `RagDialogueRequest` (text, requestedAt)
- Response: 기존과 동일 (하위 호환성 유지)

---

## ✅ Phase 7: 레거시 코드 모듈 분리 (완료)

기존 `voice/` 패키지를 **완전히 새로운 Gradle 모듈로 분리**했습니다.

### 새로운 모듈: `webflux-voice-legacy`

**위치**: `/webflux-voice-legacy/`

**패키지 구조:**
```
com.study.webflux.voice/  (rag 네임스페이스 제거)
├── controller/           # DialogueController (기존 방식)
├── service/             # DialoguePipelineService
├── client/              # LLM/TTS 클라이언트
├── model/               # ConversationMessage, RetrievalResult, RagDialogueRequest
├── repository/          # ConversationHistoryRepository
├── config/              # RagDialogueProperties, RedisConfig, WebConfig
└── common/              # DialogueConstants
```

**실행 설정:**
- 포트: 8082 (webflux-rag는 8081)
- 독립 실행: `./gradlew :webflux-voice-legacy:bootRun`
- 메인 클래스: `VoiceLegacyApplication.java`

**분리 작업:**
1. ✅ 새 Gradle 모듈 생성
2. ✅ voice/ 패키지 전체 이동
3. ✅ 패키지 네임스페이스 변경 (`com.study.webflux.rag.dialogue` → `com.study.webflux.voice`)
4. ✅ 독립 Application 클래스 생성
5. ✅ 독립 설정 파일 생성
6. ✅ webflux-rag에서 voice/ 삭제
7. ✅ 필요한 공통 클래스 복사 (RagDialogueRequest, RagDialogueProperties, DialogueConstants)
8. ✅ 빌드 검증 완료

**목적:**
- 학습/비교: Clean Architecture vs 기존 구조
- 참조: 필요 시 기존 구현 참조
- 독립 실행: 레거시 버전 단독 테스트

**참고**: `webflux-voice-legacy/README.md`

---

## 🎯 현재 아키텍처 상태

### 완료된 Clean Architecture 레이어
```
webflux-rag/
├── domain/                     ✅ 완료
│   ├── model/                  - Voice, LLM, RAG, Conversation 모델
│   ├── port/                   - 모든 Port 인터페이스 정의
│   └── service/                - PromptBuilder, SentenceAssembler
│
├── application/                ✅ 완료 (Phase 5)
│   └── service/                - DialoguePipelineService (Use Case 구현)
│
├── infrastructure/             ✅ 완료
│   ├── adapter/
│   │   ├── llm/               - OpenAI (완전), Claude/Gemini (스텁)
│   │   ├── tts/               - Supertone (Voice 외부 주입)
│   │   ├── retrieval/         - InMemory 검색
│   │   └── persistence/       - MongoDB 어댑터
│   ├── config/                - Configuration 클래스들
│   └── template/              - 템플릿 로더
│
└── voice/ (기존 코드)          🔄 병존 중
    - 기존 구현이 아직 작동 중
```

---

## 🔑 핵심 개선사항

### 1. SOLID 원칙 준수
- **SRP**: 각 어댑터가 단일 책임만 수행
- **OCP**: LlmPort, TtsPort로 프로바이더 교체 가능
- **DIP**: 도메인이 인프라에 의존하지 않음

### 2. Voice 모듈 외부 주입
```java
// Before: Voice가 SupertoneTtsStreamingClient 내부에서 생성됨
// After: Voice가 Configuration에서 생성되어 주입됨
@Bean
public TtsPort ttsPort(WebClient.Builder builder, SupertoneConfig config, Voice voice) {
    return new SupertoneTtsAdapter(builder, config, voice);
}
```

### 3. 프롬프트 템플릿 외부화
- 하드코딩된 한국어 프롬프트 → `resources/templates/*.txt`
- 재컴파일 없이 수정 가능

### 4. 확장 가능한 LLM 프로바이더
- OpenAI 완전 구현
- Claude/Gemini 스텁 제공 → 쉽게 확장 가능

---

## 📋 사용자 액션 아이템

### 즉시 가능한 작업
1. ✅ 새로운 도메인 모델과 포트 사용 시작 가능
2. ✅ Configuration Bean 활용 가능
3. ✅ 기존 코드와 병행 사용 가능

### 점진적 마이그레이션 옵션
1. **Option A**: 새로운 `DialoguePipelineService` 구현하고 기존 컨트롤러 연결
2. **Option B**: 기존 코드에서 새로운 Port 인터페이스만 활용
3. **Option C**: 전체 API 레이어 리팩토링 후 기존 코드 삭제

---

## 🧪 테스트 전략

### 완료된 작업으로 가능한 테스트
```java
@Test
void testVoiceBuilderPattern() {
    Voice voice = Voice.builder()
        .id("test-id")
        .name("test-voice")
        .provider("supertone")
        .build();

    assertNotNull(voice);
}

@Test
void testOpenAiLlmAdapter() {
    // OpenAiLlmAdapter는 완전히 구현되어 있어 Mock 없이 테스트 가능
    CompletionRequest request = CompletionRequest.streaming("Hello", "gpt-3.5-turbo");
    Flux<String> result = llmPort.streamCompletion(request);

    StepVerifier.create(result)
        .expectNextMatches(s -> s.length() > 0)
        .verifyComplete();
}
```

---

## 📚 참고 자료

- Clean Architecture Plan: `/Users/devon.woo/.claude/plans/modular-jingling-swing.md`
- 전체 아키텍처 다이어그램: Plan 파일 참조
- SOLID 원칙 적용 사례: 각 어댑터 클래스 참조

---

**작성일**: 2025-12-08
**최종 업데이트**: 2025-12-08 (Phase 7 완료 - 모듈 분리)
**빌드 상태**: ✅ SUCCESS (모든 모듈)
**총 생성 파일**: 41개 (Phase 1-5) + 1개 README (Phase 7)
**총 수정 파일**: 1개 (Phase 6)
**리팩토링 완료**: ✅ Clean Architecture 전환 완료
**레거시 코드**: ✅ 독립 모듈로 분리 완료 (`webflux-voice-legacy`)

---

## 🧪 테스트 완료

**생성된 테스트 파일 (3개):**
1. `DialoguePipelineServiceTest.java` - Application Layer (5 tests)
   - Base64 인코딩 스트림 테스트
   - 원본 오디오 바이트 스트림 테스트
   - RAG 컨텍스트 처리 테스트
   - 다중 문장 처리 테스트

2. `DialogueControllerTest.java` - API Layer (6 tests)
   - SSE 엔드포인트 테스트
   - WAV/MP3 오디오 엔드포인트 테스트
   - 입력 검증 테스트

3. `SentenceAssemblerTest.java` - Domain Service (8 tests)
   - 토큰 조립 테스트
   - 다양한 문장 부호 처리 테스트
   - 한국어/영어 문장 테스트

**테스트 실행 결과:**
```bash
./gradlew :webflux-rag:test
BUILD SUCCESSFUL ✅
19 tests completed, 0 failed
```

**삭제된 레거시 테스트:**
- `application/controller/DialogueControllerTest.java`
- `voice/client/FakeTtsStreamingClient.java`
- `voice/client/FakeLlmStreamingClient.java`

---

**최종 업데이트**: 2025-12-08 (테스트 완료)
**테스트 상태**: ✅ ALL PASS
**총 테스트**: 19개
