package com.change.qrcode.controller;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.repository.PackagesRepository;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/qr")
public class QRController {

    QRRepository QRRepository;
    UploadImageRepository uploadImageRepository;
    PackagesRepository packagesRepository;

    public QRController(QRRepository QRRepository, UploadImageRepository uploadImageRepository
            , PackagesRepository packagesRepository) {
        this.QRRepository = QRRepository;
        this.uploadImageRepository = uploadImageRepository;
        this.packagesRepository = packagesRepository;
    }

    @GetMapping("/{id}")
    public String get(Model model,
                      HttpServletRequest httpServletRequest,
                      @ModelAttribute("loginError") String loginError,
                      @PathVariable UUID id) throws ServletException {

        List<String> encodeds = new ArrayList<>();
        QR p = QRRepository.findById(id).orElseThrow();
        Packages pack = p.getPackages();

        List<Packages> packagesList = packagesRepository.findAll();
        Packages freePackage = packagesList.stream()
                .filter(fp -> fp.getPackageValue().equals(AdminController.FREE_PACKAGE_VALUE))
                .findFirst().get();

        Date today = java.sql.Date.valueOf(LocalDate.now());
        if(!pack.getPackageValue().equals(AdminController.FREE_PACKAGE_VALUE) && p.getPackageEndDate().before(today)){
            p.setPackageEndDate(null);
            p.setPackages(freePackage);
            p.setLinks(null);
            p.setTextContent(null);

            UploadImage logo = p.getLogo();
            p.setLogo(null);
            uploadImageRepository.deleteAllByQRId(p.getId());

            QRRepository.saveAndFlush(p);

            if(Objects.nonNull(logo)){
                uploadImageRepository.delete(logo);
            }
        }

        model.addAttribute("error", "");

        if(p.getIsRecorded() == null || p.getIsRecorded() == false){

            return "redirect:/registration/" + p.getId();
        }

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        if(p.getLinks() == null){
            p.setLinks("");
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        if (loginError != null && !loginError.isEmpty()){
            model.addAttribute("error", loginError);
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        model.addAttribute("links", p.getLinks());

        return "qr/qr";
    }

}
