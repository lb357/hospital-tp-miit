package io.github.lb357.hospital_tp_miit.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<String> login() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/signup")
    public ResponseEntity<String> signup() {return ResponseEntity.ok("todo");} // todo
}
