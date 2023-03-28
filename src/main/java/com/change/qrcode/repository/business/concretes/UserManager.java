package com.change.qrcode.repository.business.concretes;

import com.change.qrcode.model.User;
import com.change.qrcode.repository.UserRepository;
import com.change.qrcode.repository.business.abstracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserManager implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserManager(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void UpdateResetPasswordToken(String token, String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if(user != null){
            user.setResetPasswordToken(token);
            userRepository.save(user);
        }
        else {
            throw new Exception("User Not Found");
        }
    }

    @Override
    public void UpdatePassword(User user,String newPassword) {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);

        userRepository.save(user);

    }

    @Override
    public User GetByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }
}

