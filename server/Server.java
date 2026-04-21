import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    public static final int MAX_CLIENTS = 5;
    public static int currentClients = 0;

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]);

        new File("server_storage").mkdirs();
        new File("users.txt").createNewFile();

        ServerSocket server = new ServerSocket(port);

        System.out.println("[LOG] Server running on port " + port);

        // Thread for accepting clients
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();

                    synchronized (Server.class) {
                        if (currentClients >= MAX_CLIENTS) {
                            System.out.println("[LOG] Server full.");
                            socket.close();
                            continue;
                        }
                    }

                    System.out.println("[LOG] New client connected: " + socket);
                    new Thread(new ClientHandler(socket)).start();

                } catch (Exception e) { }
            }
        }).start();

        // Admin console runs immediately in main thread
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("server> ");
            String cmd = sc.nextLine();

            switch (cmd) {
                case "list":
                    System.out.println("[ADMIN] Active users: " + AuthManager.getActiveUser());
                    break;
                case "clients":
                    System.out.println("[ADMIN] Current clients: " + currentClients);
                    break;
		case "kick":
		    System.out.print("Username : ");
		    String user = sc.nextLine();
		    AuthManager.deactivateSocket(user);
		    System.out.println("User [DISCONNECTED] : " + user);
		    break;

                case "exit":
                    System.out.println("[ADMIN] Shutting down server...");
                    try { server.close(); } catch (Exception e) {}
                    System.exit(0);
                    break;
                default:
                    System.out.println("[ADMIN] Unknown command.");
            }
        }
    }
}

