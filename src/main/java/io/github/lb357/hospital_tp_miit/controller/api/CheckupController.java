package io.github.lb357.hospital_tp_miit.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkup")
public class CheckupController {
    @PostMapping("/create")
    public ResponseEntity<String> create() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/delete")
    public ResponseEntity<String> delete() {return ResponseEntity.ok("todo");} // todo

    @PostMapping("/get")
    public ResponseEntity<String> get() {return ResponseEntity.ok("todo");} // todo
}
