package com.uthon.cocotomo.exception;

import com.uthon.cocotomo.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.error("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.notFound(e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<BaseResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("Username not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.notFound("사용자를 찾을 수 없습니다"));
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    public ResponseEntity<BaseResponse> handleDiaryNotFoundException(DiaryNotFoundException e) {
        log.error("Diary not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.notFound(e.getMessage()));
    }

    @ExceptionHandler(CommentNotAllowedException.class)
    public ResponseEntity<BaseResponse> handleCommentNotAllowedException(CommentNotAllowedException e) {
        log.error("Comment not allowed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.forbidden(e.getMessage()));
    }

    @ExceptionHandler(DuplicateCommentException.class)
    public ResponseEntity<BaseResponse> handleDuplicateCommentException(DuplicateCommentException e) {
        log.error("Duplicate comment: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseResponse.conflict(e.getMessage()));
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<BaseResponse> handleEmailSendException(EmailSendException e) {
        log.error("Email send failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.internalServerError(e.getMessage()));
    }

    @ExceptionHandler(NoRecommendedDiaryException.class)
    public ResponseEntity<BaseResponse> handleNoRecommendedDiaryException(NoRecommendedDiaryException e) {
        log.info("No recommended diary: {}", e.getMessage());
        return ResponseEntity.ok(BaseResponse.success(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.internalServerError("서버 오류가 발생했습니다"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.internalServerError("예상치 못한 오류가 발생했습니다"));
    }
}