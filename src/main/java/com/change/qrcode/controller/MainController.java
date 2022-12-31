package com.change.qrcode.controller;

import com.change.qrcode.Employee;
import com.change.qrcode.util.QRCodeGenerator;
import com.google.zxing.WriterException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class MainController {

    private static final String QR_CODE_IMAGE_PATH = "./src/main/resources/static/img/QRCode.png";

    @GetMapping("/")
    public String getQRCode(Model model){
        String medium="https://changeQR.com/5235f795-ab5a-49bc-9321-03f6d87d07ce";
        String github="https://github.com/rahul26021999";

        byte[] image = new byte[0];
        try {

            // Generate and Return Qr Code in Byte Array
            image = QRCodeGenerator.getQRCodeImage(medium,250,250);

            // Generate and Save Qr Code Image in static/image folder
            QRCodeGenerator.generateQRCodeImage(github,250,250,QR_CODE_IMAGE_PATH);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
        // Convert Byte Array into Base64 Encode String
        String qrcode = Base64.getEncoder().encodeToString(image);

        model.addAttribute("medium",medium);
        model.addAttribute("github",github);
        model.addAttribute("qrcode",qrcode);

        return "qrcode";
    }

    @GetMapping("/deneme")
    public String getDeneme(Model model){
        List<Employee> employees = new ArrayList<>();

        for (int i = 0; i<10; i++){
            employees.add(new Employee("first name " + i, "last name " + i, "email " + i));
        }
        model.addAttribute("employees",employees);

        return "deneme";
    }

}
