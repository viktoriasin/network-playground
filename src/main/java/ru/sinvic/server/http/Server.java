package ru.sinvic.server.http;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sinvic.model.DataObject;
import ru.sinvic.server.http.RequestTimeInMemoryStorage.RequestTimeStatisticsResult;
import ru.sinvic.server.util.DataObjectGeneratorService;

@RestController
@RequiredArgsConstructor
public class Server {
    private static final Logger logger = LoggerFactory.getLogger("HttpServer");

    private final RequestTimeInMemoryStorage requestTimeInMemoryStorage;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Ok!");
    }

    @GetMapping("/get-statistics")
    public ResponseEntity<RequestTimeStatisticsResult> getStatistics(@RequestParam("path") String path) {
        return ResponseEntity.ok(requestTimeInMemoryStorage.calculateRequestStatistic(path));
    }

    @GetMapping("/get-message")
    public ResponseEntity<DataObject> getMessage(@RequestParam("request_id") int requestId) {
        logger.info("HttpServer get request with request id: {}", requestId);
        DataObject dataObject = DataObjectGeneratorService.generateDataObject(requestId);
        return ResponseEntity.ok(dataObject);
    }
}
