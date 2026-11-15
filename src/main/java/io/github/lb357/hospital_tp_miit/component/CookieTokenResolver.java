package io.github.lb357.hospital_tp_miit.component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieTokenResolver {
    private final HttpServletRequest request;

    public CookieTokenResolver(HttpServletRequest request) {
        this.request = request;
    }

    public String resolve() {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies).filter(cookie -> "token".equals(cookie.getName())).map(
                Cookie::getValue
        ).findFirst().orElse(null);
    }
}