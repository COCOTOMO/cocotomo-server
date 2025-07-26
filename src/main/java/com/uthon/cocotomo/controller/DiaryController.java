package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.AddDiaryRequest;
import com.uthon.cocotomo.dto.TodoItemsRequest;
import com.uthon.cocotomo.service.DiaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
@Tag(name = "일기", description = "일기 관련")
public class DiaryController {
    private final DiaryService service;

    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody AddDiaryRequest req) {
        service.save(req);
        return ResponseEntity.ok("일기 저장 성공");
    }

    @PostMapping("/month")
    public ResponseEntity<?> getByMonth(@RequestParam String date) {
        return ResponseEntity.ok(service.getByMonth(date));
    }
}
