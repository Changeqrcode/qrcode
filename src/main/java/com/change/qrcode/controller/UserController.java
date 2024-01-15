package com.change.qrcode.controller;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.UploadImage;
import com.change.qrcode.model.User;
import com.change.qrcode.repository.PackagesRepository;
import com.change.qrcode.repository.QRRepository;
import com.change.qrcode.repository.UploadImageRepository;
import com.change.qrcode.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller
@RequestMapping("/user")
public class UserController {

    private QRRepository QRRepository;

    private UploadImageRepository uploadImageRepository;
	private PackagesRepository packagesRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private AuthenticationProvider authenticationProvider;

    private UserRepository userRepository;

    private static final String PAYTR_API_URL = "https://www.paytr.com/odeme/api/get-token";

    public UserController(QRRepository QRRepository,
                          UploadImageRepository uploadImageRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationProvider authenticationProvider,
                          UserRepository userRepository, 
                          PackagesRepository packagesRepository) {
        this.QRRepository = QRRepository;
        this.uploadImageRepository = uploadImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.userRepository = userRepository;
        this.packagesRepository = packagesRepository;
    }

    @GetMapping("/login/qr/{id}")
    public String login(HttpServletRequest httpServletRequest,
                        @PathVariable UUID id,
                        Model model) throws ServletException{
        model.addAttribute("id", id);
        return "user/login";
    }


    @PostMapping("/login/qr/{id}")
    public String loginPost(HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            @PathVariable UUID id,
                            @RequestParam("username") String username,
                            @RequestParam("password") String password) throws ServletException{

        QR p = QRRepository.findById(id).orElseThrow();

        User u = userRepository.findByUsername(username);
        if(u != null && passwordEncoder.matches(password, u.getPassword())){

            if(!p.getUser().getUsername().equals(username)){

                redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı adı veya şifre hatalı.");
                return "redirect:/qr/" + id;
            }

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate the user
            Authentication authentication = authenticationProvider.authenticate(authRequest);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Create a new session and add the security context.
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            return "redirect:/user/edit/qr/"+id;
        } else{

            redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı bu qr kodun sahibi değildir.");
            return "redirect:/qr/" + id;
        }
    }

    @PostMapping("/logout/{id}")
    public String logout(HttpServletRequest httpServletRequest, @PathVariable UUID id) throws ServletException {
        httpServletRequest.logout();
        return "redirect:/qr/" + id;
    }

    @GetMapping("/packages/{id}")
    public String showPackages(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable UUID id) throws ServletException  {
        QR p = QRRepository.findById(id).orElseThrow();
        Packages pckg = p.getUser().getPackages();

        List<Packages> packagesList = packagesRepository.findAll();
        // Gelen paketleri ID'lerine göre küçükten büyüğe sırala
        packagesList.sort(Comparator.comparing(Packages::getId));

        // İlk elemanı kaldır ve listenin sonuna ekle
        Packages firstPackage = packagesList.remove(0);
        packagesList.add(firstPackage);
        model.addAttribute("packages", packagesList);
        model.addAttribute("currPackage", pckg.getId());
        return "user/packages";
    }

    @GetMapping("/makePayment/{packageId}/{amount}")
    public String  makePayment(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable int packageId,
                         @PathVariable int amount) throws ServletException {

       // Bu bilgileri kendi Paytr hesabınıza göre güncelleyin
        String merchantId = "420534";
        String merchantKey = "pSP1odrTZQaZcP2j";
        String merchantSalt = "pM1gGwJxu97z5JK8";
        String apiUrl = "https://www.paytr.com/odeme/api/get-token";

        String email = "qrcode@gmail.com";
        String paymentAmount = String.valueOf(amount); // 9.99 için 9.99 * 100 = 999 gönderilmelidir.
        String merchantOid = generateUniqueMerchantOid(); // Benzersiz olmalıdır
        String user_name = "qrcode@gmail.com";
        String user_address = "Antalya";
        String user_phone = "5554443322";
        String merchant_ok_url = "https://www.changeqrcode.com/user/resultPackages";
        String merchant_fail_url = "https://www.changeqrcode.com/user/resultPackages";
        String user_basket = Base64.getEncoder().encodeToString("[{\"product\":\"Premium Paket\",\"amount\":\"100.00\",\"quantity\":1}]".getBytes()); 
        String userIp = "176.232.63.43";
        String timeout_limit = "30";    
        int debug_on = 1;   
        int test_mode = 0;   
        int no_installment = 0; 
        int max_installment = 0; 
        String currency = "TL";          
        String paytrToken = "";

        
        String hashStr = merchantId + userIp + merchantOid + email + paymentAmount +
        user_basket + no_installment + max_installment + currency + test_mode + merchantSalt;

        try {
            paytrToken = generatePaytrToken(hashStr, merchantKey);

        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
       
        MultiValueMap<String, Object> postVals = new LinkedMultiValueMap<>();
        postVals.add("grant_type", "client_credentials");
        postVals.add("merchant_id", merchantId);
        postVals.add("user_ip", userIp);
        postVals.add("merchant_oid", merchantOid);
        postVals.add("paytr_token", paytrToken);
        postVals.add("user_basket", user_basket);
        postVals.add("debug_on", debug_on);
        postVals.add("no_installment", no_installment);
        postVals.add("max_installment", max_installment);
        postVals.add("user_name", user_name);
        postVals.add("user_address", user_address);
        postVals.add("user_phone", user_phone);
        postVals.add("merchant_ok_url", merchant_ok_url);
        postVals.add("merchant_fail_url", merchant_fail_url);
        postVals.add("timeout_limit", timeout_limit);
        postVals.add("currency", currency);
        postVals.add("test_mode", test_mode);
        postVals.add("email", email);
        postVals.add("payment_amount", paymentAmount);

        RestTemplate restTemplate = new RestTemplate();
        
        String result = restTemplate.postForObject(apiUrl, postVals, String.class);
        String token = "";
        // JSON çevrimi
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(result);
            token = jsonNode.get("token").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // İlgili alanı al
       
        model.addAttribute("token", token);
        return "user/payment";
    }
    
    @PostMapping("/resultPackages")
    @ResponseBody
    public String  resultPackages(HttpServletRequest httpServletRequest,
                                    @RequestParam("merchant_oid") String merchantOid,
                                    @RequestParam("status") String status,
                                    @RequestParam("total_amount") String totalAmount,
                                    @RequestParam("hash") String hash,
                                    Model model,
                                    RedirectAttributes redirectAttributes) throws ServletException  {

            // Ödeme bildirimi POST parametrelerini al
            // API Entegrasyon Bilgileri - Mağaza paneline giriş yaparak BİLGİ sayfasından alabilirsiniz.
            String merchantKey = "pSP1odrTZQaZcP2j";
            String merchantSalt = "pM1gGwJxu97z5JK8";

            String combined = merchantOid + merchantSalt + status + totalAmount;
            String generatedHash = generateHmacSha256(combined, merchantKey);

            // Oluşturulan hash'i, paytr'dan gelen post içindeki hash ile karşılaştır
            if (!hash.equals(generatedHash)) {
                return "PAYTR notification failed: bad hash";
            }

            // BURADA YAPILMASI GEREKENLER
            // 1) Siparişin durumunu merchantOid değerini kullanarak veri tabanınızdan sorgulayın.
            // 2) Eğer sipariş zaten daha önceden onaylandıysa veya iptal edildiyse "OK" dönerek işlemi sonlandırın.
            model.addAttribute("merchantOid", merchantOid);
            model.addAttribute("status", status);
            model.addAttribute("totalAmount", totalAmount);
            if ("success".equals(status)) { // Ödeme Onaylandı
                // Bildirimin alındığını PayTR sistemine bildir.
                return "user/resultPackages";
                // BURADA YAPILMASI GEREKENLER ONAY İŞLEMLERİDİR.
                // Diğer işlemleri ekleyin
            } else { // Ödemeye Onay Verilmedi
                // Bildirimin alındığını PayTR sistemine bildir.
                return "user/resultPackages";
                // BURADA YAPILMASI GEREKENLER
                // Diğer işlemleri ekleyin
            }
            // Bildirimin alındığını PayTR sistemine bildir.
       
    }

    @GetMapping("/edit/qr/{id}")
    public String qrEdit(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable UUID id) throws ServletException {
        List<String> encodeds = new ArrayList<>();
        String encodedLogo = "";
        QR p = QRRepository.findById(id).orElseThrow();

        if(!p.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ){
            redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı bu qr kodun sahibi değildir.");
            return "redirect:/qr/" + id;
        }
        
        Packages pckg = p.getUser().getPackages();
        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        UploadImage logo = uploadImageRepository.findById(p.getLogo().getId()).get();

        if(logo != null ){
            encodedLogo= "data:image/png;base64," + Base64.getEncoder().encodeToString(logo.getImageData());          
        }

        if(p.getLinks() == null || p.getLinks().isEmpty()){
            p.setLinks("Deneme Linki - Sample Link");
        }

        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        model.addAttribute("links", p.getLinks());
        model.addAttribute("logo", encodedLogo);

        return "user/edit/qr";
    }

    @PostMapping("/uploadData/{id}")
    public String uploadImage(Model model,
                              HttpServletRequest httpServletRequest,
                              RedirectAttributes redirectAttributes,
                              @RequestParam("content") String text,
                              @RequestParam("uploadLinks") String links,
                              @RequestParam("images") MultipartFile[] files,
                              @PathVariable UUID id) throws IOException, ServletException {
        QR p = QRRepository.findById(id).orElseThrow();

        Packages pckg = p.getUser().getPackages();
        String[] linkArray = links.split(",");
        if (text.length() > pckg.getCharacterLimit()) {
            redirectAttributes.addFlashAttribute("error", "Text limiti aşıldı. Daha fazlası için lütfen yüksek bir pakete geçiş yapınız.");
            redirectAttributes.addFlashAttribute("redirectLink", "/user/packages/" + id);
            return "redirect:/user/edit/qr/" + p.getId();
        } else if (linkArray.length > pckg.getLinkLimit()) {
            redirectAttributes.addFlashAttribute("error", "Link limiti aşıldı. Daha fazlası için lütfen yüksek bir pakete geçiş yapınız.");
            redirectAttributes.addFlashAttribute("redirectLink", "/user/packages/" + id);
            return "redirect:/user/edit/qr/" + p.getId();
        } else if (files.length > pckg.getImageLimit()) {
            redirectAttributes.addFlashAttribute("error", "Fotoğraf limiti aşıldı. ");
            redirectAttributes.addFlashAttribute("redirectLink", "/user/packages/" + id);
            return "redirect:/user/edit/qr/" + p.getId();
        }
        else{

      
            p.setTextContent(text);
            p.setLinks(links);
            p.setIsRecorded(true);
            uploadImageRepository.deleteAllByQRId(p.getId());

            for(MultipartFile f:files){
                if(f.getBytes().length > 0){
                    UploadImage image = new UploadImage();
                    image.setQR(p);
                    image.setImageData(f.getBytes());
                    uploadImageRepository.saveAndFlush(image);
                }

            }
            


            QRRepository.saveAndFlush(p);

            httpServletRequest.logout();

            return "redirect:/qr/" + p.getId();
        
    }

    }


    public static String generateUniqueMerchantOid() {
        // Zaman damgası ekleyerek benzersiz bir değer oluştur
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

        // Rastgele bir sayı veya UUID ekleyerek daha fazla benzersizlik sağla
        String randomValue = UUID.randomUUID().toString().replace("-", "");

        return timestamp + randomValue;
    }

    private static String generatePaytrToken(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
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
