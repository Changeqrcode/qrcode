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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String qrEdit(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable UUID id) throws ServletException {
        List<String> encodeds = new ArrayList<>();
        QR p = QRRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            httpServletRequest.logout();
            redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı bu qr kodun sahibi değildir.");
            return "redirect:/qr/" + id;
        }

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        if(p.getLinks() == null || p.getLinks().isEmpty()){
            p.setLinks("Bu bir deneme linkidir. Link eklediğinizde böyle gözükecektir. Okuduktan sonra bu linki siliniz.");
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        model.addAttribute("links", p.getLinks());

        return "user/edit/qr";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              HttpServletRequest httpServletRequest,
                              @RequestParam("content") String text,
                              @RequestParam("uploadLinks") String links,
                              @RequestParam("images") MultipartFile[] files,
                              @PathVariable UUID id) throws IOException, ServletException {
        QR p = QRRepository.findById(id).orElseThrow();
        p.setTextContent(text);
        p.setLinks(links);
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

        httpServletRequest.logout();

        return "redirect:/qr/" + p.getId();
    }

}
