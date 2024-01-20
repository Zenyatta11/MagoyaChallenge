package com.zenyatta.magoya.challenge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Health {

    /**
     * Healthcheck endpoint.

     * @return Returns "alive" as a check if the service is up and running.
     */
    @GetMapping("/healthz")
    public String getHealthStatus() {
        return "alive";
    }
}
