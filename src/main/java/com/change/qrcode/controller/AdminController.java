package com.change.qrcode.controller;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PackagesRepository;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.RoleRepository;
import com.change.qrcode.repository.UploadImageRepository;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.util.QRCodeGenerator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    QRRepository QRRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UploadImageRepository uploadImageRepository;
    private AuthenticationProvider authenticationProvider;
    private RoleRepository roleRepository;
	private PackagesRepository packagesRepository;

    public AdminController(com.change.qrcode.repository.QRRepository QRRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider, RoleRepository roleRepository,UploadImageRepository uploadImageRepository, PackagesRepository packagesRepository) {
        this.QRRepository = QRRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.roleRepository = roleRepository;
        this.uploadImageRepository = uploadImageRepository;
        this.packagesRepository = packagesRepository;
    }

    @GetMapping("login")
    public String login(HttpServletRequest httpServletRequest) throws ServletException {
        return "admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest httpServletRequest) throws ServletException {
        httpServletRequest.logout();
        return "redirect:/admin/login";
    }

    @PostMapping("/login")
    public String loginPost(HttpServletRequest request,
                            @RequestParam("username") String username,
                            @RequestParam("password") String password) throws ServletException {

        User u = userRepository.findByUsername(username);
        if(u != null && passwordEncoder.matches(password, u.getPassword())) {

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate the user
            Authentication authentication = authenticationProvider.authenticate(authRequest);

            Object[] c = authentication.getAuthorities().toArray();


            if(authentication.getAuthorities().stream().anyMatch(item -> item.getAuthority().equals("ROLE_ADMIN"))){
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);
                // Create a new session and add the security context.
                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
                return "redirect:/admin/home";
            }else{
                return "redirect:/admin/login";
            }
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/home")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String home(Model model) {
        String url="Üretilme Bekleniyor...";
        List<String> qrCodeUrls = new ArrayList<>();
        qrCodeUrls.add(url);
        List<Packages> packagesList = packagesRepository.findAll();
        packagesList.sort(Comparator.comparing(Packages::getId));
        model.addAttribute("packages", packagesList);
        model.addAttribute("qrCodeUrls", qrCodeUrls);
        return "admin/home";
    }

    @PostMapping("/generateQRCode")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String generateMultipleQRCodes(@RequestParam(name = "qrCount") int qrCount,
                              @RequestParam("image") MultipartFile file, Model model) {
        List<String> qrCodeUrls = new ArrayList<>();
            UploadImage image = new UploadImage();
        try {
         
            image.setImageData(file.getBytes());
            image = uploadImageRepository.saveAndFlush(image);           
        } catch (IOException e) {
            e.printStackTrace();            
        }

        for (int i = 0; i < qrCount; i++) {
            String url = "http://changeqr.com/qr/";
            QR newQR = new QR();
            newQR.setIsRecorded(Boolean.FALSE);
            newQR.setTextContent("test");
            newQR.setUser(null);
            newQR.setLogo(image);
            try {
                QR savedQR = QRRepository.saveAndFlush(newQR);
                url += savedQR.getId();
                qrCodeUrls.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Packages> packagesList = packagesRepository.findAll();
        packagesList.sort(Comparator.comparing(Packages::getId));
        model.addAttribute("packages", packagesList);
        model.addAttribute("qrCodeUrls", qrCodeUrls);
        return "admin/home";
    }

    @PostMapping("/updatePackage")
    public String updatePackage(@ModelAttribute Packages updatedPackage) {
        Packages existingPackage = packagesRepository.findById(updatedPackage.getId()).orElse(null);

        if (existingPackage != null) {
            // Sadece güncellenmesini istediğiniz alanları güncelle
            existingPackage.setName(updatedPackage.getName());
            existingPackage.setCharacterLimit(updatedPackage.getCharacterLimit());
            existingPackage.setLinkLimit(updatedPackage.getLinkLimit());
            existingPackage.setImageLimit(updatedPackage.getImageLimit());
            existingPackage.setLogoAllowed(updatedPackage.getLogoAllowed() != null ? updatedPackage.getLogoAllowed() : false);
            existingPackage.setLocationAllowed(updatedPackage.getLocationAllowed() != null ? updatedPackage.getLocationAllowed() : false);    
            existingPackage.setPrice(updatedPackage.getPrice());

            packagesRepository.save(existingPackage);
        }

        // İlgili sayfaya yönlendirme, örneğin paket düzenleme sayfasına
        return "redirect:/admin/home";
    }
}
