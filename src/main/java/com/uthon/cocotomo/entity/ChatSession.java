package com.uthon.cocotomo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    private ChatStage currentStage;
    
    @Column(name = "emotion_analysis", columnDefinition = "TEXT")
    private String emotionAnalysis;
    
    @Column(name = "conversation_summary", columnDefinition = "TEXT")
    private String conversationSummary;
    
    @Column(name = "generated_diary", columnDefinition = "TEXT")
    private String generatedDiary;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")

    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ChatStage {
        INITIAL_EMOTION_ANALYSIS,    // 초기 감정 분석
        EMPATHY_RESPONSE,           // 공감 및 위로
        FOLLOW_UP_QUESTIONS,        // 후속 질문
        REFLECTION_SESSION,         // 회고 세션
        RITUAL_SESSION,             // 리추얼 세션 (문제점 및 방향성 제시)
        DIARY_GENERATION,           // 일기 생성
        COMPLETED                   // 완료
    }
}