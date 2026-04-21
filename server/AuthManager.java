import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.net.Socket;

public class AuthManager {
    private static final String USER_FILE = "users.txt";
    private static final Set<String> activeUsers = new HashSet<>();
    private static final Map<String, Socket> activeSocket = new HashMap<>();

    private static String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();

        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean authenticate(String username, String password) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String hashedPassword = hashPassword(password);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals(username) && parts[1].equals(hashedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean userExists(String username) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0].equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addUser(String username, String password) throws Exception {
        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            String hashedPassword = hashPassword(password);
            writer.write(username + ":" + hashedPassword + "\n");
        }
    }

    public static boolean activeUsers(String username) {
        synchronized (activeUsers) {
            if (activeUsers.contains(username)) {
                System.out.println("User " + username + " already logged in.");
                return false;
            } else {
                activeUsers.add(username);
                return true;
            }
        }
    }

    public static void inactiveUsers(String username) {
        synchronized (activeUsers) {
            activeUsers.remove(username);
        }
    }

    public static Set<String> getActiveUser() {
        return Collections.unmodifiableSet(activeUsers);
    }

    public static void putActiveSocket(String username, Socket socket) {
	    activeSocket.put(username, socket);
    }

    public static void deactivateSocket(String username) {
	    try {
	    	if (!activeSocket.containsKey(username)) System.out.println("No active user found"); 
	    	Socket socket = activeSocket.get(username);
	    	socket.close();
	    } catch (IOException e) {
		    System.out.println("Socket Error");
	    }
    }
}

