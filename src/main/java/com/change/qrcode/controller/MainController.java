package com.change.qrcode.controller;

import com.change.qrcode.model.User;
import com.change.qrcode.util.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
	
	@GetMapping("/login")
	public String login() {
		return "redirect:user/login";
	}
	
	@GetMapping("/")
	public String home() {
		return "redirect:user/login";
	}

	@GetMapping("/deneme")
	@PreAuthorize("hasRole('ROLE_USER')")
	public String getDeneme(){
		User user = CurrentUser.getCurrentUser();
		return "deneme";
	}
}
