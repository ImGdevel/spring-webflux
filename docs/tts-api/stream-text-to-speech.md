# Stream Text-to-Speech Reference

## 개요
- `POST /v1/text-to-speech/{voice_id}/stream` 엔드포인트는 TTS 결과를 즉시 스트리밍한다.
- 비동기 응답이 필요한 챗봇/캐릭터 서비스에 적합하며, 필요 시 NDJSON 형식으로 음소 정보까지 함께 받을 수 있다.

## 1. Endpoint

```
POST https://supertoneapi.com/v1/text-to-speech/{voice_id}/stream
```

- Headers  
  - `x-sup-api-key: <api-key>`  
  - `Content-Type: application/json`
- Path Parameter  
  - `voice_id`: 사용할 음성 ID

## 2. Request Body
| 필드 | 필수 | 설명 |
| --- | --- | --- |
| `text` | ✅ | 300자 이하 텍스트 |
| `language` | ✅ | `en`, `ko`, `ja` |
| `style` | 선택 | 감정 스타일. 미지정 시 캐릭터 기본값 |
| `model` | 선택 | 기본 `sona_speech_1` |
| `output_format` | 선택 | `wav`, `mp3` (기본 `wav`) |
| `voice_settings` | 선택 | 피치/속도 등 고급 제어 |
| `include_phonemes` | 선택 | `true` 설정 시 오디오+음소 NDJSON 스트림 |

### cURL 예시

```bash
curl --request POST \
  --url https://supertoneapi.com/v1/text-to-speech/{voice_id}/stream \
  --header 'Content-Type: application/json' \
  --header 'x-sup-api-key: <api-key>' \
  --data '{
    "text": "Hello, Supertone streaming!",
    "language": "en",
    "style": "neutral",
    "model": "sona_speech_1",
    "output_format": "wav",
    "voice_settings": {
      "pitch_shift": 0,
      "pitch_variance": 1,
      "speed": 1,
      "duration": 0,
      "similarity": 3,
      "text_guidance": 1,
      "subharmonic_amplitude_control": 1
    },
    "include_phonemes": false
  }'
```

## 3. Voice Settings 세부값
| 항목 | 범위 | 기본 | 설명 |
| --- | --- | --- | --- |
| `pitch_shift` | -24 ~ 24 | 0 | 반음 단위 피치 조정 |
| `pitch_variance` | 0 ~ 2 | 1 | 피치 변화량 |
| `speed` | 0.5 ~ 2 | 1 | 전체 속도 배율 |
| `duration` | 0 ~ 60 | 0 | 결과 음성을 목표 길이에 맞게 늘이거나 줄임(초) |
| `similarity` | 1 ~ 5 | 3 | 캐릭터 음색과의 유사도 |
| `text_guidance` | 0 ~ 4 | 1 | 텍스트에 따른 발화 특성 반영 정도 |
| `subharmonic_amplitude_control` | 0 ~ 2 | 1 | 서브하모닉 크기 조절 |

## 4. Response
- `include_phonemes=false` (기본): `audio/wav` 또는 `audio/mpeg` 바이너리 스트림
- `include_phonemes=true`: NDJSON 스트림으로 전송되며 각 라인은 다음 구조를 가진다.

```json
{
  "audio_base64": "UklGRnoGAABXQVZF...",
  "phonemes": {
    "symbols": ["", "h", "ɐ", "ɡ", "ʌ", ""],
    "start_times_seconds": [0, 0.092, 0.197, 0.255, 0.29, 0.58],
    "durations_seconds": [0.092, 0.104, 0.058, 0.034, 0.29, 0.162]
  }
}
```

- `include_phonemes=true`일 때는 각 레코드를 줄 단위로 파싱해 오디오(Base64)와 음소 타이밍을 처리한다.

## 5. 주의 사항
- 텍스트는 300자를 넘길 수 없으며 초과 시 400 오류.
- `speed`는 `duration` 적용 이후에 반영된다. (예: `duration=5`, `speed=2` => 최종 약 10초)
- `style`을 생략하면 캐릭터마다 서로 다른 기본값이 적용된다. `GET /v1/voices`로 기본 스타일(배열 첫 번째 값)을 확인하자.
- 반환된 오디오 스트림은 즉시 재생하거나 파일로 저장할 수 있으며, 클라이언트 환경에 맞는 처리 로직이 필요하다.

## 6. 관련 문서
- 기본 TTS 사용법은 [Text-to-Speech Guide](./text-to-speech.md) 참고
- 오류 대응은 [Error Handling Guide](./error-handling.md) 참고
