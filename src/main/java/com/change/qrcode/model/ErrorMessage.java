package com.change.qrcode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "error_log")
@NoArgsConstructor
@Getter
public class ErrorMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageId;

    private int statusCode;

    private Date timestamp;

    private String message;

    private String description;

    private String stackTrace;

    private String localizedMessage;

    private String cause;

    public ErrorMessage(String messageId,
                        int statusCode,
                        Date timestamp,
                        String message,
                        String description,
                        String stackTrace,
                        String localizedMessage,
                        String cause) {
        this.messageId = messageId;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
        this.stackTrace = stackTrace;
        this.localizedMessage = localizedMessage;
        this.cause = cause;
    }
}