package com.change.qrcode.controller;

import com.change.qrcode.model.Pet;
import com.change.qrcode.repository.PetRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;
import java.util.UUID;

@Controller
@RequestMapping("/pet")
public class PetController {

    PetRepository petRepository;

    public PetController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @GetMapping("/{id}")
    public String get(Model model, @PathVariable UUID id) {
        String encoded;
        Pet p = petRepository.findById(id).orElseThrow();

        if(p.getIsRecorded() == null || p.getIsRecorded() == false){
           // return "redirect:/user/edit/pet/" + p.getId();
            return "redirect:/registration/" + p.getId();
        }

        if(false/*p.getImageData() != null*/){
            encoded = Base64.getEncoder().encodeToString(null/*p.getImageData()*/);
            encoded = "data:image/png;base64," + encoded;
        }
        else {
            encoded = "../img/empty.png";
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("pet", p);
        model.addAttribute("image", encoded);

        return "pet/pet";
    }

}
