
import java.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Scanner;

public class PasswordReset {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your new password: ");
        String password = sc.nextLine();

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, 120000, 256
        );

        SecretKeyFactory factory =
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        String storedHash =
            Base64.getEncoder().encodeToString(salt) + ":" +
            Base64.getEncoder().encodeToString(hash);

        System.out.println("\nGenerated password hash:");
        System.out.println(storedHash);

        spec.clearPassword();
        sc.close();
    }
}
