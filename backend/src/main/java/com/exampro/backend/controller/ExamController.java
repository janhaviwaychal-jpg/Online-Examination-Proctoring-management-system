
package com.exampro.backend.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExamController {

    private final JdbcTemplate jdbcTemplate;

    public ExamController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/exams")
    public List<Map<String, Object>> getExams() {

        String sql = "SELECT e.*, " +
                     "(SELECT COUNT(*) FROM Questions q " +
                     "WHERE q.exam_id = e.exam_id) AS total_questions " +
                     "FROM Exams e";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/exams/{examId}/questions")
    public List<Map<String, Object>> getQuestions(@PathVariable int examId) {

        String sql = "SELECT question_id, question_text, " +
                     "option_a, option_b, option_c, option_d, marks " +
                     "FROM Questions WHERE exam_id = ?";

        return jdbcTemplate.queryForList(sql, examId);
    }
}
