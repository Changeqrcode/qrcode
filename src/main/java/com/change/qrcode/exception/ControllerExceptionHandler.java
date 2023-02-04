package com.change.qrcode.exception;

import com.change.qrcode.model.ErrorMessage;
import com.change.qrcode.repository.ErrorMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class ControllerExceptionHandler {

    private String[] items ;

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    @ExceptionHandler(Exception.class)
    public String globalExceptionHandler(Exception ex, WebRequest request ){
        items = ex.getClass().getName().split("\\.");
        ErrorMessage message = new ErrorMessage(
                items[items.length-1].substring(0, 1).toLowerCase() + items[items.length-1].substring(1),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(true));

        errorMessageRepository.saveAndFlush(message);

        return "error";
    }
}