# webflux-voice-legacy

레거시 Voice 모듈 - 학습 및 비교 목적으로 분리됨

## 개요

이 모듈은 webflux-rag의 Clean Architecture 리팩토링 전 **기존 구현**을 보존한 모듈입니다.

## 목적

1. **학습 자료**: 기존 구조 vs Clean Architecture 비교 학습
2. **참고 구현**: 기존 동작 방식 참조
3. **점진적 마이그레이션**: 필요 시 참조 가능

## 패키지 구조

```
com.study.webflux.voice/
├── controller/         # RagVoiceController (기존 방식)
├── service/           # RagVoicePipelineService (모놀리식 서비스)
│   ├── RagVoicePipelineService
│   ├── SentenceAssemblyService
│   └── FakeRagRetrievalService
├── client/            # LLM/TTS 클라이언트 인터페이스
│   ├── LlmStreamingClient
│   ├── FakeLlmStreamingClient
│   ├── TtsStreamingClient
│   └── SupertoneTtsStreamingClient
├── model/             # 도메인 모델
│   ├── RagVoiceRequest
│   ├── ConversationMessage
│   └── RetrievalResult
├── repository/        # MongoDB Repository
│   └── ConversationHistoryRepository
├── config/            # 설정
│   ├── RagVoiceProperties
│   ├── RedisConfig
│   └── WebConfig
└── common/            # 상수
    └── VoiceConstants
```

## 실행 방법

```bash
# 컴파일
./gradlew :webflux-voice-legacy:compileJava

# 빌드
./gradlew :webflux-voice-legacy:build

# 실행
./gradlew :webflux-voice-legacy:bootRun
```

## API 엔드포인트

- `POST /rag/voice/sse` - SSE 스트리밍 (Base64 인코딩 오디오)
- `POST /rag/voice/audio` - WAV 바이너리
- `POST /rag/voice/audio/wav` - WAV 바이너리
- `POST /rag/voice/audio/mp3` - MP3 바이너리

**포트**: 8082

## 설정

`src/main/resources/application.yml`:
- Redis: localhost:6379
- OpenAI API Key: `OPENAI_API_KEY` 환경 변수
- Supertone API Key: `SUPERTONE_API_KEY` 환경 변수

## 주요 특징

### 기존 구현 방식
- 모든 코드가 `voice/` 패키지에 집중
- RagVoicePipelineService가 모든 로직 처리
- 구체 클래스에 직접 의존
- 하드코딩된 프롬프트

### 문제점 (Clean Architecture와 비교 시)
- ❌ 계층 분리 불명확
- ❌ 의존성 방향 위반 (서비스 → 구체 클라이언트)
- ❌ 단일 책임 원칙 위반
- ❌ 확장성 부족 (LLM 프로바이더 교체 어려움)
- ❌ 테스트 어려움

## Clean Architecture와 비교

새로운 webflux-rag 모듈과 비교하여 다음 차이점을 확인할 수 있습니다:

| 항목 | webflux-voice-legacy (기존) | webflux-rag (Clean Architecture) |
|------|---------------------------|----------------------------------|
| 패키지 구조 | 단일 패키지 | domain/application/infrastructure 분리 |
| 의존성 방향 | 서비스 → 구체 클래스 | 인터페이스 기반 (DIP) |
| 확장성 | 낮음 (하드코딩) | 높음 (Port/Adapter) |
| 테스트 | 어려움 | 쉬움 (Mock 주입) |
| 프롬프트 | 하드코딩 | 외부화 (templates/) |
| LLM 교체 | 코드 수정 필요 | 설정만 변경 |

## 학습 가이드

1. **기존 구조 이해**
   - `RagVoicePipelineService.java` 읽기
   - 모든 로직이 하나의 서비스에 있음을 확인

2. **Clean Architecture와 비교**
   - webflux-rag의 `VoicePipelineService` 와 비교
   - Port/Adapter 패턴 이해

3. **리팩토링 이유 파악**
   - SOLID 원칙 위반 사례 찾기
   - 확장성 문제 확인

## 참고 문서

- [ARCHITECTURE_COMPARISON.md](../webflux-rag/ARCHITECTURE_COMPARISON.md)
- [REFACTORING_STATUS.md](../webflux-rag/REFACTORING_STATUS.md)

## 주의사항

⚠️ **이 모듈은 학습/비교 목적으로만 사용하세요**

- 새로운 기능 개발 시 webflux-rag 사용 권장
- 레거시 코드이므로 유지보수 최소화
- Clean Architecture 버전 참조하여 개선 방향 학습

---

**작성일**: 2025-12-08
**목적**: Clean Architecture 리팩토링 비교 학습
**상태**: 보존 (Preserved for reference)
