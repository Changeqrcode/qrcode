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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


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

                var test = p.getUser().getUsername();

                redirectAttributes.addFlashAttribute("loginError", "Girilen kullanıcı adı veya şifre hatalı.");
                return "redirect:/qr/login/" + id;
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

    @PostMapping("/leaveqr/{id}")
    public String leaveqr(HttpServletRequest httpServletRequest, @PathVariable UUID id) throws ServletException {
        QR p = QRRepository.findById(id).orElseThrow();
        p.setUser(null);
        p.setIsRecorded(false);
        p.setLinks(null);
        p.setLogo(null);
        p.setTextContent(null);
        uploadImageRepository.deleteAllByQRId(p.getId());

        UploadImage logo = p.getLogo();
        p.setLogo(null);

        QRRepository.saveAndFlush(p);

        if(Objects.nonNull(logo)){
            uploadImageRepository.delete(logo);
        }

        httpServletRequest.logout();
        return "redirect:/qr/" + id;
    }


    @GetMapping("/packages/{id}")
    public String showPackages(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable UUID id) throws ServletException  {
        QR p = QRRepository.findById(id).orElseThrow();
        Packages pckg = p.getPackages();

        List<Packages> packagesList = packagesRepository.findAll();
        // Gelen paketleri ID'lerine göre küçükten büyüğe sırala
        packagesList.sort(Comparator.comparing(Packages::getId));

        // Free olan paketi en sona ekle
        Packages firstPackage = null;

        for(Packages pack:packagesList){
            if(pack.getPackageValue().equals(AdminController.FREE_PACKAGE_VALUE)){
                firstPackage = pack;
            }
        }

        packagesList.remove(firstPackage);
        packagesList.add(firstPackage);

        model.addAttribute("qrid", id);
        model.addAttribute("packages", packagesList);
        model.addAttribute("currPackage", pckg.getId());
        return "user/packages";
    }

    @GetMapping("/makePayment/{packageId}/{amount}/{qrid}")
    public String  makePayment(HttpServletRequest httpServletRequest,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @PathVariable int packageId,
                         @PathVariable int amount,
                         @PathVariable UUID qrid) throws ServletException {

        List<Packages> packagesList = packagesRepository.findAll();
        Packages packageToUse;
        var selectedPackage = packagesList.stream()
            .filter(p -> p.getId() == packageId)
            .findFirst();

        if (selectedPackage.isPresent()) {
            packageToUse = selectedPackage.get();

        } else {
            return "error";
        }

        QR qr = QRRepository.findById(qrid).orElseThrow();
        qr.setPackages(packageToUse);
        qr.setPackageEndDate(java.sql.Date.valueOf(LocalDate.now().plusYears(packageToUse.getYear())
                .plusDays(packageToUse.getDay())));
        QRRepository.saveAndFlush(qr);

        String productName = packageToUse.getName();
        Integer productAmount = packageToUse.getPrice();
        String basketString = "[{\"product\":\"" + productName + "\",\"amount\":\"" + productAmount + ".00\",\"quantity\":1}]";
        byte[] basketBytes = basketString.getBytes();

        String merchantId = "420534";
        String merchantKey = "pSP1odrTZQaZcP2j";
        String merchantSalt = "pM1gGwJxu97z5JK8";
        String apiUrl = "https://www.paytr.com/odeme/api/get-token";

        String email = "qrcode@gmail.com";
        String paymentAmount = String.valueOf(productAmount*100); // 9.99 için 9.99 * 100 = 999 gönderilmelidir.
        String merchantOid = generateUniqueMerchantOid(); // Benzersiz olmalıdır

        String user_name = "qrcode@gmail.com";
        String user_address = "Antalya";
        String user_phone = "5554443322";
        String merchant_ok_url = "https://www.changeqr.com/qr/" + qrid;
        String merchant_fail_url = "https://www.changeqr.com/payment/failpackage/"+ qrid;
        String user_basket = Base64.getEncoder().encodeToString(basketBytes);
        String userIp = getClientIp(httpServletRequest);
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

        List<UploadImage> images = uploadImageRepository.findByQRId(p.getId());

        if(images != null && images.size() > 0){
            for (UploadImage u:images){
                encodeds.add("data:image/png;base64," + Base64.getEncoder().encodeToString(u.getImageData()));
            }
        }

        try {
            UploadImage logo = uploadImageRepository.findById(p.getLogo().getId()).get();
            if(logo != null ){
                encodedLogo= "data:image/png;base64," + Base64.getEncoder().encodeToString(logo.getImageData());      
                model.addAttribute("logo", encodedLogo);    
            }
    
        } catch (Exception e) {
           
        }
        

    
        if (p.getTextContent() == null){
            p.setTextContent("");
        }

        model.addAttribute("qr", p);
        model.addAttribute("images", encodeds);
        model.addAttribute("links", p.getLinks());
        model.addAttribute("username", p.getUser().getUsername());

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

        Packages pckg = p.getPackages();
        String[] linkArray = links.split(",");
        int linklen = linkArray.length;
        int filelen = files.length;
        var filename = files[0].getOriginalFilename();
        if(links.equals("")){    
            linklen = 0;
        }
        if(files.length == 1 && filename.equals("")){    
            filelen = 0;
        }
        if (text.length() > pckg.getCharacterLimit()) {
            redirectAttributes.addFlashAttribute("error", "Text limiti aşıldı. Daha fazlası için lütfen yüksek bir pakete geçiş yapınız.");
            redirectAttributes.addFlashAttribute("redirectLink", "/user/packages/" + id);
            return "redirect:/user/edit/qr/" + p.getId();
        } else if (linklen > pckg.getLinkLimit()) {
            redirectAttributes.addFlashAttribute("error", "Link limiti aşıldı. Daha fazlası için lütfen yüksek bir pakete geçiş yapınız.");
            redirectAttributes.addFlashAttribute("redirectLink", "/user/packages/" + id);
            return "redirect:/user/edit/qr/" + p.getId();
        } else if (filelen > pckg.getImageLimit()) {
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


    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }
}
