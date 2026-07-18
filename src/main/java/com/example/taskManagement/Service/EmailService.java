package com.example.taskManagement.Service;

import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;

public interface EmailService {
    public void sendPasswordResetEmail(User user, PasswordResetToken resetToken);
}
