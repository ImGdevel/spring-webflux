# Reactor 컨텍스트 실습: 예제 중심 정리

> 이 문서는 `reactor-context.md` 이론을 실제 코드로 연습하기 위한 실습 가이드이다.  
> 목표는 **ThreadLocal 없이도 리액티브 체인 전반에 데이터를 전달하는 패턴**을 몸에 익히는 것이다.

---

## 1. 공통 준비 아이디어

- 의존성
  - Spring WebFlux 또는 Reactor Core가 이미 포함되어 있다고 가정한다.
- 공통 유틸 (선택)
  - 현재 값을 로그로 찍으면서 **컨텍스트 값과 일반 데이터가 어떻게 섞여 흐르는지**를 관찰할 수 있도록 간단한 로깅 헬퍼를 만든다.

---

## 2. 예제 1 – 가장 기본적인 컨텍스트 저장·조회

**목표**: `contextWrite` 로 값을 넣고 `deferContextual` 로 꺼내 쓰는 가장 단순한 패턴을 익힌다.

- 시나리오
  - `Mono.just("hello")` 를 시작으로,
  - `contextWrite` 에서 `"key" → "value"` 를 컨텍스트에 저장.
  - `flatMap` 안에서 `Mono.deferContextual` 로 `"key"` 값을 읽어서 메시지를 조합.

- 개념 코드 흐름
  - `Mono.just("hello")`
  - `.flatMap(msg -> Mono.deferContextual(ctx -> Mono.just(msg + " / " + ctx.get("key"))))`
  - `.contextWrite(Context.of("key", "value"))`
  - `subscribe(System.out::println);`

- 관찰 포인트
  - 일반 데이터(`"hello"`)와 컨텍스트 값(`"value"`)을 **함께 사용**할 수 있음을 확인한다.
  - 컨텍스트는 **별도 파라미터 없이**도 `deferContextual` 로 접근 가능하다는 점을 인식한다.

---

## 3. 예제 2 – `put`으로 컨텍스트에 값 추가·변경

**목표**: 기존 컨텍스트를 유지한 채 값을 추가/변경하는 패턴을 연습한다.

- 시나리오
  - 처음에는 `Context.of("traceId", "T-1")` 로 traceId 를 넣는다.
  - 중간에 `contextWrite(ctx -> ctx.put("userId", "U-100"))` 로 userId 를 추가.
  - 이후 연산에서 두 값을 동시에 읽어 로깅 문자열을 만든다.

- 개념 코드 흐름
  - `Mono.just("request")`
  - `.flatMap(msg -> Mono.deferContextual(ctx -> {`
  - &nbsp;&nbsp;`String traceId = ctx.getOrDefault("traceId", "no-trace");`
  - &nbsp;&nbsp;`String userId = ctx.getOrDefault("userId", "anonymous");`
  - &nbsp;&nbsp;`return Mono.just(msg + " / trace=" + traceId + " / user=" + userId);`
  - `}))`
  - `.contextWrite(ctx -> ctx.put("userId", "U-100"))`
  - `.contextWrite(Context.of("traceId", "T-1"))`

- 관찰 포인트
  - `Context.of` 로 만든 초기 컨텍스트 위에 `put` 으로 값을 **추가**하는 흐름을 이해한다.
  - traceId·userId 두 값이 동시에 보이는지 확인한다.

---

## 4. 예제 3 – 컨텍스트 전파 방향과 덮어쓰기 체험

**목표**: 컨텍스트가 **아래 → 위 방향으로 전파**되고, **가장 아래쪽 `contextWrite` 가 우선**한다는 점을 직접 확인한다.

- 시나리오 A – 같은 키를 여러 번 설정
  - `contextWrite(Context.of("key", "first"))`
  - 그 아래에 `contextWrite(Context.of("key", "second"))` 를 다시 선언.
  - `deferContextual` 로 `"key"` 값을 읽어본다.

- 기대 결과
  - 최종적으로 `"second"` 가 읽힌다.
  - 이유: 컨텍스트는 아래에서 위로 전파되므로, **가장 아래쪽** 설정이 마지막에 적용된다.

- 시나리오 B – 순서 바꿔보기
  - 위·아래 `contextWrite` 의 순서를 바꾸고 결과가 어떻게 달라지는지 확인한다.

- 관찰 포인트
  - `contextWrite` 의 위치와 순서가 **최종 컨텍스트 값에 매우 중요**하다는 사실을 체감한다.

---

## 5. 예제 4 – WebFlux 핸들러 + 컨텍스트 (개념)

**목표**: 실제 WebFlux 요청 처리 흐름에서 컨텍스트를 어떻게 사용할 수 있는지 시나리오를 잡아본다.

- 시나리오
  1. WebFilter 또는 HandlerFilterFunction 에서
     - HTTP 헤더의 `X-Trace-Id` 값을 읽는다.
     - 없으면 랜덤으로 생성한다.
     - 이 값을 컨텍스트에 `traceId` 키로 저장 (`contextWrite`) 한다.
  2. 이후 핸들러나 서비스 레이어에서
     - `Mono.deferContextual` 로 `traceId` 를 읽어 로그 메시지에 추가한다.

- 개념 흐름
  - 필터에서:  
    `return chain.filter(exchange).contextWrite(ctx -> ctx.put("traceId", traceId));`
  - 핸들러에서:  
    `Mono.deferContextual(ctx -> Mono.just("trace=" + ctx.getOrDefault("traceId", "no-trace")));`

- 관찰 포인트
  - 필터(바깥 레이어)에서 넣은 컨텍스트가 핸들러(안쪽 레이어)까지 잘 전파되는지 확인한다.
  - 스레드가 바뀌어도 동일한 traceId 를 공유하는지 로그로 확인해 본다.

---

## 6. 예제 5 – 컨텍스트 + 스케줄러 조합

**목표**: 스레드가 바뀌어도 컨텍스트가 유지된다는 점을, 스케줄러와 함께 사용하는 실험으로 확인한다.

- 시나리오
  - `Flux.range(1, 3)` 에서 각 값 처리 전에 `publishOn(Schedulers.boundedElastic())` 를 사용해 스레드를 변경.
  - 그 이후 단계에서 `deferContextual` 로 컨텍스트 값을 읽어본다.
  - 최초에는 `contextWrite(Context.of("requestId", "R-1"))` 로 requestId 를 넣어 둔다.

- 기대 포인트
  - `boundedElastic` 등 다른 스레드에서 실행되더라도 `requestId` 가 그대로 읽힌다.
  - `ThreadLocal` 과 달리 **스레드 전환과 상관없이 데이터가 유지**되는 것을 체험한다.

---

## 7. 예제 6 – 테스트 코드에서 컨텍스트 검증하기

**목표**: 간단한 단위 테스트로 컨텍스트 동작을 검증하는 패턴을 익힌다.

- 아이디어
  - StepVerifier 등을 사용해
    - 특정 컨텍스트 값이 반영된 문자열이 방출되는지 검증.
  - 예:
    - `StepVerifier.create(pipeline.contextWrite(Context.of("userId", "U-1")))`
    - `.expectNextMatches(msg -> msg.contains("U-1"))`
    - `.verifyComplete();`

- 관찰 포인트
  - 컨텍스트 값이 **테스트 검증 조건의 일부**가 될 수 있다는 점을 이해한다.
  - 복잡한 WebFlux 컨텍스트 로직도 단위 테스트로 충분히 검증 가능하다.

---

## 8. 마무리 정리

- 이 문서의 예제들을 통해 다음을 직접 확인하는 것이 목표다.
  - `contextWrite` 로 값 추가/변경,
  - `deferContextual` 로 값 조회,
  - 여러 `contextWrite` 체인 시 **마지막(아래쪽) 설정 우선** 규칙,
  - 스레드가 바뀌어도 컨텍스트가 유지되는지.
- 실제 프로젝트에서는
  - 인증 정보, traceId, locale, 요청 메타데이터 등을 컨텍스트에 담아
  - 필터 → 서비스 → 리포지토리까지 **동일한 컨텍스트를 공유**하는 패턴으로 확장할 수 있다.

