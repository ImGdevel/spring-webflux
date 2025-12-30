# Supertone Text-to-Speech Guide

## 개요
- `voice_id`와 텍스트를 입력하면 지정한 언어/스타일/모델에 맞는 음성을 생성한다.
- 본 문서는 REST API 호출 구조, 필수 파라미터, 고급 옵션, 응답 처리, 부가 기능을 정리한다.

## 1. Endpoint

```
POST /v1/text-to-speech/{voice_id}
```

- Path Parameter  
  - `voice_id`: 사용할 음성의 고유 ID
- Query Parameter  
  - `output_format`(선택): `wav`(기본) 또는 `mp3`
- Headers  
  - `x-sup-api-key: <YOUR_API_KEY>`  
  - `Content-Type: application/json`

## 2. Request Body 필드
| 필드 | 필수 | 설명 |
| --- | --- | --- |
| `text` | ✅ | 합성할 문장, 최대 300자 |
| `language` | ✅ | 텍스트 언어(`ko`, `en`, `ja`) |
| `style` | 선택 | 감정/톤(`neutral`, `happy` 등). 미지정 시 기본값 사용 |
| `model` | 선택 | 현재는 `sona_speech_1` 지원. 생략 시 자동 적용 |
| `voice_settings` | 선택 | 피치·속도 등 세부 조정 |

```http
POST /v1/text-to-speech/91992bbd4758bdcf9c9b01?output_format=mp3
x-sup-api-key: <YOUR_API_KEY>
Content-Type: application/json

{
  "text": "안녕하세요, 수퍼톤 API입니다.",
  "language": "ko",
  "style": "neutral",
  "model": "sona_speech_1",
  "voice_settings": {
    "pitch_shift": 0,
    "pitch_variance": 1,
    "speed": 1
  }
}
```

## 3. `voice_settings` 옵션
| 파라미터 | 범위 | 기본 | 설명 |
| --- | --- | --- | --- |
| `pitch_shift` | -12 ~ 12 | 0 | 반음 단위 피치 이동(0은 원본) |
| `pitch_variance` | 0.1 ~ 2 | 1 | 운율 변화 폭 |
| `speed` | 0.5 ~ 2 | 1 | 전체 발화 속도 |

## 4. 응답 처리
- 성공 시 `audio/wav` 또는 `audio/mpeg` 스트림을 반환한다.  
- 헤더 `X-Audio-Length`에서 재생 길이를 초 단위로 확인 가능.

```
X-Audio-Length: 3.42
```

## 5. 텍스트 입력 유의 사항
- 300자 초과 시 400 오류가 발생한다.
- 한국어, 영어, 일본어만 공식 지원.
- 문장이 너무 짧으면 어색할 수 있으며, 이모지/특수문자는 무시될 수 있다.

## 6. 모델
- 현재 `sona_speech_1`만 제공된다. 모델 필드를 생략하면 자동 선택된다.

## 7. Predict Duration API
- 실제 합성 없이 예상 발화 시간을 알 수 있다. 과금되지 않는다.

```
POST /v1/predict-duration/{voice_id}
```

```json
{
  "duration": 2.87
}
```

## 8. Streaming TTS
- 실시간 응답이 필요한 경우 [Stream Text-to-Speech](./stream-text-to-speech.md)를 사용해 NDJSON 또는 바이너리 스트림으로 오디오를 수신한다.

## 9. 오류 처리
- 빈 바디, 잘못된 API Key, 한도 초과 등 오류 유형은 [Error Handling Guide](./error-handling.md)를 따르자.
