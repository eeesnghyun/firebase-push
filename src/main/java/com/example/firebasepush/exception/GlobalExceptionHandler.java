package com.example.firebasepush.exception;


import com.example.firebasepush.api.MessageDto;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PushException.class)
    protected ResponseEntity<MessageDto> globalCustomException(
        HttpServletRequest request,
        PushException e
    ) {
        log.error(e.getMessage());

        return ResponseEntity.internalServerError()
            .body(new MessageDto(e.getMessage()));
    }

}