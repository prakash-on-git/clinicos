package com.prakash.clinicos.audit.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.prakash.clinicos.audit.entity.AuditAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {

    private Long id;
    private Long clinicId;
    private String entityType;
    private Long entityId;
    private AuditAction action;
    private Long changedBy;

    /**
     * @JsonRawValue tells Jackson to embed the stored JSON string as a nested
     * JSON object rather than escaping it as a string-within-a-string.
     * This means the API response contains a proper JSON object, not a
     * double-escaped string.
     */
    @JsonRawValue
    private String beforeState;

    @JsonRawValue
    private String afterState;

    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
