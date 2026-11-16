package io.github.lb357.hospital_tp_miit.component;

import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieTokenResolver {
    private final HttpServletRequest request;
    private final AccountService accountService;

    public CookieTokenResolver(HttpServletRequest request, AccountService accountService) {
        this.request = request;
        this.accountService = accountService;
    }

    public String resolve() {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies).filter(cookie -> "token".equals(cookie.getName())).map(
                Cookie::getValue
        ).findFirst().orElse(null);
    }

    public Session getSession() {
        return accountService.getSession(resolve());
    }
}