package com.example.firebasepush.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("fcm")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @GetMapping(value = "/test")
    public ResponseEntity<MessageDto> pushTest(
        @RequestParam(value = "token") String token
    ) {
        apiService.sendOne(ApiDto.builder()
            .pushSeq(1L)
            .title("테스트 푸쉬 발송")
            .content("테스트 푸쉬 발송입니다.")
            .pushToken(token)
            .pushLink("/board/info")
            .build());

        return ResponseEntity.ok().body(new MessageDto("success", ""));
    }
}
