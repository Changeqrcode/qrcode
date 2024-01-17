package com.change.qrcode.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PackagesRepository;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
import com.change.qrcode.repository.UserRepository;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private QRRepository QRRepository;

    private PackagesRepository packagesRepository;

    private UserRepository userRepository;

    public PaymentController(QRRepository QRRepository,
            UserRepository userRepository,
            PackagesRepository packagesRepository) {
        this.QRRepository = QRRepository;
        this.userRepository = userRepository;
        this.packagesRepository = packagesRepository;
    }

    @PostMapping("/result")
    @ResponseBody
    public String result(HttpServletRequest httpServletRequest,
            @RequestParam("merchant_oid") String merchantOid,
            @RequestParam("status") String status,
            @RequestParam("total_amount") String totalAmount,
            @RequestParam("hash") String hash,
            Model model,
            RedirectAttributes redirectAttributes) throws ServletException {

        HttpSession session = httpServletRequest.getSession(false);

        List<Packages> packagesList = packagesRepository.findAll();

        var packageIdSaved = (Integer) session.getAttribute("packageId");
        var selectedPackage = packagesList.stream()
                .filter(p -> p.getId() == packageIdSaved)
                .findFirst().get();

        var freePackage = packagesList.stream()
                .filter(p -> p.getId() == 1L)
                .findFirst().get();

        String merchantKey = "pSP1odrTZQaZcP2j";
        String merchantSalt = "pM1gGwJxu97z5JK8";

        String combined = merchantOid + merchantSalt + status + totalAmount;
        String generatedHash = generateHmacSha256(combined, merchantKey);
        User u = userRepository.findByResetPasswordToken(merchantOid);

        if (!hash.equals(generatedHash)) {
            u.setPackageEndDate(null);
            u.setPackages(freePackage);
            return "PAYTR notification failed: bad hash";

        }

        if ("success".equals(status)) { // Ödeme Onaylandı

            u.setPackageEndDate(java.sql.Date.valueOf(LocalDate.now().plusYears(1)));
            u.setPackages(selectedPackage);
            userRepository.saveAndFlush(u);
            return "OK";

        } else { // Ödemeye Onay Verilmedi

            u.setPackageEndDate(null);
            u.setPackages(freePackage);
            return "NO";

        }

    }

    private String generateHmacSha256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hashBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash: " + e.getMessage());
        }
    }

}
