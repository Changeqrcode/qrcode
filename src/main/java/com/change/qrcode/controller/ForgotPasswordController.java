package com.change.qrcode.controller;

import com.change.qrcode.dto.UserRegistrationDto;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.repository.business.concretes.UserManager;
import com.change.qrcode.util.EmailSenderService;
import com.change.qrcode.util.Utilities;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/registration/password")
public class ForgotPasswordController {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private UserManager userManager;

    private UserRepository userRepository;

    public ForgotPasswordController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



 /* Forgot Password */

    @GetMapping("/forgot")
    public String showForgotPasswordForm(Model model) {


        return "user/forgot-password";
    }

@PostMapping("/send-mail")
public  @ResponseBody Map<String, Object>  processForgotPasswordForm(HttpServletRequest request, @RequestParam("email") String email) {

    Map<String, Object> data = new HashMap<>();
    //sendMail();
    String token = RandomString.make(30);
    data.put("isError",false);
    try {
        userManager.UpdateResetPasswordToken(token,email);
        String resetPasswordLink = Utilities.getSiteUrl(request) + "/registration/password/reset_password?token="+ token;
        emailSenderService.sendMimeEmail(email,resetPasswordLink);
        System.out.println("-------------------------"+resetPasswordLink);
    } catch (Exception e) {
        data.put("isError",true);
    }

    return data;
}


    // without jquery
//    @PostMapping("/send-mail")
//    public String processForgotPasswordForm(HttpServletRequest request,Model model) {
//
//        //sendMail();
//        String email = request.getParameter("email");
//        String token = RandomString.make(30);
//        System.out.println("dustuiiiiiiiiiiiiiiiiiiiii");
//
//        try {
//            userManager.UpdateResetPasswordToken(token,email);
//            System.out.println("dustuiiiiiiiiiiiiiiiiiiii445454i");
//            String resetPasswordLink = Utilities.getSiteUrl(request) + "/registration/password/reset_password?token="+ token;
//            emailSenderService.sendMimeEmail(email,resetPasswordLink);
//            System.out.println("-------------------------"+resetPasswordLink);
//        } catch (Exception e) {
//           model.addAttribute("error",e.getMessage());
//        }
//
//        return "user/forgot-password";
//    }


    /* Reset Password */
    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token,Model model) {

        User user = userManager.GetByResetPasswordToken(token);
        if(user == null){
            System.out.println("invalid");
        }
        model.addAttribute("token",token);
        return "user/change-password";
    }


    @PostMapping("/reset_password")
    public  @ResponseBody Map<String, Object> processResetPassword (HttpServletRequest request,
    @RequestParam("token") String token,
    @RequestParam("password") String password,
    @RequestParam("checkPassword") String checkPassword){

        Map<String, Object> data = new HashMap<>();
        String resetLink = Utilities.getSiteUrl(request) + "/registration/password/reset_password?token="+ token;
        System.out.println(token);
        User user = userManager.GetByResetPasswordToken(token);
        if (user == null || !password.equals(checkPassword)){
            data.put("isError",true);
            data.put("errorMsg","Sifreniz Guncellenirken Hata Olustu");

        }
        if(user !=null){
            userManager.UpdatePassword(user,password);
        }

        return data;
    }

    // without jquery
//    @PostMapping("/reset_password")
//    public String processResetPassword(HttpServletRequest request,Model model) {
//        String token = request.getParameter("token");
//        String resetLink = Utilities.getSiteUrl(request) + "/registration/password/reset_password?token="+ token;
//        String password = request.getParameter("password");
//        String passwordCheck = request.getParameter("checkPassword");
//        System.out.println(token);
//
//        User user = userManager.GetByResetPasswordToken(token);
//        if (user == null){
//            model.addAttribute("pwError","UserNotFound");
//            System.out.println("=========================================================================== UserNotFound");
//            return  "redirect:" + resetLink ;
//        }
//        if(!password.equals(passwordCheck)){
//            model.addAttribute("pwError","Password Does Not Match");
//            System.out.println("=========================================================================== not matched");
//            return  "redirect:" + resetLink;
//        }
//        model.addAttribute("pwSuccess","Password Updated");
//        userManager.UpdatePassword(user,password);
//        return "user/change-password";
//    }

    @GetMapping("/result")
    public String showPasswordResultForm() {

        return "user/password-result";
    }


}
