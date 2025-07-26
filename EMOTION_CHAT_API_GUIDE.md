# 감정 회고 챗봇 API 가이드

## 개요
감정 회고 챗봇은 사용자의 감정 상태를 분석하고 단계별 대화를 통해 감정을 정리한 후, 최종적으로 일기를 생성하는 서비스입니다.

## 챗봇 진행 단계 (ChatStage)
1. **INITIAL_EMOTION_ANALYSIS** - 초기 감정 분석
2. **EMPATHY_RESPONSE** - 공감 및 위로
3. **FOLLOW_UP_QUESTIONS** - 후속 질문
4. **REFLECTION_SESSION** - 회고 세션
5. **RITUAL_SESSION** - 리추얼 세션 (문제점 및 방향성 제시)
6. **DIARY_GENERATION** - 일기 생성
7. **COMPLETED** - 완료

## API 엔드포인트

### 1. 새로운 챗봇 세션 시작
```
POST /emotion-chat/sessions
```

**Request Body:**
```json
{
    "userId": "1",
    "initialMessage": "오늘 정말 힘든 하루였어"
}
```

**Response:**
```json
{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "aiResponse": "안녕하세요! 오늘 하루는 어떠셨나요? 지금 어떤 기분이신지 편하게 말씀해 주세요.",
    "currentStage": "INITIAL_EMOTION_ANALYSIS",
    "emotionAnalysis": null,
    "sessionCompleted": false,
    "generatedDiary": null
}
```

### 2. 챗봇 메시지 전송
```
POST /emotion-chat/messages
```

**Request Body:**
```json
{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "message": "회사에서 상사가 계속 야근을 시켜서 스트레스가 쌓여",
    "userId": "1"
}
```

**Response:**
```json
{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "aiResponse": "정말 힘든 하루를 보내셨네요. 그런 마음이 드는 게 너무 자연스러워요.",
    "currentStage": "EMPATHY_RESPONSE",
    "emotionAnalysis": "주요 감정: 피로, 스트레스 / 강도: 7/10",
    "sessionCompleted": false,
    "generatedDiary": null
}
```

### 3. 세션 정보 조회
```
GET /emotion-chat/sessions/{sessionId}
```

**Response:**
```json
{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "currentStage": "FOLLOW_UP_QUESTIONS",
    "conversationSummary": "업무 스트레스로 인한 피로감, 상사와의 갈등",
    "generatedDiary": null,
    "createdAt": "2024-01-15T10:30:00",
    "completedAt": null,
    "isCompleted": false
}
```

### 4. 사용자 세션 목록 조회
```
GET /emotion-chat/sessions/user/{userId}
```

**Response:**
```json
[
    {
        "sessionId": "session-001",
        "currentStage": "COMPLETED",
        "conversationSummary": "스트레스가 많은 하루, 일의 복잡함으로 인한 피로감",
        "generatedDiary": "오늘은 정말 힘든 하루였다...",
        "createdAt": "2024-01-14T15:20:00",
        "completedAt": "2024-01-14T16:20:00",
        "isCompleted": true
    },
    {
        "sessionId": "session-002",
        "currentStage": "REFLECTION_SESSION",
        "conversationSummary": "업무 성취감과 동시에 느끼는 외로움",
        "generatedDiary": null,
        "createdAt": "2024-01-15T10:30:00",
        "completedAt": null,
        "isCompleted": false
    }
]
```

### 5. 세션 강제 완료
```
POST /emotion-chat/sessions/{sessionId}/complete
```

**Response:**
```json
{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "aiResponse": "세션이 완료되었습니다. 생성된 일기를 확인해 주세요.",
    "currentStage": "COMPLETED",
    "emotionAnalysis": "주요 감정: 피로에서 안정감으로 변화 / 최종 강도: 4/10",
    "sessionCompleted": true,
    "generatedDiary": "오늘의 감정 일기\n\n오늘은 정말 힘든 하루였다..."
}
```

## 테스트 API (개발/테스트용)

### 1. Mock 세션 생성 응답
```
GET /api/emotion-chat/test/mock-session-create
```

### 2. Mock 공감 응답
```
GET /api/emotion-chat/test/mock-empathy-response
```

### 3. Mock 후속 질문 응답
```
GET /api/emotion-chat/test/mock-follow-up
```

### 4. Mock 완료된 세션
```
GET /api/emotion-chat/test/mock-completed-session
```

### 5. Mock 세션 히스토리
```
GET /api/emotion-chat/test/mock-session-history
```

### 6. Mock 감정 분석
```
GET /api/emotion-chat/test/mock-emotion-analysis
```

## 사용자 플로우 테스트 시나리오

### 시나리오 1: 전체 세션 완료 플로우

1. **세션 시작**
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/sessions \
   -H "Content-Type: application/json" \
   -d '{
     "userId": "1",
     "initialMessage": "오늘 회사에서 너무 스트레스를 받았어"
   }'
   ```

2. **공감 단계 대화**
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/messages \
   -H "Content-Type: application/json" \
   -d '{
     "sessionId": "your-session-id",
     "message": "상사가 계속 야근을 시켜서 정말 화가 나",
     "userId": "1"
   }'
   ```

3. **후속 질문 단계**
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/messages \
   -H "Content-Type: application/json" \
   -d '{
     "sessionId": "your-session-id",
     "message": "가장 화났던 순간은 퇴근 시간이 지났는데도 추가 업무를 줄 때였어",
     "userId": "1"
   }'
   ```

4. **회고 세션**
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/messages \
   -H "Content-Type: application/json" \
   -d '{
     "sessionId": "your-session-id",
     "message": "이런 상황이 계속되면 번아웃이 올 것 같아",
     "userId": "1"
   }'
   ```

5. **리추얼 세션**
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/messages \
   -H "Content-Type: application/json" \
   -d '{
     "sessionId": "your-session-id",
     "message": "앞으로는 명확한 경계를 설정해야겠어",
     "userId": "1"
   }'
   ```

6. **최종 완료 (자동 일기 생성)**
   - 8회 이상 대화 후 자동으로 일기 생성되거나
   - 강제 완료 API 호출

### 시나리오 2: 강제 완료 테스트

1. 세션 시작 후 몇 번의 대화
2. 강제 완료 API 호출:
   ```bash
   curl -X POST http://localhost:8080/emotion-chat/sessions/{sessionId}/complete
   ```

### 시나리오 3: 세션 조회 테스트

1. **특정 세션 조회**
   ```bash
   curl http://localhost:8080/emotion-chat/sessions/{sessionId}
   ```

2. **사용자 전체 세션 목록 조회**
   ```bash
   curl http://localhost:8080/emotion-chat/sessions/user/1
   ```

## 중요 참고사항

### 단계별 진행 조건
- **EMPATHY_RESPONSE**: 2회 이상 대화 후 다음 단계
- **FOLLOW_UP_QUESTIONS**: 4회 이상 대화 후 다음 단계  
- **REFLECTION_SESSION**: 6회 이상 대화 후 다음 단계
- **RITUAL_SESSION**: 8회 이상 대화 후 자동 일기 생성 및 완료

### 데이터 저장
- **세션 정보**: MySQL/H2 데이터베이스에 저장
- **대화 히스토리**: Redis에 24시간 TTL로 캐시 (Redis 없으면 메모리 사용)

### AI 서비스
- **OpenAI**: 실제 OpenAI API 사용 (API 키 필요)
- **Mock Service**: OpenAI 없을 때 테스트용 응답 제공

### 인증
- JWT 토큰 기반 인증 시스템과 연동
- userId는 인증된 사용자의 ID 사용

### 에러 처리
- 세션 없음: "Session not found" 
- 사용자 없음: "User not found"
- AI 서비스 오류: 기본 에러 메시지 반환

## Swagger UI
http://localhost:8080/swagger-ui/index.html 에서 전체 API 문서 확인 가능