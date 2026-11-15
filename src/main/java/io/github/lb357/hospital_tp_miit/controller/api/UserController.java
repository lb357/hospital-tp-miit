package io.github.lb357.hospital_tp_miit.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @PostMapping("/delete")
    public ResponseEntity<String> delete() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/update")
    public ResponseEntity<String> update() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/profile")
    public ResponseEntity<String> profile() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/get")
    public ResponseEntity<String> get() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/specialization/create")
    public ResponseEntity<String> specializationCreate() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/specialization/update")
    public ResponseEntity<String> specializationUpdate() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/specialization/get")
    public ResponseEntity<String> specializationGet() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/specialists")
    public ResponseEntity<String> specialists() {return ResponseEntity.ok("todo");} // todo
}
