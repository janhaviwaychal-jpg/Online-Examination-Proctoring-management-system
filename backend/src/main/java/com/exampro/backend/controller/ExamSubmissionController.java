
package com.exampro.backend.controller;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamSubmissionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/{examId}/submit")
    @Transactional
    public Map<String, Object> submitExam(
            @PathVariable int examId,
            @RequestBody Map<String, Object> data) {

        int studentId = Integer.parseInt(
            String.valueOf(data.get("studentId"))
        );

        Object answersObject = data.get("answers");

        if (!(answersObject instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Answers are required");
        }

        Map<?, ?> submittedAnswers = (Map<?, ?>) answersObject;

        List<Map<String, Object>> questions =
            jdbcTemplate.queryForList(
                "SELECT question_id, marks FROM questions WHERE exam_id = ?",
                examId
            );

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No questions found");
        }

        Map<Integer, String> answerMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : submittedAnswers.entrySet()) {
            int questionId = Integer.parseInt(
                String.valueOf(entry.getKey())
            );

            String option = String.valueOf(entry.getValue())
                .trim().toUpperCase();

            if (!option.matches("[ABCD]")) {
                throw new IllegalArgumentException("Invalid answer");
            }

            answerMap.put(questionId, option);
        }

        if (answerMap.size() != questions.size()) {
            throw new IllegalArgumentException(
                "Please answer all questions"
            );
        }

        for (Map<String, Object> question : questions) {
            int id = ((Number) question.get("question_id")).intValue();

            if (!answerMap.containsKey(id)) {
                throw new IllegalArgumentException("Invalid question answer");
            }
        }

        Integer studentCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE user_id = ? AND role = 'STUDENT' AND status = 'ACTIVE'",
            Integer.class, studentId
        );

        if (studentCount == null || studentCount == 0) {
            throw new IllegalArgumentException("Invalid student");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO exam_attempts (student_id, exam_id, status) VALUES (?, ?, 'IN_PROGRESS')",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException("Could not create exam attempt");
        }

        int attemptId = generatedId.intValue();

        for (Map.Entry<Integer, String> answer : answerMap.entrySet()) {
            jdbcTemplate.update(
                "INSERT INTO student_answers (attempt_id, question_id, selected_option) VALUES (?, ?, ?)",
                attemptId, answer.getKey(), answer.getValue()
            );
        }

        BigDecimal totalMarks = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(marks), 0) FROM questions WHERE exam_id = ?",
            BigDecimal.class, examId
        );

        BigDecimal marksObtained = jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(SUM(
                CASE
                    WHEN UPPER(TRIM(q.correct_option)) = UPPER(TRIM(sa.selected_option))
                    THEN q.marks
                    ELSE 0
                END
            ), 0)
            FROM student_answers sa
            JOIN questions q ON sa.question_id = q.question_id
            WHERE sa.attempt_id = ?
            """,
            BigDecimal.class, attemptId
        );

        jdbcTemplate.update(
            "UPDATE exam_attempts SET status = 'SUBMITTED', submitted_at = NOW() WHERE attempt_id = ?",
            attemptId
        );

        jdbcTemplate.update(
            "INSERT INTO results (attempt_id, marks_obtained, total_marks) VALUES (?, ?, ?)",
            attemptId, marksObtained, totalMarks
        );

        return Map.of(
            "success", true,
            "message", "Exam submitted successfully",
            "attemptId", attemptId,
            "marksObtained", marksObtained,
            "totalMarks", totalMarks
        );
    }
}
