package com.uthon.cocotomo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionCreateRequest {
    
    private String userId;
    private String initialMessage;
}