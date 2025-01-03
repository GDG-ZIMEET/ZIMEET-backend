package com.gdg.z_meet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public String CheckingServerState() {
        return LocalDateTime.now().toString();
    }
}
