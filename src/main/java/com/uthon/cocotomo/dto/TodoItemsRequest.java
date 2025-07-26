package com.study.practice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TodoItemsRequest {
    private LocalDate date;
}
