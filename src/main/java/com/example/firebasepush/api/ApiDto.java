package com.example.firebasepush.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiDto {

    private Long pushSeq;
    private String pushToken;
    private String title;
    private String content;
    private String pushLink;

}
