# WebFlux 학습 로드맵 (docs/study 기준)

## 0. 전체 학습 순서 한눈에 보기

1. 리액티브 기본 개념  
   - `docs/study/reactive-streams.md:1`
2. Reactor 핵심 개념 연습  
   - (필요시: IDE에서 Reactor 연산자 체인 간단히 실험)
3. 스케줄러와 스레드 모델  
   - `docs/study/reactor-schedulers.md:1`  
   - `docs/study/reactor-schedulers-practice.md:1`
4. Reactor Context 이해 및 실습  
   - `docs/study/reactor-context.md:1`  
   - `docs/study/reactor-context-practice.md:1`
5. Reactor 에러 처리 전략  
   - `docs/study/reactor-error-handling.md:1`
6. WebFlux 개념/전체 그림  
   - `docs/study/webflux-overview.md:1`  
   - `docs/study/webflux-real-overview.md:1`
7. WebFlux 기본 요청/응답 흐름  
   - `docs/study/webflux-basic.md:1`  
   - `docs/study/webflux-request-response.md:1`
8. WebFlux 엔드포인트 설계  
   - `docs/study/webflux-endpoints.md:1`
9. 실전 적용 & 연습  
   - `docs/study/webflux-practice.md:1`  
   - (관련 테스트 코드: `src/test/java/com/study/webflux/voice/TtsFlowTimingTest.java:1` 등)

---

## 1. 리액티브 기본 개념

- 먼저 `reactive-streams.md` 를 정독하면서 **Publisher / Subscriber / Subscription / Processor** 개념과 **백프레셔** 흐름을 잡는다.
- 이 단계의 목표:
  - “왜 WebFlux가 필요한지”,
  - “리액티브 스트림이 기존 동기 방식과 무엇이 다른지” 를 명확히 이해하는 것.

## 2. Reactor 핵심 감각 익히기 (간단 실험)

- 별도 문서는 없지만, `reactive-streams.md` 에서 본 개념을 토대로 IDE에서 아래 정도를 직접 만들어 본다.
  - `Flux.just`, `Mono.just`, `map`, `flatMap`, `filter` 등의 간단한 체인.
- 이 단계의 목표:
  - “데이터가 위에서 아래로 어떻게 흘러가는지”,
  - “subscribe 시점에 실제 실행이 시작된다”는 감각을 몸으로 익히는 것.

## 3. 스케줄러와 스레드 모델

- `reactor-schedulers.md` 로 **스레드 모델 / 스케줄러 종류** (`immediate`, `single`, `boundedElastic`, `parallel`) 를 이해한다.
- 이어서 `reactor-schedulers-practice.md` 로 스케줄러 변경이 실제 로그/스레드 이름에 어떻게 드러나는지 연습한다.
- 이 단계의 목표:
  - “어느 시점에서 어떤 스레드로 코드가 실행되는지” 를 눈으로 확인하고,
  - WebFlux 컨트롤러/서비스 안에서 블로킹 코드가 왜 위험한지 직관을 얻는 것.

## 4. Reactor Context 이해 및 실습

- `reactor-context.md` 로 **컨텍스트의 개념**(ThreadLocal 과의 차이, 비동기 체인에서의 전파 방식)을 이해한다.
- `reactor-context-practice.md` 로 실제로 `contextWrite`, `deferContextual` 등을 사용해 컨텍스트를 읽고/쓰는 연습을 한다.
- 이 단계의 목표:
  - 요청 단위 트래킹 ID, 사용자 정보 등 **부가 데이터**를 어떻게 스트림 전체에 전파할지 감을 잡는 것.
  - 이후 WebFlux의 필터/전역 로깅 설계에 바로 연결된다.

## 5. Reactor 에러 처리 전략

- `reactor-error-handling.md` 를 통해 기본 에러 흐름과 다음 연산자들을 정리한다.
  - `onErrorReturn`, `onErrorResume`, `onErrorContinue`, `onErrorMap`, `onErrorStop`, `retry` 등.
- 읽으면서, 기존의 `try-catch` 와 어떻게 대응되는지 머릿속에 매핑해 본다.
- 이 단계의 목표:
  - “여기서 에러 나면 기본값으로 대체”, “이 타입의 에러는 다른 예외로 감싸기” 와 같은 **정책을 선언적으로 표현**하는 연습을 하는 것.
  - 이후 WebFlux 핸들러/필터에서 에러 전략을 설계할 때 토대가 된다.

## 6. WebFlux 개념/전체 그림

- `webflux-overview.md` 를 먼저 읽어 WebFlux의 큰 그림(논블로킹, Netty, Servlet 3.1+, 리액티브 스택)을 정리한다.
- `webflux-real-overview.md` 로 실제 애플리케이션 구조 관점에서 “어디에 무엇을 배치할지 (핸들러, 라우터, 서비스, 리포지토리 등)” 를 다시 한 번 그려본다.
- 이 단계의 목표:
  - Reactor 에서 배운 개념들이 WebFlux 프레임워크 레벨에서는 **어디에 녹아 있는지** 연결하는 것.

## 7. WebFlux 기본 요청/응답 흐름

- `webflux-basic.md` 로 간단한 예제(기본 핸들러/라우팅) 중심으로 **요청 → 핸들러 → 응답** 흐름을 잡는다.
- `webflux-request-response.md` 를 통해
  - `ServerRequest`, `ServerResponse`,
  - `Mono`, `Flux` 기반의 반환 타입이 실제 HTTP 응답으로 어떻게 매핑되는지 이해한다.
- 이 단계의 목표:
  - “전통 MVC 컨트롤러와 무엇이 다른지”,
  - “비동기/리액티브가 HTTP 핸들링에 어떻게 적용되는지” 를 코드 레벨에서 이해하는 것.

## 8. WebFlux 엔드포인트 설계

- `webflux-endpoints.md` 에서 라우터 함수 스타일, 엔드포인트 설계 패턴 등을 본다.
- 이 단계의 목표:
  - 실무에서 자주 쓰는 **라우트 구성 패턴**, **핸들러 분리 방식** 등을 정리해두고,
  - 나중에 실제 프로젝트에서 구조를 선택할 때 참고할 수 있게 하는 것.

## 9. 실전 적용 & 연습

- `webflux-practice.md` 를 마지막으로 보면서,
  - 앞에서 배운 리액티브 개념, 스케줄러, 컨텍스트, 에러 처리, WebFlux 엔드포인트를 한 번에 묶어보는 연습을 한다.
- 필요하다면 테스트 코드(예: `src/test/java/com/study/webflux/voice/TtsFlowTimingTest.java:1`) 를 함께 읽어
  - “시간 흐름 / 비동기 처리” 를 테스트로 어떻게 검증하는지 확인한다.
- 이 단계의 목표:
  - 지금까지의 내용을 실제 코드 레벨에서 **끊김 없이 이어서 쓸 수 있는지** 점검하고,
  - 부족한 부분(예: 에러 처리, 컨텍스트, 스케줄러)을 다시 위 단계 문서로 돌아가 보충하는 것.

