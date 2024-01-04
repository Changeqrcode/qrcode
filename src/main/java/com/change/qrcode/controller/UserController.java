package com.change.qrcode.controller;

import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
import com.change.qrcode.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private QRRepository QRRepository;

    private UploadImageRepository uploadImageRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private AuthenticationProvider authenticationProvider;

    private UserRepository userRepository;

    public UserController(QRRepository QRRepository,
                          UploadImageRepository uploadImageRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationProvider authenticationProvider,
                          UserRepository userRepository) {
        this.QRRepository = QRRepository;
        this.uploadImageRepository = uploadImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.userRepository = userRepository;
    }

    @GetMapping("/login/qr/{id}")
    public String login(HttpServletRequest httpServletRequest,
                        @PathVariable UUID id,
                        Model model) throws ServletException{
        model.addAttribute("id", id);
        return "user/login";
    }


    @PostMapping("/login/qr/{id}")
    public String loginPost(HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            @PathVariable UUID id,
                            @RequestParam("username") String username,
                            @RequestParam("password") String password) throws ServletException{

        QR p = QRRepository.findById(id).orElseThrow();

        User u = userRepository.findByUsername(username);
        if(u != null && passwordEncoder.matches(password, u.getPassword())){

            if(!p.getUser().getUsername().equals(username)){

                redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı adı veya şifre hatalı.");
                return "redirect:/qr/" + id;
            }

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate the user
            Authentication authentication = authenticationProvider.authenticate(authRequest);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Create a new session and add the security context.
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            return "redirect:/user/edit/qr/"+id;
        } else{

            redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı bu qr kodun sahibi değildir.");
            return "redirect:/qr/" + id;
        }
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
        String encodedLogo = "";
        QR p = QRRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı bu qr kodun sahibi değildir.");
            return "redirect:/qr/" + id;
        }

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        UploadImage logo = uploadImageRepository.findById(p.getLogo().getId()).get();

        if(logo != null ){
            encodedLogo= "data:image/png;base64," + Base64.getEncoder().encodeToString(logo.getImageData());          
        }

        if(p.getLinks() == null || p.getLinks().isEmpty()){
            p.setLinks("Deneme Linki - Sample Link");
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        model.addAttribute("links", p.getLinks());
        model.addAttribute("logo", encodedLogo);

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
