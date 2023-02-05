package com.change.qrcode.controller;

import com.change.qrcode.dto.UserRegistrationDto;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.security.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/registration")
public class UserRegistrationController {

	private UserService userService;
	private QRRepository QRRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	private AuthenticationProvider authenticationProvider;

	public UserRegistrationController(UserService userService, com.change.qrcode.repository.QRRepository QRRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider) {
		this.userService = userService;
		this.QRRepository = QRRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationProvider = authenticationProvider;
	}

	@GetMapping("/{id}")
	public String showRegistrationForm(Model model, @PathVariable UUID id) {
		QR p = QRRepository.findById(id).orElseThrow();
		UserRegistrationDto userDto = new UserRegistrationDto("","","", "","");
		String anyError = null;

		if(p.getIsRecorded() == null || p.getIsRecorded() == false){
			model.addAttribute("qr", p);
			model.addAttribute("userDto", userDto);
			model.addAttribute("anyError", anyError);
			return "registration";
		}else {
			return "redirect:/qr/" + p.getId();
		}

	}
	
	@PostMapping("/{id}")
	public String registerUserAccount(HttpServletRequest request,
									  Model model,
									  @ModelAttribute("userDto") UserRegistrationDto userRegistrationDto,
									  @PathVariable UUID id) {

		Boolean isExistAccount;
		QR entity = QRRepository.findById(id).orElseThrow();
		model.addAttribute("anyError", null);

		User u = userRepository.findByUsername(userRegistrationDto.getUsername());
		if(u != null && passwordEncoder.matches(userRegistrationDto.getPassword(), u.getPassword())){
			entity.setUser(u);
			entity.setIsRecorded(Boolean.TRUE);
			QRRepository.saveAndFlush(entity);

			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userRegistrationDto.getUsername(), userRegistrationDto.getPassword());

			// Authenticate the user
			Authentication authentication = authenticationProvider.authenticate(authRequest);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);

			// Create a new session and add the security context.
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			return "redirect:/user/edit/qr/" + id;
		}else {
			model.addAttribute("anyError", "Girilen kullanıcı adı veya şifre hatalı.");
			model.addAttribute("qr", entity);
			return "registration";
		}

	}

	@PostMapping("new/{id}")
	public String registerNewUserAccount(HttpServletRequest request,
									  Model model,
									  @ModelAttribute("userDto") UserRegistrationDto userRegistrationDto,
									  @PathVariable UUID id) {

		QR entity = QRRepository.findById(id).orElseThrow();
		model.addAttribute("anyError", null);
		if(userRepository.findByUsername(userRegistrationDto.getRegisterUsername()) != null){
			model.addAttribute("anyError", "Girilen kullanıcı ismi zaten mevcut.\n" +
					"Başka bir kullanıcı ismi giriniz.\n" +
					" Var olan bir hesabınızı bağlamak istiyorsanız seçeneği işaretlemeyi unutmayınız!!");
			model.addAttribute("qr", entity);
			return "registration";
		}else{
			if(!userRegistrationDto.getRegisterPassword().equals(userRegistrationDto.getCheckPassword())){
				model.addAttribute("anyError", "Girilen şifreler birbiriyle uyuşmamaktadır.");
				model.addAttribute("qr", entity);
				return "registration";
			}

			User u = userService.save(userRegistrationDto);
			entity.setUser(u);
			entity.setIsRecorded(Boolean.TRUE);
			QRRepository.saveAndFlush(entity);

			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userRegistrationDto.getRegisterUsername(), userRegistrationDto.getRegisterPassword());

			// Authenticate the user
			Authentication authentication = authenticationProvider.authenticate(authRequest);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);

			// Create a new session and add the security context.
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			return "redirect:/user/edit/qr/" + id;
		}


	}
}
