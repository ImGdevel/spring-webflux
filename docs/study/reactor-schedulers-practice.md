# Reactor 스케줄러 실습: 예제 중심 정리

> 이 문서는 `reactor-schedulers.md` 이론 정리를 실제 코드 예제로 연습하기 위한 실습 가이드이다.  
> 주요 목표는 **어떤 작업을 어떤 스케줄러에 배치해야 하는지**, 그리고 `subscribeOn` / `publishOn` 이 **실제로 어떻게 스레드를 바꾸는지**를 체감하는 것이다.

---

## 1. 공통 준비 사항

- 의존성
  - Spring WebFlux 또는 Reactor Core가 이미 포함되어 있다고 가정한다.
- 공통 유틸
  - 현재 스레드 이름을 찍는 간단한 헬퍼를 하나 만들어 두면 좋다.
  - 예시 아이디어 (실제 구현은 프로젝트 코드 스타일에 맞게 작성):
    - `log("stage")` → `[스레드명] stage: 값` 형태로 출력.

---

## 2. 예제 1 – 스케줄러 없이 기본 흐름 관찰

**목표**: 별도의 스케줄러를 지정하지 않았을 때, Reactor 스트림이 어떤 스레드에서 실행되는지 확인한다.

- 시나리오
  - `Flux.range(1, 5)` 로 숫자를 생성.
  - `map` 단계에서 현재 스레드명을 출력.
  - `subscribe` 에서도 현재 스레드명을 출력.
- 기대 포인트
  - 테스트를 **JUnit 테스트**로 실행하면 JUnit 관련 스레드에서 실행된다.
  - **Spring WebFlux 컨트롤러**에서 실행하면 Netty 이벤트 루프 스레드(예: `reactor-http-nio-*`) 에서 실행되는 것을 볼 수 있다.

---

## 3. 예제 2 – I/O-bound 작업 + `boundedElastic`

**목표**: I/O 작업을 `boundedElastic` 스케줄러에 보내는 패턴을 연습한다.

- 시나리오
  - `Flux.range(1, 5)` 로 요청 ID를 만든다고 가정.
  - `flatMap` 안에서 I/O 작업을 흉내 내기 위해 `Thread.sleep(100)` 을 사용.
    - 실제로는 외부 API 호출, DB 쿼리 등을 배치.
  - `.subscribeOn(Schedulers.boundedElastic())` 를 체인에 추가.
  - 각 단계에서 스레드명을 출력.
- 관찰 포인트
  - `range` 생성, `flatMap` 내부, `subscribe` 모두 `boundedElastic` 스레드 풀에서 실행되는지 확인.
  - 메인 스레드(테스트 스레드)와 다른 스레드 이름으로 나오는지 확인.
- 참고
  - 예제에서 `Thread.sleep` 은 단순 I/O 대기 시뮬레이션용이다.
  - 실제 WebFlux 코드에서는 `Thread.sleep` 대신 **논블로킹 클라이언트(WebClient, R2DBC 등)** 를 사용해야 한다.

---

## 4. 예제 3 – CPU-bound 작업 + `parallel`

**목표**: CPU 중심 연산을 `parallel` 스케줄러에서 병렬 처리하는 패턴을 연습한다.

- 시나리오
  - `Flux.range(1, 10)` 을 생성.
  - 각 숫자에 대해 CPU를 좀 사용하는 연산을 흉내 내기 위해 작은 반복문/계산을 수행.
  - `.publishOn(Schedulers.parallel())` 를 사용해 연산 구간을 `parallel` 스케줄러로 전환.
  - 연산 전·후, 구독 단계에서 스레드명을 출력.
- 관찰 포인트
  - 여러 개의 `parallel-*` 스레드에서 연산이 동시에 수행되는지 확인.
  - 스레드 이름이 CPU 코어 수만큼 다양하게 나오는지 확인.
- 확장 실습
  - 같은 연산을 `boundedElastic` 과 `parallel` 로 각각 실행해보고, 로그/성능 차이를 비교해 본다.

---

## 5. 예제 4 – `subscribeOn` vs `publishOn` 차이 체감

**목표**: `subscribeOn` 이 업스트림 전체에, `publishOn` 이 다운스트림에만 영향을 준다는 점을 코드로 확인한다.

- 기본 파이프라인 예시 구조
  - `Flux.range(1, 5)`
  - `.map(… 1단계 …)`  // stage 1
  - `.publishOn(Schedulers.parallel())`
  - `.map(… 2단계 …)`  // stage 2
  - `.subscribe(...)`

### 5.1 케이스 A – `subscribeOn`만 사용

- 체인 예시
  - `Flux.range(1, 5).map(stage1).map(stage2).subscribeOn(Schedulers.boundedElastic()).subscribe(...)`
- 관찰 포인트
  - `stage1`, `stage2`, `subscribe` 모두 `boundedElastic` 스레드에서 실행되는지 확인.
  - **연산자 위치와 상관없이 소스 쪽(업스트림) 전체에 영향을 준다**는 점을 기억.

### 5.2 케이스 B – `publishOn`만 사용

- 체인 예시
  - `Flux.range(1, 5).map(stage1).publishOn(Schedulers.parallel()).map(stage2).subscribe(...)`
- 관찰 포인트
  - `stage1` 은 이전 스레드(예: 테스트 스레드, Netty 이벤트 루프)에서 실행.
  - `publishOn` 이후인 `stage2`, `subscribe` 는 `parallel` 스레드에서 실행.
  - **publishOn의 위치에 따라 어느 지점부터 스레드가 바뀌는지** 확인.

### 5.3 케이스 C – `subscribeOn` + `publishOn` 조합

- 체인 예시
  - `Flux.range(1, 5)`
  - `.map(stage1)`                        // boundedElastic
  - `.publishOn(Schedulers.parallel())`   // 여기서 스레드 전환
  - `.map(stage2)`                        // parallel
  - `.subscribeOn(Schedulers.boundedElastic())`
  - `.subscribe(...)`
- 관찰 포인트
  - 소스와 `stage1` 은 `boundedElastic` 에서 실행.
  - `publishOn` 이후인 `stage2`, `subscribe` 는 `parallel` 에서 실행.
  - `subscribeOn` 은 업스트림, `publishOn` 은 다운스트림에 영향을 준다는 개념을 확실히 정리.

---

## 6. 예제 5 – 테스트 코드에서의 주의점

**목표**: 비동기 스트림 테스트에서 “메인 스레드가 먼저 끝나지 않도록” 주의하는 패턴을 익힌다.

- 문제 상황
  - `main` 메서드나 단순 테스트 메서드에서 비동기 스트림을 실행하면,
  - 스트림이 끝나기도 전에 메서드가 끝나서 프로세스/테스트가 종료될 수 있다.

- 단순 해결책 (연습용)
  - 마지막에 `Thread.sleep(1000)` 등으로 잠깐 대기.
  - 또는 `CountDownLatch` 를 사용해 스트림 완료 시 latch를 내려주고, 테스트에서는 `await()` 으로 기다린다.

- 실제 서비스 코드에서는
  - WebFlux 서버(Netty)가 이벤트 루프를 유지하므로, 일반적으로 따로 sleep을 둘 필요는 없다.
  - 다만 **단위 테스트**에서는 위와 같은 대기/동기화 패턴을 잘 활용해야 한다.

---

## 7. 마무리 정리

- 스케줄러 실습을 통해 다음을 직접 확인하는 것이 목표다.
  - I/O-bound 작업 ↔ `boundedElastic`
  - CPU-bound 작업 ↔ `parallel`
  - `subscribeOn` 은 **소스·업스트림 전체**에 영향을 미치고,
  - `publishOn` 은 **해당 위치 이후 다운스트림**만 스레드를 전환한다.
- 실제 프로젝트에서 스레드 이름 로그를 자주 찍어 보면서,
  - “어떤 코드가 어느 스레드에서 실행되는지” 를 체감하면
  - WebFlux/Reactive 애플리케이션의 동작 방식이 훨씬 직관적으로 느껴질 것이다.

