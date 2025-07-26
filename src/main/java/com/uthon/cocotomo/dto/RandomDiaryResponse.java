package com.uthon.cocotomo.dto;

import lombok.Data;

@Data
public class RandomDiaryResponse {
    private Long id;
    private String content;
    private String date;
}