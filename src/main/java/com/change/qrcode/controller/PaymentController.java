package com.change.qrcode.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/result")
    @ResponseBody
    public String  result(HttpServletRequest httpServletRequest,
                                    @RequestParam("merchant_oid") String merchantOid,
                                    @RequestParam("status") String status,
                                    @RequestParam("total_amount") String totalAmount,
                                    @RequestParam("hash") String hash,
                                    Model model,
                                    RedirectAttributes redirectAttributes) throws ServletException  {

		String merchantKey = "pSP1odrTZQaZcP2j";
		String merchantSalt = "pM1gGwJxu97z5JK8";

		String combined = merchantOid + merchantSalt + status + totalAmount;
		String generatedHash = generateHmacSha256(combined, merchantKey);

		  if (!hash.equals(generatedHash)) {
                return "PAYTR notification failed: bad hash";
			}

		if ("success".equals(status)) { // Ödeme Onaylandı

			return "OK";

		} else { // Ödemeye Onay Verilmedi
	
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
