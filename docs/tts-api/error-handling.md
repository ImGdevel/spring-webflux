# Error Handling Guide

## 개요
- Supertone API는 HTTP 상태 코드로 문제 원인을 전달한다.
- 아래 표는 주로 발생하는 오류, 원인, 해결 방법을 정리한 것이다.

## 1. 주요 HTTP 오류
| 상태 코드 | 의미 | 발생 원인 | 해결 방법 |
| --- | --- | --- | --- |
| 400 Bad Request | 잘못된 요청 | 필수 필드(`text`, `language`) 누락, 존재하지 않는 `style`/`model`, `voice_settings` 범위 초과, JSON 파싱 오류, 300자 초과 텍스트 | 요청 JSON과 값 범위를 재검토. 에디터/린터로 JSON 검사 |
| 401 Unauthorized | 인증 실패 | `x-sup-api-key` 누락, 잘못된 키 사용 | 콘솔에서 발급한 키를 정확히 입력하고 앞뒤 공백 제거 |
| 402 Not Enough Credits | 크레딧 부족 | 잔여 크레딧 0, 요금제 미구매 | [Play 구독 페이지](https://play.supertone.ai)에서 요금제 구매/충전 |
| 403 Forbidden | 권한 없음 | 타 계정의 `voice_id` 사용, 권한 없는 서브유저, 콘솔 외 키 사용 | `GET /v1/voices`로 소유 여부 확인. 클론 음성은 생성 계정만 호출 가능 |
| 404 Not Found | 리소스 없음 | 존재하지 않는 `voice_id`, `GET /voices/search` 조건 과도, 잘못된 엔드포인트 경로 | 경로/파라미터 오타 확인, 콘솔이나 `GET /v1/voices`로 ID 재검증 |
| 408 Request Timeout | 처리 지연 | 네트워크 불안정, 바디가 과도하게 복잡, 서버 지연 | 입력 단순화 후 재시도, 반복 시 고객지원에 문의 |
| 429 Too Many Requests | 레이트 리밋 초과 | 짧은 시간 과도한 요청 | `Rate Limits` 문서 참고, 잠시 대기 후 재시도, 엔터프라이즈 요금제는 상향 요청 가능 |
| 500 Internal Server Error | 서버 내부 오류 | 일시적인 서버 문제 | 동일 요청 반복 실패 시 시간/바디/헤더 정보를 포함해 고객지원에 문의 |

## 2. 흔한 사례
- 요청 바디 없이 호출하여 400 발생
- Postman 환경 변수에 API Key를 잘못 세팅해 401 발생
- 다른 계정이 만든 클론 음성을 호출해 403 발생
- 크레딧 소진 후 추가 호출로 402 발생
- 300자를 넘는 텍스트로 400 발생

## 3. 대응 팁
- 오류 메시지는 간단하므로 **HTTP 코드 + 요청 내용을 직접 확인**하는 것이 가장 빠르다.
- 동일 오류가 반복되면 요청 본문/헤더와 발생 시각을 첨부해 고객지원에 전달한다.
- 호출 전 신용 잔액과 레이트 리밋을 확인하면 장애를 예방할 수 있다.
- 레이트 리밋은 [Rate Limits 문서](https://docs.supertoneapi.com/en/user-guide/rate-limits)를 참고한다.

## 4. 참고
- 음성 선택: [Voice Selection Guide](./voice-selection.md)
- 기본 TTS: [Text-to-Speech Guide](./text-to-speech.md)
- 스트리밍 TTS: [Stream Text-to-Speech Reference](./stream-text-to-speech.md)
