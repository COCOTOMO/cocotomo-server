package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.*;
import com.uthon.cocotomo.entity.ChatSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/emotion-chat/test")
@Tag(name = "감정 회고 챗봇 테스트", description = "테스트용 Mock 데이터 API")
public class EmotionChatTestController {
    
    @GetMapping("/mock-session-create")
    @Operation(summary = "Mock 세션 생성 응답", description = "테스트용 세션 생성 응답 데이터")
    public ResponseEntity<ChatMessageResponse> getMockSessionCreate() {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId("test-session-123")
                .aiResponse("안녕하세요! 오늘 하루는 어떠셨나요? 지금 어떤 기분이신지 편하게 말씀해 주세요.")
                .currentStage(ChatSession.ChatStage.INITIAL_EMOTION_ANALYSIS)
                .emotionAnalysis(null)
                .sessionCompleted(false)
                .generatedDiary(null)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mock-empathy-response")
    @Operation(summary = "Mock 공감 응답", description = "테스트용 공감 단계 응답 데이터")
    public ResponseEntity<ChatMessageResponse> getMockEmpathyResponse() {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId("test-session-123")
                .aiResponse("정말 힘든 하루를 보내셨네요. 그런 마음이 드는 게 너무 자연스러워요. 혼자서 이 모든 걸 감당하느라 얼마나 지치셨을까요.")
                .currentStage(ChatSession.ChatStage.EMPATHY_RESPONSE)
                .emotionAnalysis("주요 감정: 피로, 스트레스 / 강도: 7/10")
                .sessionCompleted(false)
                .generatedDiary(null)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mock-follow-up")
    @Operation(summary = "Mock 후속 질문 응답", description = "테스트용 후속 질문 단계 응답 데이터")
    public ResponseEntity<ChatMessageResponse> getMockFollowUpResponse() {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId("test-session-123")
                .aiResponse("그 상황에서 가장 답답했던 순간은 언제였나요? 그때 몸으로는 어떤 느낌이 들었는지도 궁금해요.")
                .currentStage(ChatSession.ChatStage.FOLLOW_UP_QUESTIONS)
                .emotionAnalysis("주요 감정: 피로, 스트레스, 답답함 / 강도: 7/10")
                .sessionCompleted(false)
                .generatedDiary(null)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mock-completed-session")
    @Operation(summary = "Mock 완료된 세션", description = "테스트용 완료된 세션 응답 데이터")
    public ResponseEntity<ChatMessageResponse> getMockCompletedSession() {
        String mockDiary = """
                오늘의 감정 일기
                
                오늘은 정말 힘든 하루였다. 일이 생각보다 복잡해지면서 스트레스가 쌓였고, 
                마음도 무거워졌다. 하지만 누군가와 이야기를 나누면서 내 감정을 정리할 수 있어서 다행이었다.
                
                때로는 완벽하지 않아도 괜찮다는 걸 다시 한번 깨달았다. 
                내일은 조금 더 여유를 가지고 하루를 시작해보려고 한다.
                """;
        
        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId("test-session-123")
                .aiResponse("오늘의 감정 회고가 모두 완료되었습니다. 생성된 일기를 확인해 보세요.")
                .currentStage(ChatSession.ChatStage.COMPLETED)
                .emotionAnalysis("주요 감정: 피로에서 안정감으로 변화 / 최종 강도: 4/10")
                .sessionCompleted(true)
                .generatedDiary(mockDiary)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mock-session-history")
    @Operation(summary = "Mock 세션 히스토리", description = "테스트용 사용자 세션 목록 데이터")
    public ResponseEntity<List<ChatSessionHistoryResponse>> getMockSessionHistory() {
        List<ChatSessionHistoryResponse> history = Arrays.asList(
                ChatSessionHistoryResponse.builder()
                        .sessionId("session-001")
                        .currentStage(ChatSession.ChatStage.COMPLETED)
                        .conversationSummary("스트레스가 많은 하루, 일의 복잡함으로 인한 피로감, 대화를 통한 감정 정리")
                        .generatedDiary("오늘은 정말 힘든 하루였다. 하지만 누군가와 이야기하면서...")
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .completedAt(LocalDateTime.now().minusDays(1).plusHours(1))
                        .isCompleted(true)
                        .build(),
                        
                ChatSessionHistoryResponse.builder()
                        .sessionId("session-002")
                        .currentStage(ChatSession.ChatStage.REFLECTION_SESSION)
                        .conversationSummary("업무 성취감과 동시에 느끼는 외로움에 대한 대화")
                        .generatedDiary(null)
                        .createdAt(LocalDateTime.now().minusHours(2))
                        .completedAt(null)
                        .isCompleted(false)
                        .build()
        );
        
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/mock-emotion-analysis")
    @Operation(summary = "Mock 감정 분석", description = "테스트용 감정 분석 결과 데이터")
    public ResponseEntity<EmotionAnalysisDto> getMockEmotionAnalysis() {
        EmotionAnalysisDto analysis = EmotionAnalysisDto.builder()
                .primaryEmotion("스트레스")
                .emotionIntensity(7.5)
                .secondaryEmotions(Arrays.asList("피로", "답답함", "불안"))
                .emotionDescription("업무로 인한 복합적인 스트레스 상태, 감정적 에너지 소모가 큰 상태")
                .suggestedResponse("공감과 위로를 중심으로 한 따뜻한 응답, 구체적인 상황 탐색 필요")
                .build();
        
        return ResponseEntity.ok(analysis);
    }
}