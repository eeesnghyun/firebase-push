package com.example.firebasepush.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {

    private String message;
    private Object result;

    public MessageDto(String message) {
        this.message = message;
    }

    public MessageDto(String message, Object result) {
        this.message = message;
        this.result = result;
    }
}
