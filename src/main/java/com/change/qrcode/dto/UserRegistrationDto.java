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
	private String checkPassword;

	private Boolean isExistAccount;

	public UserRegistrationDto(String username, String password, String checkPassword, Boolean isExistAccount) {
		this.username = username;
		this.password = password;
		this.checkPassword = checkPassword;
		this.isExistAccount = isExistAccount;
	}
}
