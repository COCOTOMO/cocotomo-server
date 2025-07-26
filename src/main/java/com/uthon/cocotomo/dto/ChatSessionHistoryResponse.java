package com.uthon.cocotomo.dto;

import com.uthon.cocotomo.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionHistoryResponse {
    
    private String sessionId;
    private ChatSession.ChatStage currentStage;
    private String conversationSummary;
    private String generatedDiary;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private boolean isCompleted;
}