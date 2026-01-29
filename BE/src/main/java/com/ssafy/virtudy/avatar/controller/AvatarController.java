package com.ssafy.virtudy.avatar.controller;

import com.ssafy.virtudy.avatar.dto.AvatarDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    // 아바타 생성 api
    /**
     * 이미지를 받아야 함.
     */
    @Operation(summary = "아바타 생성", description = "이미지를 받으면 분석하여 해당하는 라벨에 맞게 아바타 값을 json 형태로 반환합니다.")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AvatarDto.Response createAvatar(
            @Parameter(description = "업로드할 이미지 파일")
            @RequestPart(value = "image") MultipartFile image
    )
    {
            if (image.isEmpty()) {
                throw new IllegalArgumentException("이미지가 없습니다.");
            }

            // TODO : 이미지 분석 및 아바타 생성 로직 연결

        return new AvatarDto.Response();
    }
}
