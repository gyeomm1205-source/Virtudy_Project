package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.service.StudyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Log API", description = "학습 로그/이벤트 관련 API")
@RestController
@RequestMapping("/api/study/log") // AI 서버가 여기로 JSON을 보내야함: 형식은 StudyLogRequest
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;

    /**
     * 학습 중 발생한 이벤트를 로그로 저장합니다.
     * <p>
     * AI 모델이 감지한 졸음, 핸드폰 사용, 자리 비움 등의 이벤트를 서버로 전송할 때 사용합니다.
     * </p>
     *
     * @param request 학습 로그 요청 데이터 (세션ID, 이벤트타입, 감지시간)
     * @return 저장된 로그의 ID (Long)
     */
    @Operation(summary = "학습 로그 저장", description = "AI 모델 혹은 클라이언트로부터 학습 이벤트 로그를 수신합니다.")
    @PostMapping
    public ResponseEntity<Long> saveLog(@RequestBody StudyLogRequest request) {
        Long logId = studyLogService.saveLog(request);
        return ResponseEntity.ok(logId);
    }
}
