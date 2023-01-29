package com.change.qrcode.controller;

import com.change.qrcode.model.Pet;
import com.change.qrcode.repository.PetRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    PetRepository petRepository;

    public UserController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/home")
    public String home() {
        return "user/home";
    }

    @GetMapping("/edit/pet/{id}")
    public String petEdit(HttpServletRequest httpServletRequest, Model model, @PathVariable UUID id) throws ServletException {
        String encoded;
        Pet p = petRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            httpServletRequest.logout();
            return "redirect:/user/edit/pet/" + id;
        }

        if(p.getImageData() != null){
            encoded = Base64.getEncoder().encodeToString(p.getImageData());
            encoded = "data:image/png;base64," + encoded;
        }
        else {
            encoded = ".../img/empty.png";
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("pet", p);
        model.addAttribute("image", encoded);
        return "user/edit/pet";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              @RequestParam("content") String text,
                              @RequestParam("image") MultipartFile file,
                              @PathVariable UUID id) throws IOException {
        Pet p = petRepository.findById(id).orElseThrow();
        p.setTextContent(text);
        p.setImageData(file.getBytes());
        p.setIsRecorded(true);

        petRepository.save(p);

        return "redirect:/pet/" + p.getId();
    }

}
