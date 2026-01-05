package ru.sinvic.server.http.perf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LeakedServer {
    private final LeakedContainer leakedContainer;

    @GetMapping("/create-objects")
    public ResponseEntity<String> createObjects() {
        leakedContainer.addObjects();
        return ResponseEntity.ok("Ok!");
    }

    @GetMapping("/clear-objects")
    public ResponseEntity<String> clear() {
        leakedContainer.clear();
        return ResponseEntity.ok("Ok!");
    }
}
