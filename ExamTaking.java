import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class ExamTaking {

    public static void startExam(int examId, int studentId, Scanner sc) {

        String url = "jdbc:mysql://localhost:3306/online_examination";
        String user = "root";
        String password = "Janhavi@2007";

        String query = "SELECT question_id, question_text, "
                + "option_a, option_b, option_c, option_d, "
                + "correct_option, marks "
                + "FROM Questions WHERE exam_id = ?";

        int score = 0;
        int totalMarks = 0;
        int attemptId = 0;

        try (Connection con = DriverManager.getConnection(
                url, user, password)) {

            con.setAutoCommit(false);

            try {
                // Create exam attempt
                String attemptQuery =
                    "INSERT INTO Exam_Attempts "
                    + "(student_id, exam_id, status) "
                    + "VALUES (?, ?, 'IN_PROGRESS')";

                try (PreparedStatement attemptPs =
                        con.prepareStatement(
                            attemptQuery,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    attemptPs.setInt(1, studentId);
                    attemptPs.setInt(2, examId);
                    attemptPs.executeUpdate();

                    try (ResultSet keys = attemptPs.getGeneratedKeys()) {
                        if (keys.next()) {
                            attemptId = keys.getInt(1);
                        } else {
                            throw new Exception(
                                "Could not create exam attempt."
                            );
                        }
                    }
                }

                // Display questions and save answers
                try (PreparedStatement ps = con.prepareStatement(query);
                     PreparedStatement answerPs = con.prepareStatement(
                        "INSERT INTO Student_Answers "
                        + "(attempt_id, question_id, selected_option) "
                        + "VALUES (?, ?, ?)")) {

                    ps.setInt(1, examId);

                    try (ResultSet rs = ps.executeQuery()) {

                        boolean found = false;

                        while (rs.next()) {
                            found = true;

                            int questionId = rs.getInt("question_id");
                            int marks = rs.getInt("marks");

                            System.out.println(
                                "\n" + rs.getString("question_text")
                            );
                            System.out.println(
                                "A. " + rs.getString("option_a")
                            );
                            System.out.println(
                                "B. " + rs.getString("option_b")
                            );
                            System.out.println(
                                "C. " + rs.getString("option_c")
                            );
                            System.out.println(
                                "D. " + rs.getString("option_d")
                            );

                            System.out.print(
                                "Enter your answer (A/B/C/D): "
                            );

                            String answer = sc.nextLine()
                                    .trim().toUpperCase();

                            while (!answer.matches("[ABCD]")) {
                                System.out.print(
                                    "Invalid answer. Enter A, B, C or D: "
                                );
                                answer = sc.nextLine()
                                        .trim().toUpperCase();
                            }

                            // Save selected answer
                            answerPs.setInt(1, attemptId);
                            answerPs.setInt(2, questionId);
                            answerPs.setString(3, answer);
                            answerPs.executeUpdate();

                            totalMarks += marks;

                            if (answer.equals(
                                    rs.getString("correct_option"))) {
                                score += marks;
                            }
                        }

                        if (!found) {
                            System.out.println(
                                "No questions found for this exam."
                            );
                            con.rollback();
                            return;
                        }
                    }
                }

                // Update exam attempt
                String updateAttempt =
                    "UPDATE Exam_Attempts "
                    + "SET status = 'SUBMITTED', "
                    + "submitted_at = CURRENT_TIMESTAMP "
                    + "WHERE attempt_id = ?";

                try (PreparedStatement updatePs =
                        con.prepareStatement(updateAttempt)) {

                    updatePs.setInt(1, attemptId);
                    updatePs.executeUpdate();
                }

                // Save final result
                String resultQuery =
                    "INSERT INTO Results "
                    + "(attempt_id, marks_obtained, total_marks) "
                    + "VALUES (?, ?, ?)";

                try (PreparedStatement resultPs =
                        con.prepareStatement(resultQuery)) {

                    resultPs.setInt(1, attemptId);
                    resultPs.setInt(2, score);
                    resultPs.setInt(3, totalMarks);
                    resultPs.executeUpdate();
                }

                con.commit();

                System.out.println("\n===== EXAM RESULT =====");
                System.out.println(
                    "Your score: " + score + "/" + totalMarks
                );
                System.out.println("Result saved successfully!");
                System.out.println("Answers saved successfully!");

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println(
                "Error during exam: " + e.getMessage()
            );
        }
    }
}
