package io.github.lb357.hospital_tp_miit.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String home(Model model) {return "pages/home/home";} //todo

    @GetMapping("/login")
    public String login(Model model) {return "pages/login/login";} //todo

    @GetMapping("/profile")
    public String profile(Model model) {return "pages/profile/profile";} //todo

    @GetMapping("/admin")
    public String admin(Model model) {return "pages/admin/admin";} //todo

    @GetMapping("/appointments")
    public String appointments(Model model) {return "pages/appointments/appointments";} //todo

    @GetMapping("/record")
    public String record(Model model) {return "pages/record/record";} //todo

    @GetMapping("/entry")
    public String entry(Model model) {return "pages/entry/entry";} //todo

    @GetMapping("/about")
    public String about(Model model) {return "pages/about/about";} //todo
}