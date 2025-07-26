package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.BaseResponse;
import com.uthon.cocotomo.dto.TodoItemsRequest;
import com.uthon.cocotomo.dto.TodoRequest;
import com.uthon.cocotomo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
@Tag(name = "투두", description = "투두 리스트 관련 API")
public class TodoController {
    private final TodoService todoService;

    @Operation(summary = "할일 아이템 추가", description = "새로운 할일 아이템을 추가합니다.")
    @PostMapping("/new")
    public ResponseEntity<BaseResponse> add(@RequestBody TodoRequest request) {
        todoService.add(request);
        return ResponseEntity.ok(BaseResponse.success("할일 아이템 추가 성공"));
    }

    @Operation(summary = "할일 아이템 삭제", description = "특정 ID의 할일 아이템을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.ok(BaseResponse.success("할일 아이템 삭제 성공"));
    }

    @Operation(summary = "특정 날짜 할일 아이템 조회", description = "특정 날짜의 할일 아이템 목록을 조회합니다.")
    @GetMapping("/items")
    public ResponseEntity<?> getItems(@RequestBody TodoItemsRequest request) {
        return ResponseEntity.ok(todoService.getItems(request));
    }
}
