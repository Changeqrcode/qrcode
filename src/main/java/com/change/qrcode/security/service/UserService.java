package com.change.qrcode.security.service;

import com.change.qrcode.dto.UserRegistrationDto;
import com.change.qrcode.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService{
	User save(UserRegistrationDto registrationDto);
}
