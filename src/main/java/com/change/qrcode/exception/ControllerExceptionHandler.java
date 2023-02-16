package com.change.qrcode.exception;

import com.change.qrcode.model.ErrorMessage;
import com.change.qrcode.repository.ErrorMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@ControllerAdvice
public class ControllerExceptionHandler {

    private String[] items ;

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    @ExceptionHandler(Exception.class)
    public ModelAndView globalExceptionHandler(Exception ex, WebRequest request ){
        items = ex.getClass().getName().split("\\.");
        ModelAndView m = new ModelAndView();

        if(ex instanceof MethodArgumentTypeMismatchException){
            ErrorMessage message = new ErrorMessage(items[items.length-1].substring(0, 1).toLowerCase() + items[items.length-1].substring(1),
                                                    HttpStatus.NO_CONTENT.value(),
                                                    new Date(),
                                            "Gitmeye çalıştığınız link bozuk olabilir veya böyle bir link olmayabilir.",
                                                    request.getDescription(true),
                                                    ex.getStackTrace().toString(),
                                                    ex.getLocalizedMessage(),
                                                    ex.getCause().toString());
            m.setViewName("error");
            m.addObject("errorMessage", message);
        }else{
            ErrorMessage message = new ErrorMessage(
                    items[items.length-1].substring(0, 1).toLowerCase() + items[items.length-1].substring(1),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    new Date(),
                    ex.getMessage(),
                    request.getDescription(true),
                    ex.getStackTrace().toString(),
                    ex.getLocalizedMessage(),
                    ex.getCause().toString());

            errorMessageRepository.saveAndFlush(message);
            m.setViewName("error");
            m.addObject("errorMessage", message);
        }

        return m;
    }
}