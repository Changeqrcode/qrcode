package com.change.qrcode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

@Component
@SessionScope
@Getter
@Setter
public class UserSecurityImpl implements Serializable {

    private String username;
    private String password;
}
