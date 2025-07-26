package com.uthon.cocotomo.service;

import com.uthon.cocotomo.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateService {
    
    public String getSystemPromptForStage(ChatSession.ChatStage stage) {
        switch (stage) {
            case INITIAL_EMOTION_ANALYSIS:
                return loadPromptTemplate("prompts/emotion-analysis-system.txt");
                
            case EMPATHY_RESPONSE:
                return loadPromptTemplate("prompts/empathy-response-system.txt");
                
            case FOLLOW_UP_QUESTIONS:
                return loadPromptTemplate("prompts/follow-up-questions-system.txt");
                
            case REFLECTION_SESSION:
                return loadPromptTemplate("prompts/reflection-session-system.txt");
                
            case RITUAL_SESSION:
                return loadPromptTemplate("prompts/ritual-session-system.txt");
                
            case DIARY_GENERATION:
                return loadPromptTemplate("prompts/diary-generation-system.txt");
                
            default:
                return getDefaultSystemPrompt();
        }
    }
    
    public String buildContextPrompt(ChatSession session, List<String> conversationHistory, String userMessage) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 간단한 컨텍스트 정보만 추가 (EmotionChatService에서 이미 구조화함)
        contextBuilder.append("지침: 사용자의 감정을 충분히 이해하고 공감하며, ");
        contextBuilder.append("현재 대화 단계에 맞는 적절한 응답을 제공하세요. ");
        contextBuilder.append("자연스럽고 따뜻한 톤으로 대화를 이어가세요.");
        
        return contextBuilder.toString();
    }
    
    public String getEmotionAnalysisPrompt(String userMessage) {
        String template = loadPromptTemplate("prompts/emotion-analysis-prompt.txt");
        return template.replace("{USER_MESSAGE}", userMessage);
    }
    
    public String getDiaryGenerationPrompt(List<String> conversationHistory, String emotionAnalysis) {
        StringBuilder conversationText = new StringBuilder();
        for (String message : conversationHistory) {
            conversationText.append(message).append("\n");
        }
        
        String template = loadPromptTemplate("prompts/diary-generation-prompt.txt");
        return template
                .replace("{CONVERSATION_HISTORY}", conversationText.toString())
                .replace("{EMOTION_ANALYSIS}", emotionAnalysis != null ? emotionAnalysis : "감정 분석 정보 없음");
    }
    
    public String getConversationSummaryPrompt(List<String> conversationHistory) {
        StringBuilder conversationText = new StringBuilder();
        for (String message : conversationHistory) {
            conversationText.append(message).append("\n");
        }
        
        String template = loadPromptTemplate("prompts/conversation-summary-prompt.txt");
        return template.replace("{CONVERSATION_HISTORY}", conversationText.toString());
    }
    
    private String getStageDescription(ChatSession.ChatStage stage) {
        switch (stage) {
            case INITIAL_EMOTION_ANALYSIS:
                return "초기 감정 분석 - 사용자의 감정을 파악하는 단계";
            case EMPATHY_RESPONSE:
                return "공감 및 위로 - 사용자의 감정에 공감하고 위로하는 단계";
            case FOLLOW_UP_QUESTIONS:
                return "후속 질문 - 더 깊은 대화를 위한 질문을 하는 단계";
            case REFLECTION_SESSION:
                return "회고 세션 - 하루를 돌아보며 정리하는 단계";
            case RITUAL_SESSION:
                return "리추얼 세션 - 문제점과 개선 방향을 제시하는 단계";
            case DIARY_GENERATION:
                return "일기 생성 - 대화 내용을 바탕으로 일기를 작성하는 단계";
            case COMPLETED:
                return "완료 - 모든 과정이 완료된 상태";
            default:
                return "알 수 없는 단계";
        }
    }
    
    private String getDefaultSystemPrompt() {
        return """
                당신은 감정 회고를 도와주는 친근하고 공감적인 AI 상담사입니다.
                사용자의 감정을 이해하고 공감하며, 적절한 위로와 조언을 제공하세요.
                대화는 자연스럽고 따뜻한 톤으로 진행하되, 사용자의 상황에 맞는 적절한 질문을 통해 깊이 있는 회고를 이끌어내세요.
                """;
    }
    
    private String loadPromptTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.warn("Prompt template not found: {}, using default", path);
                return getDefaultSystemPrompt();
            }
            
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading prompt template: {}", path, e);
            return getDefaultSystemPrompt();
        }
    }
}