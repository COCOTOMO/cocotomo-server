package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.BaseResponse;
import com.uthon.cocotomo.dto.DiaryCommentRequest;
import com.uthon.cocotomo.service.DiaryCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diary-comments")
@RequiredArgsConstructor
@Tag(name = "일기 댓글", description = "일기 댓글 관련")
public class DiaryCommentController {
    private final DiaryCommentService service;

    @Operation(summary = "랜덤 일기 추천", description = "1주일 내 다른 사용자의 일기를 랜덤으로 추천합니다. 이미 댓글을 작성한 일기는 제외됩니다.")
    @GetMapping("/random")
    public ResponseEntity<?> getRandomDiary() {
        return ResponseEntity.ok(service.getRandomDiary());
    }

    @Operation(summary = "일기에 댓글 작성", description = "특정 일기에 댓글을 작성합니다. 자신의 일기에는 댓글을 달 수 없습니다.")
    @PostMapping("/{diaryId}")
    public ResponseEntity<BaseResponse> addComment(@PathVariable Long diaryId, @RequestBody DiaryCommentRequest request) {
        service.addComment(diaryId, request);
        return ResponseEntity.ok(BaseResponse.success("댓글이 작성되었습니다"));
    }

    @Operation(summary = "내 일기에 달린 댓글 조회", description = "내가 작성한 일기에 다른 사용자들이 달은 댓글들을 조회합니다.")
    @GetMapping("/my-diaries")
    public ResponseEntity<?> getCommentsForMyDiaries() {
        return ResponseEntity.ok(service.getCommentsForMyDiaries());
    }

    @Operation(summary = "내가 작성한 댓글 기록 조회", description = "내가 다른 사람의 일기에 작성한 댓글 기록을 조회합니다.")
    @GetMapping("/my-comments")
    public ResponseEntity<?> getMyComments() {
        return ResponseEntity.ok(service.getMyComments());
    }
}