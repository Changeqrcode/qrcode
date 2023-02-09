package com.change.qrcode.controller;

import com.change.qrcode.dto.UserRegistrationDto;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.RoleRepository;
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
import java.util.*;

@Controller
@RequestMapping("/registration")
public class UserRegistrationController {

	private UserService userService;
	private QRRepository QRRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	private AuthenticationProvider authenticationProvider;
	private RoleRepository roleRepository;

	public UserRegistrationController(UserService userService, com.change.qrcode.repository.QRRepository QRRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider, RoleRepository roleRepository) {
		this.userService = userService;
		this.QRRepository = QRRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationProvider = authenticationProvider;
		this.roleRepository = roleRepository;
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

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> registerUser(HttpServletRequest request,
													 @RequestParam("username") String username,
													 @RequestParam("password") String password,
													 @RequestParam("checkPassword") String checkPassword,
													 @RequestParam("id") String id) {
		Map<String, Object> data = new HashMap<>();

		QR entity = QRRepository.findById(UUID.fromString(id)).orElseThrow();
		data.put("isError", false);
		if(userRepository.findByUsername(username) != null){
			data.put("isError", true);
			data.put("errorMessage", "Girilen kullanıcı ismi zaten mevcut." +
					" Başka bir kullanıcı ismi giriniz." +
					" Var olan bir hesabınızı bağlamak istiyorsanız normal giriş yapınız!!");
			return data;
		}else{
			if(!password.equals(checkPassword)){
				data.put("isError", true);
				data.put("errorMessage", "Girilen şifreler birbiriyle uyuşmamaktadır.");
				return data;
			}

			User newUser = new User();
			newUser.setUsername(username);
			newUser.setPassword(passwordEncoder.encode(password));
			newUser.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));
			User u = userService.save(newUser);

			entity.setUser(u);
			entity.setIsRecorded(Boolean.TRUE);
			QRRepository.saveAndFlush(entity);

			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

			// Authenticate the user
			Authentication authentication = authenticationProvider.authenticate(authRequest);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);

			// Create a new session and add the security context.
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
			return data;
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> loginUser(HttpServletRequest request,
													 @RequestParam("username") String username,
													 @RequestParam("password") String password,
													 @RequestParam("id") String id) {
		Map<String, Object> data = new HashMap<>();

		QR entity = QRRepository.findById(UUID.fromString(id)).orElseThrow();
		data.put("isError", false);

		User u = userRepository.findByUsername(username);
		if(u != null && passwordEncoder.matches(password, u.getPassword())){
			entity.setUser(u);
			entity.setIsRecorded(Boolean.TRUE);
			QRRepository.saveAndFlush(entity);

			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

			// Authenticate the user
			Authentication authentication = authenticationProvider.authenticate(authRequest);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);

			// Create a new session and add the security context.
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			return data;
		}else {
			data.put("isError", true);
			data.put("errorMessage", "Girilen kullanıcı adı veya şifre hatalı.");
			return data;
		}
	}
}
