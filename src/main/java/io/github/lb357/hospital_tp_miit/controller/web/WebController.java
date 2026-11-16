package io.github.lb357.hospital_tp_miit.controller.web;

import io.github.lb357.hospital_tp_miit.component.CookieTokenResolver;
import io.github.lb357.hospital_tp_miit.model.session.Session;
import io.github.lb357.hospital_tp_miit.model.user.User;
import io.github.lb357.hospital_tp_miit.model.user.UserRole;
import io.github.lb357.hospital_tp_miit.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final CookieTokenResolver cookieTokenResolver;
    private final AccountService accountService;


    @GetMapping("/")
    public String home(Model model) {return "pages/home/home";}

    @GetMapping("/login")
    public String login(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session != null) return "redirect:/profile";
        return "pages/login/login";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session == null) return "redirect:/login";
        User user = accountService.getUser(session);
        return switch (user.getRole()) {
            case ADMIN -> "pages/profile/admin-profile";
            case PATIENT -> "pages/profile/patient-profile";
            case DOCTOR -> "pages/profile/doctor-profile";
        };
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session == null) return "redirect:/login";
        User user = accountService.getUser(session);
        if (user.getRole().equals(UserRole.ADMIN)) {
            return "pages/admin/admin";
        } else {
            return "pages/profile";
        }
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session == null) return "redirect:/login";
        User user = accountService.getUser(session);
        return switch (user.getRole()) {
            case ADMIN -> "pages/appointments/admin-appointments";
            case PATIENT -> "pages/appointments/patient-appointments";
            case DOCTOR -> "pages/appointments/doctor-appointments";
        };
    }

    @GetMapping("/record")
    public String record(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session == null) return "redirect:/login";
        User user = accountService.getUser(session);
        return switch (user.getRole()) {
            case ADMIN, DOCTOR -> "pages/record/other-record";
            case PATIENT -> "pages/record/self-record";
        };
    }

    @GetMapping("/entry")
    public String entry(Model model) {
        Session session = cookieTokenResolver.getSession();
        if (session == null) return "redirect:/login";
        User user = accountService.getUser(session);
        if (user.getRole().equals(UserRole.DOCTOR)) {
            return "pages/entry/entry";
        } else {
            return "pages/profile";
        }
    }

    @GetMapping("/about")
    public String about(Model model) {return "pages/about/about";}
}