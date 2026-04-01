import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    static Scanner sc = new Scanner(System.in);

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pause() {
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    public static void main(String[] args) throws Exception {

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        while (true) {

            clear();

            System.out.println("==== Secure File Server ====");
            System.out.println("1 Login");
            System.out.println("2 Create Account");
            System.out.println("3 Exit");

            int choice = Integer.parseInt(sc.nextLine());

            if (choice == 3)
                return;

            Socket socket = new Socket(ip, port);

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            out.writeInt(choice);

            System.out.print("Username: ");
            String user = sc.nextLine();

            System.out.print("Password: ");
            String pass = sc.nextLine();

            out.writeUTF(user);
            out.writeUTF(pass);

            if (!in.readBoolean()) {

                System.out.println("Authentication failed.");
                pause();
                socket.close();
                continue;
            }

            while (true) {

                clear();

                System.out.println("==== File Server ====");
                System.out.println("1 Upload");
                System.out.println("2 List Files");
                System.out.println("3 Download");
                System.out.println("4 Logout");

                int option = Integer.parseInt(sc.nextLine());

                switch (option) {

                    case 1:
                        FileTransfer.upload(out);
                        pause();
                        break;

                    case 2:
                        FileTransfer.listFiles(in, out);
                        pause();
                        break;

                    case 3:
                        FileTransfer.download(in, out);
                        pause();
                        break;

                    case 4:
                        out.writeInt(4);
                        socket.close();
                        break;
                }

                if (option == 4)
                    break;
            }
        }
    }
}
