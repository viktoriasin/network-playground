package ru.sinvic.server.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sinvic.server.http.RequestTimeInMemoryStorage.RequestTimeStatisticsResult;

@RestController
@RequiredArgsConstructor
public class Server {

    private final RequestTimeInMemoryStorage requestTimeInMemoryStorage;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Ok!");
    }

    @GetMapping("/get-statistics")
    public ResponseEntity<RequestTimeStatisticsResult> getStatistics(@RequestParam("path") String path) {
        return ResponseEntity.ok(requestTimeInMemoryStorage.calculateRequestStatistic(path));
    }
}
