package com.change.qrcode.controller;

import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
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

    QRRepository QRRepository;

    UploadImageRepository uploadImageRepository;

    public UserController(QRRepository QRRepository, UploadImageRepository uploadImageRepository) {
        this.QRRepository = QRRepository;
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

    @PostMapping("/logout/{id}")
    public String logout(HttpServletRequest httpServletRequest, @PathVariable UUID id) throws ServletException {
        httpServletRequest.logout();
        return "redirect:/qr/" + id;
    }

    @GetMapping("/edit/qr/{id}")
    public String qrEdit(HttpServletRequest httpServletRequest, Model model, @PathVariable UUID id) throws ServletException {
        List<String> encodeds = new ArrayList<>();
        QR p = QRRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            httpServletRequest.logout();
            return "redirect:/user/edit/qr/" + id;
        }

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        return "user/edit/qr";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              @RequestParam("content") String text,
                              @RequestParam("images") MultipartFile[] files,
                              @PathVariable UUID id) throws IOException {
        QR p = QRRepository.findById(id).orElseThrow();
        p.setTextContent(text);
        p.setIsRecorded(true);

        uploadImageRepository.deleteAllByQRId(p.getId());

        for(MultipartFile f:files){
            if(f.getBytes().length > 0){
                UploadImage image = new UploadImage();
                image.setQR(p);
                image.setImageData(f.getBytes());
                uploadImageRepository.saveAndFlush(image);
            }

        }


        QRRepository.saveAndFlush(p);

        return "redirect:/qr/" + p.getId();
    }

}
