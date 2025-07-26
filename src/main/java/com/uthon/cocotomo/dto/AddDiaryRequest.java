package com.uthon.cocotomo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddDiaryRequest {
    private String content;
    private LocalDate date;
}
