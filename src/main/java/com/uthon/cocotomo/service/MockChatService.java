package com.uthon.cocotomo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@ConditionalOnMissingBean(name = "openAiChatModel")
@Slf4j
public class MockChatService {
    
    // 세션별 대화 상태 저장
    private final Map<String, ConversationState> conversationStates = new HashMap<>();
    private final Random random = new Random();
    
    private static class ConversationState {
        List<String> history = new ArrayList<>();
        String currentEmotion = "";
        String lastUserMessage = "";
        int messageCount = 0;
        String currentStage = "";
        Set<String> mentionedTopics = new HashSet<>();
        String userName = "";
    }
    
    public String call(String prompt) {
        try {
            log.debug("Received prompt: {}", prompt);
            
            // 명확한 요청 타입 구분
            if (prompt.startsWith("EMOTION_ANALYSIS_REQUEST")) {
                String cleanPrompt = prompt.substring("EMOTION_ANALYSIS_REQUEST\n".length());
                return handleEmotionAnalysis(cleanPrompt);
            }
            
            if (prompt.startsWith("CONVERSATION_REQUEST")) {
                String cleanPrompt = prompt.substring("CONVERSATION_REQUEST\n".length());
                return handleConversation(cleanPrompt);
            }
            
            // 이전 방식 호환성 유지
            if (isEmotionAnalysisPrompt(prompt)) {
                return handleEmotionAnalysis(prompt);
            }
            
            if (isDiaryGenerationPrompt(prompt)) {
                return handleDiaryGeneration(prompt);
            }
            
            if (isConversationSummaryPrompt(prompt)) {
                return handleConversationSummary(prompt);
            }
            
            // 일반 대화 처리
            return handleConversation(prompt);
            
        } catch (Exception e) {
            log.error("Error in MockChatService", e);
            return "죄송해요, 잠시 생각이 필요해요. 다시 말씀해 주실 수 있나요?";
        }
    }
    
    private boolean isEmotionAnalysisPrompt(String prompt) {
        return prompt.contains("EMOTION_ANALYSIS") && !prompt.contains("SESSION_ID:");
    }
    
    private boolean isDiaryGenerationPrompt(String prompt) {
        return prompt.contains("일기") || prompt.contains("DIARY_GENERATION");
    }
    
    private boolean isConversationSummaryPrompt(String prompt) {
        return prompt.contains("요약") || prompt.contains("CONVERSATION_SUMMARY");
    }
    
    private String handleEmotionAnalysis(String prompt) {
        String userMessage = extractUserMessage(prompt);
        
        if (userMessage.contains("스트레스") || userMessage.contains("힘들") || userMessage.contains("피곤")) {
            return "주요 감정: 스트레스, 피로 / 강도: 7/10 / 세부: 업무 압박으로 인한 정신적 피로감";
        } else if (userMessage.contains("화") || userMessage.contains("짜증") || userMessage.contains("답답")) {
            return "주요 감정: 분노, 답답함 / 강도: 8/10 / 세부: 상황에 대한 통제력 상실감으로 인한 분노";
        } else if (userMessage.contains("슬프") || userMessage.contains("우울") || userMessage.contains("외로")) {
            return "주요 감정: 슬픔, 우울감 / 강도: 6/10 / 세부: 감정적 지지 부족으로 인한 고립감";
        } else if (userMessage.contains("불안") || userMessage.contains("걱정") || userMessage.contains("두려")) {
            return "주요 감정: 불안, 걱정 / 강도: 7/10 / 세부: 미래에 대한 불확실성으로 인한 불안감";
        }
        
        return "주요 감정: 복합적 감정 상태 / 강도: 6/10 / 세부: 상황에 대한 혼재된 감정들";
    }
    
    private String handleDiaryGeneration(String prompt) {
        return """
                📔 오늘의 감정 일기
                
                오늘은 정말 많은 감정이 교차했던 하루였다. 
                처음에는 힘들고 스트레스받는 상황들이 계속되어서 마음이 무거웠지만,
                이렇게 천천히 내 감정을 들여다보는 시간을 가지니 조금씩 정리가 되는 것 같다.
                
                나의 감정들이 모두 자연스럽고 타당한 것들이라는 걸 깨달았다.
                힘든 상황에서 힘들어하는 건 당연한 일이고, 그런 감정을 느끼는 나 자신을 
                조금 더 따뜻하게 바라볼 수 있게 되었다.
                
                내일은 오늘보다 조금 더 나은 하루가 되기를, 그리고 지금의 이 마음가짐을 
                잊지 않고 지낼 수 있기를 바란다.
                """;
    }
    
    private String handleConversationSummary(String prompt) {
        return "사용자의 감정 상태와 상황에 대한 깊이 있는 대화. 감정 인식과 수용 과정을 통한 마음의 정리.";
    }
    
    private String handleConversation(String prompt) {
        String sessionId = extractSessionId(prompt);
        String userMessage = extractUserMessage(prompt);
        String currentStage = extractCurrentStage(prompt);
        List<String> conversationHistory = extractConversationHistory(prompt);
        
        ConversationState state = conversationStates.computeIfAbsent(sessionId, k -> new ConversationState());
        state.lastUserMessage = userMessage;
        state.messageCount++;
        state.currentStage = currentStage;
        
        // 대화 히스토리를 state에 업데이트 (기존 히스토리는 유지하고 새로운 것만 추가)
        if (!conversationHistory.isEmpty()) {
            // 중복 제거를 위해 새로운 히스토리만 추가
            for (String historyItem : conversationHistory) {
                if (!state.history.contains(historyItem)) {
                    state.history.add(historyItem);
                }
            }
        }
        
        // 감정 키워드 추출 및 저장
        updateEmotionAndTopics(state, userMessage);
        
        log.info("Session: {}, Stage: {}, Message count: {}, User message: {}, History size: {}", 
                sessionId, currentStage, state.messageCount, userMessage, state.history.size());
        
        return generateContextualResponse(state, userMessage, currentStage);
    }
    
    private void updateEmotionAndTopics(ConversationState state, String userMessage) {
        // 감정 상태 업데이트
        if (userMessage.contains("스트레스") || userMessage.contains("힘들")) {
            state.currentEmotion = "stressed";
        } else if (userMessage.contains("화") || userMessage.contains("짜증")) {
            state.currentEmotion = "angry";
        } else if (userMessage.contains("슬프") || userMessage.contains("우울")) {
            state.currentEmotion = "sad";
        } else if (userMessage.contains("불안") || userMessage.contains("걱정")) {
            state.currentEmotion = "anxious";
        } else if (userMessage.contains("좋") || userMessage.contains("기쁘")) {
            state.currentEmotion = "happy";
        }
        
        // 주제 추출
        if (userMessage.contains("회사") || userMessage.contains("업무") || userMessage.contains("일")) {
            state.mentionedTopics.add("work");
        }
        if (userMessage.contains("상사") || userMessage.contains("동료")) {
            state.mentionedTopics.add("colleagues");
        }
        if (userMessage.contains("야근") || userMessage.contains("퇴근")) {
            state.mentionedTopics.add("overtime");
        }
        if (userMessage.contains("가족") || userMessage.contains("친구")) {
            state.mentionedTopics.add("relationships");
        }
    }
    
    private String generateContextualResponse(ConversationState state, String userMessage, String currentStage) {
        
        log.debug("Generating contextual response for stage: {}, message count: {}, user message: {}", 
                 currentStage, state.messageCount, userMessage);
        
        // 첫 번째 메시지 (세션 시작)
        if (state.messageCount == 1) {
            return generateInitialResponse(userMessage);
        }
        
        // 단계별 응답 생성 (대화 히스토리와 맥락 고려)
        switch (currentStage) {
            case "INITIAL_EMOTION_ANALYSIS":
                return generateInitialResponse(userMessage);
                
            case "EMPATHY_RESPONSE":
                return generateEmpathyResponse(state, userMessage);
                
            case "FOLLOW_UP_QUESTIONS":
                return generateFollowUpQuestions(state, userMessage);
                
            case "REFLECTION_SESSION":
                return generateReflectionResponse(state, userMessage);
                
            case "RITUAL_SESSION":
                return generateRitualResponse(state, userMessage);
                
            default:
                return generateGeneralResponse(state, userMessage);
        }
    }
    
    private String generateInitialResponse(String userMessage) {
        String[] responses = {
            "안녕하세요! 오늘 하루는 어떠셨나요? 지금 어떤 기분이신지 편하게 말씀해 주세요.",
            "안녕하세요! 오늘 어떤 일이 있으셨는지 천천히 들려주세요. 제가 함께 들어드릴게요.",
            "안녕하세요! 지금 마음이 어떠신지 궁금해요. 편안하게 이야기해 주세요."
        };
        return responses[random.nextInt(responses.length)];
    }
    
    private String generateEmpathyResponse(ConversationState state, String userMessage) {
        
        // 전체 대화 히스토리를 분석하여 맥락 파악
        String fullContext = String.join(" ", state.history) + " " + userMessage;
        log.info("Full conversation context: {}", fullContext);
        
        // 이전 대화에서 언급된 핵심 키워드들 분석
        StringBuilder contextualResponse = new StringBuilder();
        
        // 1. 이전 대화에서 나온 구체적 상황들을 기억하고 언급
        if (fullContext.contains("친구") && fullContext.contains("싸웠")) {
            if (userMessage.contains("사과") || userMessage.contains("어떻게")) {
                return chooseRandom(new String[]{
                    "아까 말씀하신 친구와의 갈등 상황이 계속 마음에 걸리시는군요. 사과하고 싶은 마음이 드시는 것 자체가 소중한 관계라는 증거예요.",
                    "친구와 싸우신 일 때문에 여전히 마음이 무거우시죠. 먼저 다가가고 싶은 마음이 느껴져요. 정말 좋은 친구를 두셨네요.",
                    "처음에 말씀해주신 친구와의 다툼이 계속 신경 쓰이시는 것 같아요. 어떻게 관계를 회복할지 고민이 많으시겠어요."
                });
            }
            
            if (userMessage.contains("당황") || userMessage.contains("화를 내")) {
                return chooseRandom(new String[]{
                    "아까 친구가 갑자기 화를 내서 당황하셨다고 하셨는데, 지금도 그 순간이 계속 떠오르시나 봐요.",
                    "친구가 예상치 못하게 화를 낸 상황이 아직도 마음에 남아있으시군요. 그런 반응을 보이면 정말 당황스러우셨을 거예요.",
                    "처음에 말씀해주신 것처럼 친구의 갑작스러운 반응이 여전히 이해가 안 가시는 것 같아요."
                });
            }
        }
        
        // 2. 직장/업무 관련 맥락 기억
        if (fullContext.contains("회사") || fullContext.contains("상사") || fullContext.contains("야근")) {
            if (userMessage.contains("스트레스") || userMessage.contains("힘들") || userMessage.contains("지쳤")) {
                return chooseRandom(new String[]{
                    "앞서 회사에서의 스트레스 상황을 말씀해주셨는데, 그 영향이 지금도 계속되고 있는 것 같아요.",
                    "상사 때문에 받으신 스트레스가 아직도 마음을 무겁게 하고 있으시군요. 그런 상황이 지속되면 정말 지치실 수밖에 없어요.",
                    "처음에 말씀하신 직장에서의 압박감이 여전히 해결되지 않은 것 같네요. 계속 이런 상황이면 몸과 마음이 버티기 어려우실 거예요."
                });
            }
        }
        
        // 3. 현재 메시지의 감정 상태와 이전 대화 연결
        if (userMessage.contains("어떻게") || userMessage.contains("방법")) {
            return chooseRandom(new String[]{
                "지금까지 말씀해주신 상황들을 종합해보면, 정말 많은 고민이 있으시겠어요. 해결책을 찾고 싶은 마음이 느껴져요.",
                "앞서 나눈 이야기들을 들어보니, 상황을 개선하고 싶은 의지가 강하게 느껴져요. 그런 마음가짐이 정말 소중해요.",
                "이전에 말씀해주신 것들과 지금 하시는 말씀을 보면, 적극적으로 문제를 해결하려는 모습이 보여요."
            });
        }
        
        // 4. 동의하는 응답에 대한 이전 맥락 활용
        if (userMessage.contains("네") || userMessage.contains("맞") || userMessage.contains("그래")) {
            if (!state.history.isEmpty()) {
                return chooseRandom(new String[]{
                    "처음에 말씀해주신 상황과 지금 하시는 말씀을 보면, 그때의 감정이 지금도 이어지고 있는 것 같아요. 더 자세히 말씀해 주세요.",
                    "앞서 나눈 대화를 바탕으로 보면, 아직 해결되지 않은 부분들이 있는 것 같네요. 어떤 부분이 가장 마음에 걸리세요?",
                    "이전에 말씀해주신 것들과 연결해서 생각해보면, 더 구체적으로 어떤 도움이 필요하실까요?"
                });
            }
        }
        
        // 5. 기본 공감 응답 (대화 맥락 포함)
        return chooseRandom(new String[]{
            "지금까지 말씀해주신 모든 상황들이 정말 힘드셨을 것 같아요. 그런 감정들을 느끼는 게 너무 자연스러워요.",
            "처음부터 지금까지의 이야기를 들어보니, 정말 많은 일들을 겪고 계시는군요. 그런 마음이 충분히 이해가 가요.",
            "앞서 나눈 대화들을 통해 얼마나 복잡한 상황에 계신지 알 수 있어요. 이런 감정들을 표현해주셔서 고마워요."
        });
    }
    
    private String generateFollowUpQuestions(ConversationState state, String userMessage) {
        
        // 전체 대화 맥락을 활용한 후속 질문
        String fullContext = String.join(" ", state.history) + " " + userMessage;
        
        // 1. 친구 관련 상황에 대한 구체적 질문
        if (fullContext.contains("친구") && (fullContext.contains("싸웠") || fullContext.contains("갈등"))) {
            return chooseRandom(new String[]{
                "처음에 말씀하신 친구와의 상황에서, 그 친구는 평소에도 이런 식으로 반응하는 편인가요?",
                "앞서 친구가 화를 냈다고 하셨는데, 그 친구와는 얼마나 오래 알고 지내신 사이인가요?",
                "친구와의 다툼 이후로 서로 연락을 하셨나요? 아니면 아직 어색한 상황인가요?",
                "이전에 말씀해주신 친구와의 관계를 돌이켜보면, 이런 일이 처음인가요?"
            });
        }
        
        // 2. 직장/업무 관련 후속 질문
        if (fullContext.contains("회사") || fullContext.contains("상사") || fullContext.contains("직장")) {
            return chooseRandom(new String[]{
                "앞서 말씀하신 직장 상황이 계속 마음에 걸리시는 것 같은데, 이런 일이 자주 있는 편인가요?",
                "처음에 상사 얘기를 하셨는데, 그분과의 관계가 전반적으로 어떤 편이신가요?",
                "회사에서의 스트레스를 집에서도 계속 생각하게 되시나요?",
                "직장에서 이런 상황일 때 의지할 수 있는 동료가 있으신가요?"
            });
        }
        
        // 3. 감정 상태에 따른 깊이 있는 질문
        if (userMessage.contains("어떻게") || userMessage.contains("방법")) {
            return chooseRandom(new String[]{
                "지금까지 나눈 이야기를 보면 해결하고 싶은 마음이 강하게 느껴져요. 이전에 비슷한 상황에서 도움이 되었던 방법이 있나요?",
                "앞서 말씀해주신 상황들을 종합해보면, 가장 우선적으로 해결하고 싶은 부분은 어떤 건가요?",
                "처음부터 지금까지의 상황을 생각해보면, 어떤 점이 가장 마음에 걸리세요?"
            });
        }
        
        // 4. 대화 흐름에 따른 질문
        if (state.messageCount >= 3) {
            return chooseRandom(new String[]{
                "이렇게 여러 가지 이야기를 나누면서, 지금 가장 힘든 부분은 어떤 건가요?",
                "처음에 말씀해주신 것부터 지금까지, 마음의 변화가 있으셨나요?",
                "지금까지의 대화를 통해 새롭게 떠오르는 생각이나 느낌이 있으신가요?"
            });
        }
        
        // 5. 일반적인 후속 질문 (맥락 포함)
        return chooseRandom(new String[]{
            "앞서 말씀해주신 상황에서 가장 답답했던 순간은 언제였나요?",
            "이런 이야기를 나누면서 어떤 기분이 드세요?",
            "지금까지 말씀해주신 것들 중에서 가장 마음에 와 닿는 부분이 있다면 어떤 건가요?",
            "이전 대화에서 나온 내용들과 연결해서 생각해보면, 어떤 패턴이 있는 것 같나요?"
        });
    }
    
    private String generateReflectionResponse(ConversationState state, String userMessage) {
        
        if (userMessage.contains("생각") || userMessage.contains("느낌") || userMessage.contains("마음")) {
            return chooseRandom(new String[]{
                "그런 생각이 드시는군요. 오늘 하루를 돌아보면서 어떤 부분에서 가장 '나답다'고 느끼셨나요?",
                "그 마음을 좀 더 깊이 들여다보면, 어떤 것이 가장 중요하게 느껴지세요?",
                "지금까지 얘기하신 걸 들어보니, 당신이 정말 세심하고 책임감이 강한 분이라는 게 느껴져요."
            });
        }
        
        if (state.mentionedTopics.contains("work")) {
            return chooseRandom(new String[]{
                "일을 하면서 가장 보람을 느끼는 순간은 언제인가요? 힘든 일들만 있는 건 아니잖아요.",
                "이런 어려운 상황들이 있지만, 그래도 이 일을 계속하게 만드는 이유가 있나요?",
                "직장에서의 경험들이 당신을 어떤 사람으로 성장시켰다고 생각하세요?"
            });
        }
        
        return chooseRandom(new String[] {
            "오늘 대화를 통해서 자신에 대해 새롭게 알게 된 것이 있나요?",
            "이런 감정들을 겪으면서도 버텨낼 수 있게 해주는 당신만의 힘이 있다면 무엇일까요?",
            "과거에 비슷한 어려움을 겪었을 때 도움이 되었던 것들이 있나요?",
            "지금의 이 감정을 색깔로 표현한다면 어떤 색일까요? 그 이유도 궁금해요."
        });
    }
    
    private String generateRitualResponse(ConversationState state, String userMessage) {
        
        if (userMessage.contains("도움") || userMessage.contains("방법") || userMessage.contains("해결")) {
            return chooseRandom(new String[]{
                "정말 현명한 접근이에요. 작은 변화부터 시작하는 게 가장 지속 가능한 방법이죠.",
                "그런 마음가짐이 정말 중요해요. 스스로를 위한 변화를 만들어가는 거네요.",
                "좋은 방향으로 생각하고 계시는 것 같아요. 그런 의지가 있으시면 분명 달라질 거예요."
            });
        }
        
        if (userMessage.contains("앞으로") || userMessage.contains("내일") || userMessage.contains("미래")) {
            return chooseRandom(new String[]{
                "미래에 대한 긍정적인 계획을 세우고 계시는군요. 그런 희망이 정말 소중해요.",
                "내일부터 작은 것 하나씩 실천해보시는 건 어떨까요? 완벽하지 않아도 괜찮으니까요.",
                "변화에 대한 의지가 느껴져요. 그런 마음이 첫 번째 변화의 시작이죠."
            });
        }
        
        return chooseRandom(new String[]{
            "오늘 대화를 통해 많은 것들을 정리하셨네요. 스스로에게 조금 더 관대해지셔도 좋을 것 같아요.",
            "당신이 느끼는 모든 감정들이 소중하고 의미가 있어요. 그런 감정들을 통해 성장하고 계시는 거예요.",
            "오늘의 이 시간이 앞으로 더 나은 내일들을 만들어가는 디딤돌이 되었으면 좋겠어요.",
            "힘든 시간을 보내셨지만, 이렇게 자신을 돌아보는 시간을 갖는 것 자체가 정말 용기 있는 일이에요."
        });
    }
    
    private String generateGeneralResponse(ConversationState state, String userMessage) {
        
        if (userMessage.contains("네") || userMessage.contains("응") || userMessage.contains("그래")) {
            return chooseRandom(new String[]{
                "더 자세히 이야기해 주실 수 있나요?",
                "그 부분에 대해 좀 더 구체적으로 들려주세요.",
                "어떤 기분이셨는지 더 말씀해 주시겠어요?"
            });
        }
        
        return chooseRandom(new String[]{
            "말씀해 주신 내용이 정말 의미 있게 느껴져요. 더 이야기하고 싶은 부분이 있나요?",
            "그런 경험을 하셨군요. 그때 마음이 어떠셨는지 더 알고 싶어요.",
            "이해해요. 그런 상황에서 어떤 점이 가장 어려웠나요?"
        });
    }
    
    private String chooseRandom(String[] options) {
        return options[random.nextInt(options.length)];
    }
    
    // 새로운 단순화된 프롬프트 파싱 메서드들
    private String extractSessionId(String prompt) {
        String[] lines = prompt.split("\n");
        for (String line : lines) {
            if (line.startsWith("SESSION_ID:")) {
                String sessionId = line.substring("SESSION_ID:".length()).trim();
                if (!sessionId.isEmpty()) {
                    log.debug("Extracted session ID: {}", sessionId);
                    return sessionId;
                }
            }
        }
        return "default-session";
    }
    
    private String extractUserMessage(String prompt) {
        log.debug("Extracting user message from prompt: {}", prompt);
        
        // 감정 분석 프롬프트 형태 처리
        if (prompt.contains("{USER_MESSAGE}")) {
            String userMessage = prompt.replace("{USER_MESSAGE}", "").trim();
            log.debug("Extracted user message from emotion analysis: {}", userMessage);
            return userMessage;
        }
        
        // 일반 대화 프롬프트 형태 처리  
        String[] lines = prompt.split("\n");
        for (String line : lines) {
            if (line.startsWith("USER_MESSAGE:")) {
                String userMessage = line.substring("USER_MESSAGE:".length()).trim();
                log.debug("Extracted user message: {}", userMessage);
                return userMessage;
            }
        }
        
        log.warn("Could not extract user message from prompt");
        return "";
    }
    
    private String extractCurrentStage(String prompt) {
        String[] lines = prompt.split("\n");
        for (String line : lines) {
            if (line.startsWith("STAGE:")) {
                String stage = line.substring("STAGE:".length()).trim();
                log.debug("Extracted stage: {}", stage);
                return stage;
            }
        }
        return "EMPATHY_RESPONSE";
    }
    
    private List<String> extractConversationHistory(String prompt) {
        List<String> history = new ArrayList<>();
        String[] lines = prompt.split("\n");
        boolean inHistory = false;
        
        for (String line : lines) {
            if (line.equals("HISTORY_START")) {
                inHistory = true;
                continue;
            }
            if (line.equals("HISTORY_END")) {
                inHistory = false;
                break;
            }
            if (inHistory && !line.trim().isEmpty()) {
                history.add(line.trim());
                log.debug("Added to conversation history: {}", line.trim());
            }
        }
        
        log.debug("Extracted conversation history: {} items", history.size());
        return history;
    }
}