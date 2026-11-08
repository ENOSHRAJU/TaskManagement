package com.example.taskManagement.Common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookie {

    private static final String NAME = "refreshToken";
    private static final String PATH = "/auth";
    private static final Duration MAX_AGE = Duration.ofDays(7); // keep equal to REFRESH_TOKEN_EXPIRY_DAYS

    private final boolean secure;

    public RefreshTokenCookie(@Value("${app.cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie create(String token) {
        return build(token, MAX_AGE);
    }

    // Used on logout: same cookie, empty value, zero lifetime -> browser deletes it
    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
