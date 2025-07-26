package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.*;
import com.uthon.cocotomo.service.EmotionChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emotion-chat")
@RequiredArgsConstructor
@Tag(name = "감정 회고 챗봇", description = "감정 회고 챗봇 관련 API")
public class EmotionChatController {
    
    private final EmotionChatService emotionChatService;
    
    @PostMapping("/sessions")
    @Operation(summary = "새로운 챗봇 세션 시작", description = "사용자의 첫 메시지와 함께 새로운 감정 회고 세션을 시작합니다.")
    public ResponseEntity<ChatMessageResponse> createSession(
            @RequestBody ChatSessionCreateRequest request) {
        
        ChatMessageResponse response = emotionChatService.createNewSession(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/messages")
    @Operation(summary = "챗봇 메시지 전송", description = "기존 세션에 사용자 메시지를 전송하고 AI 응답을 받습니다.")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestBody ChatMessageRequest request) {
        
        ChatMessageResponse response = emotionChatService.processUserMessage(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "세션 정보 조회", description = "특정 세션의 현재 상태와 정보를 조회합니다.")
    public ResponseEntity<ChatSessionHistoryResponse> getSession(
            @Parameter(description = "세션 ID") @PathVariable String sessionId) {
        
        ChatSessionHistoryResponse response = emotionChatService.getSessionInfo(sessionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/sessions/user/{userId}")
    @Operation(summary = "사용자 세션 목록 조회", description = "특정 사용자의 모든 세션 목록을 조회합니다.")
    public ResponseEntity<List<ChatSessionHistoryResponse>> getUserSessions(
            @Parameter(description = "사용자 ID") @PathVariable String userId) {
        
        List<ChatSessionHistoryResponse> response = emotionChatService.getUserSessionHistory(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "세션 강제 완료", description = "진행 중인 세션을 강제로 완료하고 일기를 생성합니다.")
    public ResponseEntity<ChatMessageResponse> completeSession(
            @Parameter(description = "세션 ID") @PathVariable String sessionId) {
        
        ChatMessageResponse response = emotionChatService.forceCompleteSession(sessionId);
        return ResponseEntity.ok(response);
    }
}