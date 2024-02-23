package com.change.qrcode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/login")
	public String login() {
		return "redirect:admin/login";
	}
	
	@GetMapping("/")
	public String home() {
		return "redirect:http://www.changeqrcode.com";
	}

	@GetMapping("/accessDenied")
	public String error403() {
		return "accessDenied";
	}

}
