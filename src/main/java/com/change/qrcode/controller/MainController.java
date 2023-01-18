package com.change.qrcode.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/")
	public String home() {
		return "login";
	}

	@GetMapping("/deneme")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String getDeneme(){
		return "deneme";
	}
}
