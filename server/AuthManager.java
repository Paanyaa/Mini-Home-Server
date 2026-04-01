import java.io.*;
import java.security.MessageDigest;

public class AuthManager {

    private static final String FILE = "users.txt";

    // Hash password using SHA-256
    private static String hash(String input) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = md.digest(input.getBytes());

        StringBuilder hex = new StringBuilder();

        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    // Authenticate user
    public static boolean authenticate(String user, String pass) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(FILE));
        String line;

        String hashed = hash(pass);

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(":");

            if (parts[0].equals(user) && parts[1].equals(hashed)) {
                br.close();
                return true;
            }
        }

        br.close();
        return false;
    }

    // Check if user already exists
    public static boolean userExists(String user) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(FILE));
        String line;

        while ((line = br.readLine()) != null) {

            if (line.split(":")[0].equals(user)) {
                br.close();
                return true;
            }
        }

        br.close();
        return false;
    }

    // Add new user with hashed password
    public static void addUser(String user, String pass) throws Exception {

        FileWriter fw = new FileWriter(FILE, true);

        String hashed = hash(pass);

        fw.write(user + ":" + hashed + "\n");

        fw.close();
    }
}
