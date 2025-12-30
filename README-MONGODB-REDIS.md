# MongoDB + Redis Setup Guide

## Quick Start

### 1. Start MongoDB & Redis
```bash
docker-compose up -d
```

### 2. Verify Services
```bash
docker ps
```

Expected output:
```
webflux-mongodb  (port 27017)
webflux-redis    (port 6379)
```

### 3. Run Application
```bash
# Set environment variables
export OPENAI_API_KEY=your-openai-key
export SUPERTONE_API_KEY=your-supertone-key

# Run webflux-rag
./gradlew :webflux-rag:bootRun
```

## Database Info

### MongoDB
- **URL**: `mongodb://localhost:27017/ragdb`
- **Database**: `ragdb`
- **Collection**: `conversations`
- **Purpose**: 대화 기록 영속 저장

### Redis
- **Host**: `localhost`
- **Port**: `6379`
- **Purpose**: 캐싱 (10분 TTL)

## Repository Features

### ConversationHistoryRepository
```java
// 대화 저장 (MongoDB)
Mono<ConversationMessage> saveQuery(String query)

// 최신 10건 조회
Flux<ConversationMessage> findTop10ByOrderByCreatedAtDesc()

// 전체 조회
Flux<ConversationMessage> findAll()
```

## Stop Services
```bash
docker-compose down
```

## Clean Data
```bash
docker-compose down -v
```
