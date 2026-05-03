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
	System.out.println("use \033[3mh\033[0m for help\n");
        while (true) {
            System.out.print("server> ");
            String cmd = sc.nextLine();

            switch (cmd) {
                case "list":
                    System.out.println("[ADMIN] Active users: " + AuthManager.getActiveUser());
                    break;
                case "no":
                    System.out.println("[ADMIN] Current clients: " + currentClients);
                    break;
		case "kick":
		    System.out.print("Username : ");
		    String user = sc.nextLine();
		    AuthManager.deactivateSocket(user);
		    System.out.println("User [DISCONNECTED] : " + user);
		    break;
		case "h":
		    System.out.println("1. \033[3mlist\033[0m to List connected client names.\n
				    2. \033[3mno\033[0m to get number of clients connected.\n
				    3. \033[3mkick\033[0m to kick a cilent.\n
				    4. \033[3mexit\033[0m to shut server.\n");
		    break;

                case "exit":
                    System.out.println("[ADMIN] Shutting down server...");
                    try { server.close(); } catch (Exception e) {}
                    System.exit(0);
                    break;
                default:
		    if(!cmd.isEmpty()) System.out.println("[ADMIN] Unknown command.");
            }
        }
    }
}

