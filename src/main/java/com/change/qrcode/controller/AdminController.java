package com.change.qrcode.controller;

import com.change.qrcode.model.QR;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.util.QRCodeGenerator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;

@Controller
@RequestMapping("/admin")
public class AdminController {
    QRRepository QRRepository;

    public AdminController(QRRepository QRRepository) {
        this.QRRepository = QRRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/home")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String home(Model model) {
        String url="Üretilme Bekleniyor...";
        String qrcode = "../img/wait.png";

        model.addAttribute("url", url);
        model.addAttribute("qrcode", qrcode);

        return "admin/home";
    }

    @PostMapping("/generateQRCode")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String generateQRCode(Model model){
        String url="http://localhost:8080/qr/";

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
