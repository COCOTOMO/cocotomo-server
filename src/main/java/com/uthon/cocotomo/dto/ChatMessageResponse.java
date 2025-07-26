package com.uthon.cocotomo.dto;

import com.uthon.cocotomo.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    
    private String sessionId;
    private String aiResponse;
    private ChatSession.ChatStage currentStage;
    private String emotionAnalysis;
    private boolean sessionCompleted;
    private String generatedDiary;
}