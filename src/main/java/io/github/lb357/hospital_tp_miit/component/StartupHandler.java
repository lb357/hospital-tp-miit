package io.github.lb357.hospital_tp_miit.component;

import io.github.lb357.hospital_tp_miit.service.AccountService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupHandler {
    @Value("${hospital.init-admin-password}")
    private String adminPassword;

    private final AccountService accountService;

    @PostConstruct
    public void init() {
        if (accountService.getUser(1) == null)
            accountService.signupAdmin("Фамилия", "Имя", "Отчество", adminPassword);
    }
}
