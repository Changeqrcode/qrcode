package com.change.qrcode.repository.business.abstracts;

import com.change.qrcode.model.User;

public interface UserService {

    User findByUsername(String username);
    void UpdateResetPasswordToken(String token,String email) throws Exception;
    void UpdatePassword(User user,String newPassword);
    User GetByResetPasswordToken(String token);
}
