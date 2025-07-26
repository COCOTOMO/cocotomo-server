package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.AddDiaryRequest;
import com.uthon.cocotomo.dto.BaseResponse;
import com.uthon.cocotomo.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "일기 작성", description = "새로운 일기를 작성합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse> save(@RequestBody AddDiaryRequest req) {
        service.save(req);
        return ResponseEntity.ok(BaseResponse.success("일기 저장 성공"));
    }

    @Operation(summary = "월별 일기 조회", description = "특정 월의 일기 목록을 조회합니다. date 형식: YYYY-MM")
    @PostMapping("/month")
    public ResponseEntity<?> getByMonth(@RequestParam String date) {
        return ResponseEntity.ok(service.getByMonth(date));
    }

    @Operation(summary = "일기 수정", description = "특정 날짜의 일기를 수정합니다. date 형식: YYYY-MM-DD")
    @PatchMapping("/{date}")
    public ResponseEntity<BaseResponse> update(@PathVariable String date, @RequestBody AddDiaryRequest req) {
        service.update(date, req);
        return ResponseEntity.ok(BaseResponse.success("일기 수정 성공"));
    }

    @Operation(summary = "특정 날짜 일기 조회", description = "특정 날짜의 일기를 조회합니다. date 형식: YYYY-MM-DD")
    @GetMapping("/{date}")
    public ResponseEntity<?> getById(@PathVariable String date) {
        return ResponseEntity.ok(service.getByDate(date));
    }
}
