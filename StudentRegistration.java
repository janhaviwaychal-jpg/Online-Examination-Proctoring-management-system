
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;

public class StudentRegistration {

    public static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, 120000, 256
        );

        byte[] hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .getEncoded();

        spec.clearPassword();

        return Base64.getEncoder().encodeToString(salt)
            + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/online_examination";
        String user = "root";
        String password = "Janhavi@2007";

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter full name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String studentPassword = scanner.nextLine();

            if (name.isEmpty() || email.isEmpty()
                    || studentPassword.length() < 8
                    || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                System.out.println(
                    "Invalid details. Enter a name, valid email, "
                    + "and password of at least 8 characters."
                );
                return;
            }

            String hashedPassword = hashPassword(studentPassword);

            String sql = "INSERT INTO Users "
                + "(full_name, email, password_hash, role, status) "
                + "VALUES (?, ?, ?, 'STUDENT', 'ACTIVE')";

            try (Connection connection =
                     DriverManager.getConnection(url, user, password);
                 PreparedStatement statement =
                     connection.prepareStatement(sql)) {

                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, hashedPassword);

                statement.executeUpdate();

                System.out.println(
                    "Student registered successfully!"
                );

            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    System.out.println(
                        "This email is already registered."
                    );
                } else {
                    System.out.println(
                        "Database error: " + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
