package com.study.practice.controller;

import com.study.practice.dto.TodoItemsRequest;
import com.study.practice.dto.TodoRequest;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.study.practice.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
@Tag(name = "투두", description = "투두 리스트 관련 API")
public class TodoController {
    private final TodoService todoService;

    @PostMapping("/new")
    public ResponseEntity<?> add(@RequestBody TodoRequest request) {
        todoService.add(request);
        return ResponseEntity.ok("추가 성공");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.ok("삭제 성공");
    }

    @GetMapping("/items")
    public ResponseEntity<?> getItems(@RequestBody TodoItemsRequest request) {
        return ResponseEntity.ok(todoService.getItems(request));
    }
}
