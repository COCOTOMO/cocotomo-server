package com.uthon.cocotomo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uthon.cocotomo.dto.ChatMessageRequest;
import com.uthon.cocotomo.dto.ChatMessageResponse;
import com.uthon.cocotomo.dto.ChatSessionCreateRequest;
import com.uthon.cocotomo.dto.ChatSessionHistoryResponse;
import com.uthon.cocotomo.entity.ChatSession;
import com.uthon.cocotomo.entity.User;
import com.uthon.cocotomo.repository.ChatSessionRepository;
import com.uthon.cocotomo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmotionChatService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private OpenAiChatModel openAiChatModel;
    
    @Autowired(required = false)
    private MockChatService mockChatService;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    public EmotionChatService(ChatSessionRepository chatSessionRepository,
                             UserRepository userRepository,
                             PromptTemplateService promptTemplateService,
                             ObjectMapper objectMapper) {
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
        this.promptTemplateService = promptTemplateService;
        this.objectMapper = objectMapper;
    }
    
    private static final String CONVERSATION_HISTORY_KEY = "chat:conversation:";
    private static final int CONVERSATION_HISTORY_TTL = 24; // 24시간
    
    // 메모리 기반 대화 히스토리 저장소
    private final Map<String, List<String>> conversationHistoryMap = new ConcurrentHashMap<>();
    
    private String callAIService(String prompt) {
        if (openAiChatModel != null) {
            log.info("Using OpenAI ChatModel");
            return openAiChatModel.call(prompt);
        } else if (mockChatService != null) {
            log.info("Using Mock Chat Service");
            return mockChatService.call(prompt);
        } else {
            log.warn("No AI service available, returning default response");
            return "AI 서비스가 현재 사용할 수 없습니다. 나중에 다시 시도해 주세요.";
        }
    }
    
    @Transactional
    public ChatMessageResponse createNewSession(ChatSessionCreateRequest request) {
        User user = userRepository.findById(Long.parseLong(request.getUserId()))
                .orElseGet(() -> {
                    // Create test user if not exists
                    User newUser = new User();
                    newUser.setEmail("test@example.com");
                    newUser.setPassword("testpassword");
                    newUser.setRole("USER");
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });
        
        String sessionId = UUID.randomUUID().toString();
        
        // Create new chat session
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .user(user)
                .currentStage(ChatSession.ChatStage.INITIAL_EMOTION_ANALYSIS)
                .build();
        
        chatSessionRepository.save(chatSession);
        
        // Process initial message
        ChatMessageRequest messageRequest = new ChatMessageRequest(sessionId, request.getInitialMessage(), request.getUserId());
        return processUserMessage(messageRequest);
    }
    
    @Transactional
    public ChatMessageResponse processUserMessage(ChatMessageRequest request) {
        ChatSession session = chatSessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // Get conversation history
        List<String> conversationHistory = getConversationHistory(request.getSessionId());
        
        // Add user message to history
        addToConversationHistory(request.getSessionId(), "User: " + request.getMessage());
        
        // Process based on current stage
        String aiResponse = generateAIResponse(session, request.getMessage(), conversationHistory);
        
        // Add AI response to history
        addToConversationHistory(request.getSessionId(), "AI: " + aiResponse);
        
        // Update session stage and data
        updateSessionProgress(session, request.getMessage(), aiResponse);
        
        // Build response
        return ChatMessageResponse.builder()
                .sessionId(request.getSessionId())
                .aiResponse(aiResponse)
                .currentStage(session.getCurrentStage())
                .emotionAnalysis(session.getEmotionAnalysis())
                .sessionCompleted(session.getCurrentStage() == ChatSession.ChatStage.COMPLETED)
                .generatedDiary(session.getGeneratedDiary())
                .build();
    }
    
    private String generateAIResponse(ChatSession session, String userMessage, List<String> conversationHistory) {
        String systemPrompt = promptTemplateService.getSystemPromptForStage(session.getCurrentStage());
        String contextPrompt = promptTemplateService.buildContextPrompt(session, conversationHistory, userMessage);
        
        try {
            // 일반 대화임을 명시하여 MockChatService가 구분할 수 있도록
            String fullPrompt = "CONVERSATION_REQUEST\n" + buildStructuredPrompt(session, systemPrompt, contextPrompt, userMessage, conversationHistory);
            return callAIService(fullPrompt);
        } catch (Exception e) {
            log.error("Error generating AI response: ", e);
            return "죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해 주세요.";
        }
    }
    
    private String buildStructuredPrompt(ChatSession session, String systemPrompt, String contextPrompt, String userMessage, List<String> conversationHistory) {
        // 단순화된 프롬프트 구조 - MockChatService가 쉽게 파싱할 수 있도록
        StringBuilder prompt = new StringBuilder();
        
        // 핵심 정보만 포함
        prompt.append("SESSION_ID:").append(session.getSessionId()).append("\n");
        prompt.append("STAGE:").append(session.getCurrentStage()).append("\n");
        
        // 이전 대화 내용
        if (!conversationHistory.isEmpty()) {
            prompt.append("HISTORY_START\n");
            int startIndex = Math.max(0, conversationHistory.size() - 6);
            for (int i = startIndex; i < conversationHistory.size(); i++) {
                prompt.append(conversationHistory.get(i)).append("\n");
            }
            prompt.append("HISTORY_END\n");
        }
        
        // 현재 사용자 메시지 - 가장 중요하고 파싱하기 쉬운 형태
        prompt.append("USER_MESSAGE:").append(userMessage).append("\n");
        
        log.debug("Built simplified prompt for session {}: {}", session.getSessionId(), prompt.toString());
        
        return prompt.toString();
    }
    
    @Transactional
    private void updateSessionProgress(ChatSession session, String userMessage, String aiResponse) {
        // Analyze emotion if in initial stage
        if (session.getCurrentStage() == ChatSession.ChatStage.INITIAL_EMOTION_ANALYSIS) {
            String emotionAnalysis = analyzeEmotion(userMessage);
            session.setEmotionAnalysis(emotionAnalysis);
            session.setCurrentStage(ChatSession.ChatStage.EMPATHY_RESPONSE);
        }
        // Progress through stages based on conversation flow
        else {
            progressToNextStage(session, userMessage, aiResponse);
        }
        
        chatSessionRepository.save(session);
    }
    
    private String analyzeEmotion(String message) {
        String emotionPrompt = promptTemplateService.getEmotionAnalysisPrompt(message);
        
        try {
            // 감정 분석임을 명시하여 MockChatService가 구분할 수 있도록
            String prefixedPrompt = "EMOTION_ANALYSIS_REQUEST\n" + emotionPrompt;
            return callAIService(prefixedPrompt);
        } catch (Exception e) {
            log.error("Error analyzing emotion: ", e);
            return "감정 분석 중 오류가 발생했습니다.";
        }
    }
    
    private void progressToNextStage(ChatSession session, String userMessage, String aiResponse) {
        List<String> conversationHistory = getConversationHistory(session.getSessionId());
        
        // Determine if should progress to next stage based on conversation length and content
        int conversationTurns = conversationHistory.size() / 2; // Divide by 2 since each turn has user + AI message
        
        switch (session.getCurrentStage()) {
            case EMPATHY_RESPONSE:
                // Progress after 2-3 turns, or if user provides detailed emotional response
                if (conversationTurns >= 2 && (conversationTurns >= 3 || isDetailedEmotionalResponse(userMessage))) {
                    session.setCurrentStage(ChatSession.ChatStage.FOLLOW_UP_QUESTIONS);
                }
                break;
                
            case FOLLOW_UP_QUESTIONS:
                // Progress after 3-5 turns, or if user shows readiness for reflection
                if (conversationTurns >= 3 && (conversationTurns >= 5 || showsReflectiveThinking(userMessage))) {
                    session.setCurrentStage(ChatSession.ChatStage.REFLECTION_SESSION);
                }
                break;
                
            case REFLECTION_SESSION:
                // Progress after 4-6 turns, or if user demonstrates self-awareness
                if (conversationTurns >= 4 && (conversationTurns >= 6 || showsSelfAwareness(userMessage))) {
                    session.setCurrentStage(ChatSession.ChatStage.RITUAL_SESSION);
                }
                break;
                
            case RITUAL_SESSION:
                // Progress after 3-5 turns in ritual stage, or if user commits to action
                if (conversationTurns >= 6 && (conversationTurns >= 8 || showsActionCommitment(userMessage))) {
                    generateDiaryAndComplete(session);
                }
                break;
                
            default:
                break;
        }
    }
    
    private boolean isDetailedEmotionalResponse(String userMessage) {
        return userMessage.length() > 20 && 
               (userMessage.contains("느꼈") || userMessage.contains("생각했") || 
                userMessage.contains("기분") || userMessage.contains("마음"));
    }
    
    private boolean showsReflectiveThinking(String userMessage) {
        return userMessage.contains("왜") || userMessage.contains("어떻게") || 
               userMessage.contains("원인") || userMessage.contains("이유") ||
               userMessage.contains("때문에") || userMessage.contains("생각해보니");
    }
    
    private boolean showsSelfAwareness(String userMessage) {
        return userMessage.contains("내가") || userMessage.contains("나는") || 
               userMessage.contains("스스로") || userMessage.contains("깨달았") ||
               userMessage.contains("알겠") || userMessage.contains("이해했");
    }
    
    private boolean showsActionCommitment(String userMessage) {
        return userMessage.contains("해야겠") || userMessage.contains("할 거") || 
               userMessage.contains("노력") || userMessage.contains("시도") ||
               userMessage.contains("계획") || userMessage.contains("앞으로");
    }
    
    private void generateDiaryAndComplete(ChatSession session) {
        List<String> conversationHistory = getConversationHistory(session.getSessionId());
        String diaryPrompt = promptTemplateService.getDiaryGenerationPrompt(conversationHistory, session.getEmotionAnalysis());
        
        try {
            String generatedDiary = callAIService(diaryPrompt);
            
            session.setGeneratedDiary(generatedDiary);
            session.setCurrentStage(ChatSession.ChatStage.DIARY_GENERATION);
            session.setCompletedAt(LocalDateTime.now());
            
            // Generate final summary
            String summary = generateConversationSummary(conversationHistory);
            session.setConversationSummary(summary);
            
            session.setCurrentStage(ChatSession.ChatStage.COMPLETED);
            
        } catch (Exception e) {
            log.error("Error generating diary: ", e);
            session.setGeneratedDiary("일기 생성 중 오류가 발생했습니다.");
        }
    }
    
    private String generateConversationSummary(List<String> conversationHistory) {
        String summaryPrompt = promptTemplateService.getConversationSummaryPrompt(conversationHistory);
        
        try {
            return callAIService(summaryPrompt);
        } catch (Exception e) {
            log.error("Error generating summary: ", e);
            return "대화 요약 생성 중 오류가 발생했습니다.";
        }
    }
    
    public ChatSessionHistoryResponse getSessionInfo(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        return buildSessionHistoryResponse(session);
    }
    
    public List<ChatSessionHistoryResponse> getUserSessionHistory(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ChatSession> sessions = chatSessionRepository.findByUserOrderByCreatedAtDesc(user);
        
        return sessions.stream()
                .map(this::buildSessionHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ChatMessageResponse forceCompleteSession(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getCurrentStage() != ChatSession.ChatStage.COMPLETED) {
            generateDiaryAndComplete(session);
            chatSessionRepository.save(session);
        }
        
        return ChatMessageResponse.builder()
                .sessionId(sessionId)
                .aiResponse("세션이 완료되었습니다. 생성된 일기를 확인해 주세요.")
                .currentStage(session.getCurrentStage())
                .emotionAnalysis(session.getEmotionAnalysis())
                .sessionCompleted(true)
                .generatedDiary(session.getGeneratedDiary())
                .build();
    }
    
    private ChatSessionHistoryResponse buildSessionHistoryResponse(ChatSession session) {
        return ChatSessionHistoryResponse.builder()
                .sessionId(session.getSessionId())
                .currentStage(session.getCurrentStage())
                .conversationSummary(session.getConversationSummary())
                .generatedDiary(session.getGeneratedDiary())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .isCompleted(session.getCurrentStage() == ChatSession.ChatStage.COMPLETED)
                .build();
    }
    
    // Conversation history management
    private List<String> getConversationHistory(String sessionId) {
        return conversationHistoryMap.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());
    }
    
    private void addToConversationHistory(String sessionId, String message) {
        List<String> history = getConversationHistory(sessionId);
        history.add(message);
        log.debug("Added to conversation history for session {}: {}", sessionId, message);
    }
}