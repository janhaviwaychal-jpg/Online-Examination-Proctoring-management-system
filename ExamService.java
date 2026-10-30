
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ExamService {

    public static void displayExams() {
        String url = "jdbc:mysql://localhost:3306/online_examination";
        String user = "root";
        String password = "Janhavi@2007";

        String query = "SELECT exam_id, exam_title, duration_minutes, total_marks FROM Exams";

        try (
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query)
        ) {
            System.out.println("\n===== AVAILABLE EXAMS =====");

            while (result.next()) {
                System.out.println("Exam ID: " + result.getInt("exam_id"));
                System.out.println("Exam Name: " + result.getString("exam_title"));
                System.out.println("Duration: " + result.getInt("duration_minutes") + " minutes");
                System.out.println("Total Marks: " + result.getInt("total_marks"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error displaying exams: " + e.getMessage());
        }
    }
}
