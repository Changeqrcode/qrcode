package com.change.qrcode.controller;

import com.change.qrcode.model.QR;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.RoleRepository;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.util.QRCodeGenerator;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    QRRepository QRRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    private AuthenticationProvider authenticationProvider;
    private RoleRepository roleRepository;

    public AdminController(com.change.qrcode.repository.QRRepository QRRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider, RoleRepository roleRepository) {
        this.QRRepository = QRRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.roleRepository = roleRepository;
    }
    @GetMapping("/login")
    public String getRedirectPage(HttpServletRequest httpServletRequest) throws ServletException {
        return "admin/redirect";
    }
    @GetMapping("")
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
        String qrcode = "/img/wait.png";

        model.addAttribute("url", url);
        model.addAttribute("qrcode", qrcode);

        return "admin/home";
    }

    @PostMapping("/generateQRCode")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String generateQRCode(Model model){
        String url="http://changeqr.com/qr/";

        QR newQR = new QR();

        newQR.setIsRecorded(Boolean.FALSE);
        newQR.setTextContent("");
        newQR.setUser(null);

        byte[] image = new byte[0];
        try {
            QR savedQR = QRRepository.saveAndFlush(newQR);
            url += savedQR.getId();

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

        return "admin/home";
    }
}
