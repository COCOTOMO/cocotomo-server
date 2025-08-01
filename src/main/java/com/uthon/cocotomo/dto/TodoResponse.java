package com.uthon.cocotomo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodoResponse {
    private Long id;
    private String content;
    private boolean completed;
}
