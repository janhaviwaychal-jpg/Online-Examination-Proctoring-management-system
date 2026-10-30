
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class StudentLogin {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String url = "jdbc:mysql://localhost:3306/online_examination";
        String dbUser = "root";
        String dbPassword = "Janhavi@2007";

        try {
            Connection con = DriverManager.getConnection(
                url, dbUser, dbPassword
            );

            String sql = "SELECT user_id, password_hash FROM Users WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int studentId = rs.getInt("user_id");
                String storedHash = rs.getString("password_hash");

                String[] parts = storedHash.split(":");
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

                PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, 120000, 256
                );

                SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                byte[] actualHash =
                    factory.generateSecret(spec).getEncoded();

                spec.clearPassword();

                if (java.security.MessageDigest.isEqual(
                    expectedHash, actualHash)) {

                    System.out.println("Login successful!");

                    ExamService.displayExams();

                    System.out.print("Enter Exam ID: ");
                    int examId = Integer.parseInt(sc.nextLine());
                    ExamTaking.startExam(examId, studentId, sc);

                } else {
                    System.out.println("Invalid email or password.");
                }

            } else {
                System.out.println("Invalid email or password.");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        sc.close();
    }
}
