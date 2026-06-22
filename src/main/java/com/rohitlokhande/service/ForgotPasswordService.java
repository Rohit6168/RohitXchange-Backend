package com.rohitlokhande.service;

import com.rohitlokhande.domain.VerificationType;
import com.rohitlokhande.model.ForgotPasswordToken;
import com.rohitlokhande.model.User;

public interface ForgotPasswordService {

    ForgotPasswordToken createToken(User user, String id, String otp,
                                    VerificationType verificationType,String sendTo);

    ForgotPasswordToken findById(String id);

    ForgotPasswordToken findByUser(Long userId);

    void deleteToken(ForgotPasswordToken token);

    boolean verifyToken(ForgotPasswordToken token,String otp);
}
