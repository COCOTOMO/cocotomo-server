package com.uthon.cocotomo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiaryCommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String commenterEmail;
    private Long diaryId;
    private String diaryDate;
}