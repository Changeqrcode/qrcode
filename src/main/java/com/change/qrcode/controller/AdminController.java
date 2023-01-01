package com.change.qrcode.controller;

import com.change.qrcode.config.UserSecurityImpl;
import com.change.qrcode.model.Pet;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PetRepository;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.util.QRCodeGenerator;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Base64;

@Controller
public class AdminController {
    UserRepository userRepository;
    PetRepository petRepository;


    UserSecurityImpl state;

    @Autowired
    public AdminController(UserRepository userRepository, PetRepository petRepository, UserSecurityImpl state) {
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.state = state;
    }

    @GetMapping("/login")
    public String getAdminLogin(Model model){
        User u = new User();

        model.addAttribute("user", u);
        return "login";
    }

    @PostMapping("/login")
    public String checkAdmin(@ModelAttribute User u, Model model){
        User admin = userRepository.findAll().get(0);

        if(admin.getUsername().equals(u.getUsername()) && admin.getPassword().equals(u.getPassword())){
            String url = "";
            String qrcode = "./img/wait.png";
            model.addAttribute("url", url);
            model.addAttribute("qrcode", qrcode);
            state.setUsername(u.getUsername());
            state.setPassword(u.getPassword());
            return "redirect:/admin";
        }

        return "login";
    }

    @GetMapping("/admin")
    public String getAdminPanel(Model model){
        User admin = userRepository.findAll().get(0);

        if(admin.getUsername().equals(state.getUsername()) && admin.getPassword().equals(state.getPassword())){
            String url = "";
            String qrcode = "./img/wait.png";
            model.addAttribute("url", url);
            model.addAttribute("qrcode", qrcode);
            state.setPassword("");
            state.setUsername("");
            return "admin";
        }

        return "redirect:/login";
    }

    @PostMapping("/generateQRCode")
    public String generateQRCode(Model model){
        String url="http://localhost:8080/pet/";

        Pet newPet = new Pet();

        url += newPet.getId();

        byte[] image = new byte[0];
        try {
            Pet savedPet = petRepository.save(newPet);
            url += savedPet.getId();

            // Generate and Return Qr Code in Byte Array
            image = QRCodeGenerator.getQRCodeImage(url,250,250);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Convert Byte Array into Base64 Encode String
        String qrcode = Base64.getEncoder().encodeToString(image);
        qrcode = "data:image/png;base64," + qrcode;

        model.addAttribute("url",url);
        model.addAttribute("qrcode",qrcode);

        return "admin";
    }

}
