package com.example.skripsi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebResponse<T> {
    private boolean success;
    private String message;
    private T result;
    private T meta;
}
