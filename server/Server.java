import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int MAX_CLIENTS = 5;
    public static int currentClients = 0;

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]);

        new File("server_storage").mkdirs();
        new File("users.txt").createNewFile();

        ServerSocket server = new ServerSocket(port);

        System.out.println("Server running on port " + port);

        while (true) {

            Socket socket = server.accept();

            synchronized (Server.class) {

                if (currentClients >= MAX_CLIENTS) {
                    System.out.println("Server full.");
                    socket.close();
                    continue;
                }
            }

            new Thread(new ClientHandler(socket)).start();
        }
    }
}
