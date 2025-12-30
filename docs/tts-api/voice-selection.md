# Supertone Voice Selection Guide

## 개요
- 음성 합성 결과를 좌우하는 핵심은 `voice_id`이다.  
- `voice_id`와 함께 언어(`language`), 스타일(`style`), 모델(`model`)을 올바르게 지정해야 정확한 음성이 생성된다.  
- 아래 방법을 통해 원하는 캐릭터를 찾고, API 호출에 필요한 정보를 확인할 수 있다.

## Voice ID 확인 방법

### 1. Supertone Play에서 복사
1. [Supertone Play](https://play.supertone.ai)에서 원하는 음성을 선택한다.  
2. 음성 카드의 **Copy voice ID** 버튼을 클릭하면 ID가 클립보드에 복사된다.  
3. 클론한 음성은 별도 탭에서 관리 가능하다.

### 2. `GET /v1/voices` 로 전체 목록 조회
- 조건 없이 모든 프리셋/클론 음성을 받고 싶을 때 사용한다.
- 요청

```
GET /v1/voices
```

- 응답 주요 필드
  - `voice_id`: API 호출에 사용할 고유 ID
  - `name`, `description`, `age`, `gender`, `use_case`: 캐릭터 메타 정보
  - `language`, `styles`, `models`: 지원 언어/감정/모델 목록
  - `samples`: 언어/스타일/모델 조합별 샘플 오디오 URL
  - `thumbnail_image_url`: 대표 이미지

```json
{
  "voice_id": "91992bbd4758bdcf9c9b01",
  "name": "Adam",
  "language": ["ko", "en", "ja"],
  "styles": ["neutral"],
  "models": ["sona_speech_1"],
  "samples": [
    {
      "language": "ko",
      "style": "neutral",
      "model": "sona_speech_1",
      "url": "https://.../speech.wav"
    }
  ]
}
```

### 3. `GET /v1/voices/search` 로 조건 검색
- 언어, 스타일, 이름 등으로 필터링하여 필요한 음성만 찾는다.

```
GET /v1/voices/search?language=ko&style=happy
```

- 지원 필터: `language`, `style`, `name`, `description`, `gender`, `age`, `use_case` 등  
- 정렬 기능은 없으므로 조건을 구체적으로 지정한다.

## 샘플 오디오와 스타일 호환성
- 각 음성의 `samples` 배열은 언어/스타일/모델 조합별 샘플 오디오 URL을 제공한다.
- 샘플 URL에 직접 접근하면 실제 음성을 다운로드할 수 있다.
- 캐릭터마다 지원하는 스타일/언어/모델 조합이 다르므로, 사용 전 호환성을 반드시 확인한다.

```json
{
  "language": "ko",
  "style": "neutral",
  "model": "sona_speech_1",
  "url": "https://.../speech.wav"
}
```

## 다음 단계
- 원하는 `voice_id`를 확보했다면 [Text-to-Speech Guide](./text-to-speech.md)를 참고해 텍스트, 파라미터 설정, 오디오 포맷을 지정한다.
