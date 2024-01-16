package com.change.qrcode.controller;

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
		return "OK";
	}
	

}
