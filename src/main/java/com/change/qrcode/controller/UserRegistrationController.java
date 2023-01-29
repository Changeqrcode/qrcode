package com.change.qrcode.controller;

import com.change.qrcode.dto.UserRegistrationDto;
import com.change.qrcode.model.Pet;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PetRepository;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.security.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/registration")
public class UserRegistrationController {

	private UserService userService;
	private PetRepository petRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	public UserRegistrationController(UserService userService, PetRepository petRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.petRepository = petRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/{id}")
	public String showRegistrationForm(Model model, @PathVariable UUID id) {
		Pet p = petRepository.findById(id).orElseThrow();
		UserRegistrationDto userDto = new UserRegistrationDto("","","", false);
		String anyError = null;

		if(p.getIsRecorded() == null || p.getIsRecorded() == false){
			model.addAttribute("pet", p);
			model.addAttribute("userDto", userDto);
			model.addAttribute("anyError", anyError);
			return "registration";
		}else {
			return "redirect:/pet/" + p.getId();
		}

	}
	
	@PostMapping("/{id}")
	public String registerUserAccount(Model model,
									  @ModelAttribute("userDto") UserRegistrationDto userRegistrationDto,
									  @PathVariable UUID id) {

		Boolean isExistAccount = userRegistrationDto.getIsExistAccount();
		Pet entity = petRepository.findById(id).orElseThrow();
		model.addAttribute("anyError", null);

		if(isExistAccount){
			User u = userRepository.findByUsername(userRegistrationDto.getUsername());
			if(u != null && passwordEncoder.matches(userRegistrationDto.getPassword(), u.getPassword())){
				entity.setUser(u);
				entity.setIsRecorded(Boolean.TRUE);
				petRepository.save(entity);
				return "redirect:/pet/" + id;
			}else {
				model.addAttribute("anyError", "Girilen kullanıcı adı veya şifre hatalı.");
				model.addAttribute("pet", entity);
				return "registration";
			}

		}else {
			if(userRepository.findByUsername(userRegistrationDto.getUsername()) != null){
				model.addAttribute("anyError", "Girilen kullanıcı ismi zaten mevcut.\n" +
						"Başka bir kullanıcı ismi giriniz.\n" +
						" Var olan bir hesabınızı bağlamak istiyorsanız seçeneği işaretlemeyi unutmayınız!!");
				model.addAttribute("pet", entity);
				return "registration";
			}else{
				if(!userRegistrationDto.getPassword().equals(userRegistrationDto.getCheckPassword())){
					model.addAttribute("anyError", "Girilen şifreler birbiriyle uyuşmamaktadır.");
					model.addAttribute("pet", entity);
					return "registration";
				}

				User u = userService.save(userRegistrationDto);
				entity.setUser(u);
				entity.setIsRecorded(Boolean.TRUE);
				petRepository.save(entity);
				return "redirect:/pet/" + id;
			}

		}
	}
}
