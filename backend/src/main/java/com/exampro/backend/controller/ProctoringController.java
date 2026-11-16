
package com.exampro.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proctoring")
@CrossOrigin(origins = "http://localhost:5173")
public class ProctoringController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/activity")
    public Map<String, Object> saveActivity(
            @RequestBody Map<String, Object> data) {

        try {
            Integer attemptId = Integer.valueOf(
                data.get("attemptId").toString()
            );

            String activityType =
                data.get("activityType").toString();

            String details =
                data.get("details") == null
                ? ""
                : data.get("details").toString();

            jdbcTemplate.update(
                "INSERT INTO Proctoring_Activities " +
                "(attempt_id, activity_type, details) VALUES (?, ?, ?)",
                attemptId, activityType, details
            );

            return Map.of(
                "success", true,
                "message", "Proctoring activity saved"
            );

        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Unable to save proctoring activity"
            );
        }
    }
}
