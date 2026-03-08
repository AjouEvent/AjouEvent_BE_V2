package com.example.ajouevent_be_v2.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResponseDto {

    private HttpStatus successStatus;
    private String successContent;
    private Object data;
}
