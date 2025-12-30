# WebFlux Reactive Study

리액티브 스트림과 Spring WebFlux를 실습하며 정리하는 저장소입니다. Mono/Flux 기초부터 SSE, 백프레셔, 외부 API 호출, 간단한 음성(LLM→TTS) 파이프라인까지 한 곳에서 실험할 수 있도록 구성했습니다.

## Tech Stack
- Java 21, Spring Boot 3.4 (WebFlux, Validation)
- Reactor (Flux/Mono, Sinks, backpressure operators)
- Gradle Wrapper

## 실행 방법
```bash
./gradlew bootRun    # 8080 포트 기본
```

## 주요 데모 엔드포인트
- 시간 스트리밍: `/time/simple/stream`, `/time/simple/counter` (기본 SSE), `/time/stream`, `/time/counter` (Reactor Context에 traceId 포함)
- 채팅 브로드캐스트: `POST /chat/messages` {user, message} → `GET /chat/stream` SSE 로 모든 메시지 수신
- 메모 CRUD: `POST /memos` {content}, `GET /memos`, `GET /memos/{id}` (인메모리 저장소로 리액티브 흐름 연습)
- 외부 API 호출: `GET /external/joke` — Chuck Norris 조크 API를 WebClient로 호출하며 오류 시 기본 응답으로 폴백
- 백프레셔 체험: `GET /api/backpressure/limit-rate`, `/drop-hot`, `/buffered` (SSE로 요청/드랍/배치 상황 관찰)
- 음성 파이프라인 v1: `POST /voice/sse` {text} → 가짜 LLM + TTS를 거쳐 "오디오 청크" 문자열을 SSE로 스트리밍
- 음성 파이프라인 v2: `POST /voice/v2/sse` {text, requestedAt} → LLM 스트리밍 → 문장 조립 → TTS 스트리밍 → Base64 청크 SSE

## 빠른 호출 예시
```bash
# 시계 SSE
curl -N http://localhost:8080/time/stream

# 채팅 발행 후 스트림 구독
curl -X POST http://localhost:8080/chat/messages \
  -H "Content-Type: application/json" \
  -d '{"user":"dev","message":"hello webflux"}'
curl -N http://localhost:8080/chat/stream

# 백프레셔 limitRate 체험
curl -N http://localhost:8080/api/backpressure/limit-rate

# 음성 파이프라인 v2
curl -N -X POST http://localhost:8080/voice/v2/sse \
  -H "Content-Type: application/json" \
  -d '{"text":"안녕하세요, WebFlux 스트리밍 연습 중입니다.","requestedAt":"2025-02-06T09:00:00Z"}'
```

## 추가 참고
- `com.study.webflux.example.publisher.*` : Mono/Flux 기본 동작과 flatMap 예제
- `com.study.webflux.trace.TraceIdFilter` : 요청 헤더의 `X-Trace-Id`를 Reactor Context로 전달하는 필터
- `docs/` : 별도 학습 노트와 API 실험 기록
