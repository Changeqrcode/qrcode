package com.change.qrcode.controller;

import com.change.qrcode.model.Pet;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.repository.PetRepository;
import com.change.qrcode.repository.UploadImageRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    PetRepository petRepository;

    UploadImageRepository uploadImageRepository;

    public UserController(PetRepository petRepository, UploadImageRepository uploadImageRepository) {
        this.petRepository = petRepository;
        this.uploadImageRepository = uploadImageRepository;
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
        List<String> encodeds = new ArrayList<>();
        Pet p = petRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            httpServletRequest.logout();
            return "redirect:/user/edit/pet/" + id;
        }

        if(p.getImages() != null && p.getImages().size() > 0){
            for (UploadImage u:p.getImages()){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }
        else {
            encodeds.add(".../img/empty.png");
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("pet", p);
        model.addAttribute("images", encodeds);
        return "user/edit/pet";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              @RequestParam("content") String text,
                              @RequestParam("images") MultipartFile[] files,
                              @PathVariable UUID id) throws IOException {
        Pet p = petRepository.findById(id).orElseThrow();
        p.setTextContent(text);
        p.setIsRecorded(true);

        for(MultipartFile f:files){
            UploadImage image = new UploadImage();
            image.setPet(p);
            image.setImageData(f.getBytes());
            uploadImageRepository.save(image);
        }


        petRepository.save(p);

        return "redirect:/pet/" + p.getId();
    }

}
