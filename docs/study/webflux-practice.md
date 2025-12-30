# Spring WebFlux 실습: 라우터·핸들러 예제 정리

> 이 문서는 `webflux-basic.md`, `webflux-overview.md`, `webflux-real-overview.md` 에서 정리한 이론을  
> 실제 코드 예제로 연습하기 위한 가이드이다.  
> 목표는 **가장 단순한 WebFlux API**부터 **함수형 라우팅**과 **스트리밍 엔드포인트**까지 직접 만들어 보는 것이다.

---

## 1. 공통 준비 사항

- 프로젝트
  - Spring WebFlux 의존성이 포함된 Spring Boot 프로젝트라고 가정한다.
  - 기본 엔트리 포인트: `WebfluxApplication` (이미 존재).
- 패키지 예시
  - `com.study.webflux.handler`
  - `com.study.webflux.router`
  - `com.study.webflux.controller`

아래 예제들은 패키지 구조와 클래스 이름만 참고용이며, 실제 프로젝트 스타일에 맞게 조정하면 된다.

---

## 2. 예제 1 – 가장 단순한 @RestController + Mono/Flux

**목표**: 기존 MVC와 비슷한 느낌으로 진입하되, 리턴 타입을 `Mono`/`Flux` 로 바꿔 보는 것부터 시작한다.

- 기능
  - `GET /hello` → 문자열 하나(`Mono<String>`)
  - `GET /numbers` → 숫자 여러 개(`Flux<Integer>`)

- 핵심 포인트
  - `@RestController` + `@GetMapping` 은 MVC와 동일하다.
  - 차이는 **반환 타입**이 `String`/`List<T>` 가 아니라 `Mono<T>`/`Flux<T>` 라는 점.
  - 내부에서 별도의 스레드 작업을 하지 않아도, WebFlux는 이미 논블로킹 Netty 위에서 동작한다.

---

## 3. 예제 2 – 함수형 라우터(RouterFunction) + HandlerFunction

**목표**: WebFlux 고유의 함수형 라우팅 스타일을 연습한다.

- 구성
  1. `GreetingHandler` – 실제 비즈니스 로직 처리
  2. `GreetingRouter` – HTTP 메서드/경로에 따라 어떤 핸들러를 호출할지 정의

- 기능 예시
  - `GET /functional/hello` → `"Hello Functional WebFlux!"`
  - `GET /functional/user/{name}` → `"Hello, {name}!"`

- 핵심 포인트
  - 라우팅 정의는 `RouterFunction<ServerResponse>` 를 반환하는 `@Bean` 메서드에서 한다.
  - `RouterFunctions.route()` + `RequestPredicates.GET("...")` 방식으로 구성.
  - 핸들러는 `ServerRequest` → `Mono<ServerResponse>` 형태의 함수를 구현한다.

---

## 4. 예제 3 – 스트리밍 엔드포인트 (Flux + interval)

**목표**: WebFlux의 “스트리밍” 감각을 느껴보기 위해, 일정 주기로 숫자를 계속 보내는 엔드포인트를 만들어 본다.

- 기능
  - `GET /stream/numbers` → 1초마다 증가하는 숫자를 계속 내보내는 SSE(Server-Sent Events) 스타일 스트림.

- 개념 흐름
  - `Flux.interval(Duration.ofSeconds(1))` 로 0,1,2,... 를 생성.
  - 필요하면 `.map` 으로 메시지를 문자열로 변환.
  - `MediaType.TEXT_EVENT_STREAM` 으로 응답하면 브라우저에서 스트림으로 볼 수 있다.

- 관찰 포인트
  - 브라우저/HTTP 클라이언트에서 응답이 한 번에 끝나지 않고 **계속 이어지는 형태**를 확인한다.
  - WebFlux가 이처럼 **연속 데이터 스트림**을 다루기에 자연스럽다는 점을 체감한다.

---

## 5. 예제 4 – WebClient를 이용한 비동기 호출

**목표**: WebFlux 서버에서 또 다른 HTTP API를 비동기적으로 호출하는 패턴을 연습한다.

- 기능
  - `GET /client/hello` → 내부적으로 다른 서비스의 `/hello` 엔드포인트를 WebClient로 호출 후 응답 반환.

- 핵심 포인트
  - `WebClient` 를 사용하면 **블로킹 없이** 외부 HTTP 호출을 할 수 있다.
  - 반환 타입 역시 `Mono`/`Flux` 이므로, 전체 파이프라인이 리액티브하게 이어진다.
  - MVC에서도 WebClient를 쓸 수 있지만, WebFlux와 함께 사용하면 스택 전체가 리액티브가 된다.

---

## 6. 예제 5 – 간단한 서비스 레이어 분리 + Reactor 연산자

**목표**: 컨트롤러/핸들러에서 서비스 레이어를 분리하고, `map`, `flatMap` 같은 Reactor 연산자를 함께 써 보는 연습을 한다.

- 시나리오
  - `HelloService`:
    - `Mono<String> getGreeting(String name)` – 비동기적으로 인사 메시지 생성.
  - 컨트롤러/핸들러:
    - `GET /hello/{name}` → `HelloService` 를 호출해 결과 반환.
  - 연산자:
    - `map` 으로 문자열을 가공하거나,
    - `flatMap` 으로 다른 `Mono`/`Flux` 를 이어 붙인다.

- 관찰 포인트
  - 기존 Service/Repository 계층 구조를 유지하면서도 반환 타입만 `Mono`/`Flux` 로 바뀌었을 때,
  - 전체 흐름이 어떻게 달라지는지 감각을 익힌다.

---

## 7. 테스트 및 실행 아이디어

- 실행
  - `WebfluxApplication` 을 실행한 뒤, 브라우저 또는 `curl`, Postman으로 엔드포인트 호출.
  - 스트리밍 엔드포인트는 브라우저 탭에서 직접 확인하면 이해가 쉽다.

- 간단한 검사
  - 각 엔드포인트에서 **스레드 이름**을 로그로 찍어 두고,
    - 요청을 여러 번 보내면서 어떤 스레드에서 처리되는지 관찰해 본다.
  - 이후 `Schedulers` 를 함께 사용하면서 스레드가 어떻게 바뀌는지도 연계해서 실습하면 좋다.

---

## 8. 마무리

- 이 문서의 목표는 “WebFlux가 뭔지 알겠다”에서 한 단계 더 나아가,
  - “**직접 간단한 WebFlux API를 만들고, 스트림처럼 동작하는 것을 확인해 봤다**” 수준까지 올라가는 것이다.
- 실제 프로젝트에서는
  - 인증/로깅/컨텍스트/스케줄러/에러 처리 등 앞에서 정리한 내용들을 조합해
  - 더 복잡한 흐름을 만들게 되므로,
  - 여기 예제들을 발판으로 점차 범위를 넓혀 가면 좋다.

