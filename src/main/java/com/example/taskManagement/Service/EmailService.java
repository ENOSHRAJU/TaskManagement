package com.example.taskManagement.Service;

import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;

public interface EmailService {
    void sendVerificationEmail(User user, EmailVerificationToken verificationToken);
    void sendPasswordResetEmail(User user, PasswordResetToken resetToken);
}
