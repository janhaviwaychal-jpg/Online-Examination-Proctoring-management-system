
package com.exampro.backend;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ResultController {

    private final JdbcTemplate jdbcTemplate;

    public ResultController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getStudentResults(
            @PathVariable int studentId) {

        String sql = """
            SELECT
                r.result_id,
                r.attempt_id,
                e.exam_title,
                r.marks_obtained,
                r.total_marks,
                r.evaluated_at
            FROM results r
            JOIN exam_attempts ea
                ON r.attempt_id = ea.attempt_id
            JOIN exams e
                ON ea.exam_id = e.exam_id
            WHERE ea.student_id = ?
            ORDER BY r.evaluated_at DESC
            """;

        return jdbcTemplate.queryForList(sql, studentId);
    }
}
