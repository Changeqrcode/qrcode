package com.change.qrcode.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PackagesRepository;
import com.change.qrcode.repository.QRRepository;
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


        if ("success".equals(status)) { // Ödeme Onaylandı

            return "OK";

        } else { // Ödemeye Onay Verilmedi

            return "NO";

        }

    }

    @GetMapping("/savepackage/{id}")
    public String savePackage(HttpServletRequest httpServletRequest,
            Model model,
            RedirectAttributes redirectAttributes,
            @PathVariable UUID id) throws ServletException {


        List<Packages> packagesList = packagesRepository.findAll();

        QR p = QRRepository.findById(id).orElseThrow();
        User u = p.getUser();

        u.setPackageEndDate(java.sql.Date.valueOf(LocalDate.now().plusYears(1)));
        userRepository.saveAndFlush(u);
        return "payment/savepackage";

    }

    @GetMapping("/failpackage/{id}")
    public String failPackage(HttpServletRequest httpServletRequest,
            Model model,
            RedirectAttributes redirectAttributes,
            @PathVariable UUID id) throws ServletException {

        List<Packages> packagesList = packagesRepository.findAll();

        var freePackage = packagesList.stream()
                .filter(p -> p.getId() == 1L)
                .findFirst().get();


        QR p = QRRepository.findById(id).orElseThrow();
        User u = p.getUser();

        u.setPackageEndDate(null);
        u.setPackages(freePackage);
        userRepository.saveAndFlush(u);
        return "payment/failpackage";

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
