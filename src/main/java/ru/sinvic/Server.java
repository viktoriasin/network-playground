package ru.sinvic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Server {

    @GetMapping("/ping")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Ok!");
    }

}
