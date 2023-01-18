package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
	private String username;
	private String password;
	
	public UserRegistrationDto(){
		
	}
	
	public UserRegistrationDto(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
}
