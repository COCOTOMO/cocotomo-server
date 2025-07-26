package com.uthon.cocotomo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisDto {
    
    private String primaryEmotion;
    private Double emotionIntensity;
    private List<String> secondaryEmotions;
    private String emotionDescription;
    private String suggestedResponse;
}