package com.change.qrcode.controller;

import com.change.qrcode.Employee;
import com.change.qrcode.model.Pet;
import com.change.qrcode.repository.PetRepository;
import com.change.qrcode.util.QRCodeGenerator;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class MainController {

    PetRepository petRepository;

    @Autowired
    public MainController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @GetMapping("/pet/{id}")
    public String getPetById(Model model, @PathVariable Long id){
        String encoded;
        Pet p = petRepository.findById(id).orElseThrow();
        if(p.getImageData() != null){
            encoded = Base64.getEncoder().encodeToString(p.getImageData());
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
        return "pet";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              @RequestParam("content") String text,
                              @RequestParam("image") MultipartFile file,
                              @PathVariable Long id) throws IOException {
        Pet p = petRepository.findById(id).orElseThrow();
        p.setTextContent(text);
        p.setImageData(file.getBytes());
        p.setIsRecorded(true);

        petRepository.save(p);

        return "redirect:/pet/" + p.getId();
    }


}
