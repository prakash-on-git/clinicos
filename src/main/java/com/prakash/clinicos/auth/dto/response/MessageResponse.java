package com.prakash.clinicos.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Generic success message for operations that don't return data. */
@Getter
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
