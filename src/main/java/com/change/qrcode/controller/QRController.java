package com.change.qrcode.controller;

import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/qr")
public class QRController {

    QRRepository QRRepository;
    UploadImageRepository uploadImageRepository;

    public QRController(QRRepository QRRepository, UploadImageRepository uploadImageRepository) {
        this.QRRepository = QRRepository;
        this.uploadImageRepository = uploadImageRepository;
    }

    @GetMapping("/{id}")
    public String get(Model model, @PathVariable UUID id) {
        List<String> encodeds = new ArrayList<>();
        QR p = QRRepository.findById(id).orElseThrow();

        if(p.getIsRecorded() == null || p.getIsRecorded() == false){

            return "redirect:/registration/" + p.getId();
        }

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);

        return "qr/qr";
    }

}
