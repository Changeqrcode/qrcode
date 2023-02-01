package com.change.qrcode.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegistrationDto {
	private String username;
	private String password;
	private String registerUsername;
	private String registerPassword;
	private String checkPassword;

	public UserRegistrationDto(String username, String password, String registerUsername, String registerPassword, String checkPassword) {
		this.username = username;
		this.password = password;
		this.registerUsername = registerUsername;
		this.registerPassword = registerPassword;
		this.checkPassword = checkPassword;
	}
}
