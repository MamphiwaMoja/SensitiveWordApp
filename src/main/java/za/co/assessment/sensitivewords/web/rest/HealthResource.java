package za.co.assessment.sensitivewords.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import za.co.assessment.sensitivewords.config.Constants;

@RestController
@RequestMapping(ApiPaths.HEALTH)
public class HealthResource {

    @GetMapping
    @Operation(summary = "Application health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", Constants.APPLICATION_NAME,
                "checkedAt", Instant.now().toString()
        ));
    }
}
