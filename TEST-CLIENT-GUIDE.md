# RAG Dialogue Pipeline 테스트 클라이언트 가이드

## 시작하기

### 1. MongoDB & Redis 시작
```bash
docker-compose up -d
```

### 2. 환경변수 설정 (선택사항)
```bash
export OPENAI_API_KEY=your-openai-key
export SUPERTONE_API_KEY=your-supertone-key
```

### 3. 애플리케이션 실행
```bash
./gradlew :webflux-rag:bootRun
```

### 4. 브라우저에서 테스트 클라이언트 접속
```
http://localhost:8081/
```

## 테스트 클라이언트 사용법

### 화면 구성
1. **질문 입력창**: 테스트할 텍스트 쿼리 입력
2. **전송 버튼**: 파이프라인 실행
3. **상태 표시**: 현재 처리 상태
4. **오디오 청크**: 수신된 Base64 오디오 데이터

### 기능
- ✅ SSE(Server-Sent Events) 스트리밍 실시간 표시
- ✅ Base64 오디오 청크 개수 카운팅
- ✅ 응답 시간 측정
- ✅ 에러 핸들링 및 표시
- ✅ Ctrl/Cmd + Enter로 빠른 전송

### 테스트 시나리오

#### 1. 기본 테스트
```
질문: "WebFlux란 무엇인가요?"
예상: LLM 응답 → 문장 조립 → TTS 변환 → 오디오 청크 스트리밍
```

#### 2. RAG 컨텍스트 테스트
```
1차: "WebFlux에 대해 설명해주세요"
2차: "Reactor에 대해 설명해주세요"
3차: "WebFlux 관련 질문" (1차 대화가 RAG 컨텍스트로 활용됨)
```

#### 3. 긴 질문 테스트
```
질문: "Spring WebFlux의 장점과 단점을 자세히 설명하고,
      어떤 상황에서 사용하는 것이 적합한지 알려주세요."
```

## API 직접 호출 (curl)

```bash
curl -X POST http://localhost:8081/rag/dialogue/sse \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "text": "WebFlux란 무엇인가요?",
    "requestedAt": "2025-12-08T12:00:00Z"
  }'
```

## 파이프라인 흐름

```
1. 텍스트 쿼리 입력
   ↓
2. MongoDB에 저장
   ↓
3. RAG: 최신 10건 대화 기록에서 유사 문맥 검색 (키워드 매칭)
   ↓
4. LLM: OpenAI API로 컨텍스트 포함 스트리밍 응답 생성
   ↓
5. 문장 조립: 토큰을 문장 단위로 버퍼링
   ↓
6. TTS: Supertone API로 각 문장을 음성으로 변환
   ↓
7. SSE: Base64 인코딩된 오디오 청크 스트리밍
```

## 문제 해결

### MongoDB 연결 실패
```bash
# MongoDB 컨테이너 상태 확인
docker ps | grep mongodb

# MongoDB 재시작
docker-compose restart mongodb
```

### Redis 연결 실패
```bash
# Redis 컨테이너 상태 확인
docker ps | grep redis

# Redis 재시작
docker-compose restart redis
```

### API 키 오류
`.env` 파일 확인:
```
OPENAI_API_KEY=sk-...
SUPERTONE_API_KEY=sup-...
```

### CORS 오류
이미 CORS 설정 완료되어 있음. 브라우저 콘솔 확인.

## 데이터 확인

### MongoDB 대화 기록 조회
```bash
docker exec -it webflux-mongodb mongosh

use ragdb
db.conversations.find().sort({createdAt: -1}).limit(10)
```

### Redis 캐시 확인
```bash
docker exec -it webflux-redis redis-cli

KEYS *
TTL <key>
```
