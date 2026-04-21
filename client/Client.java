import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

        static Scanner sc = new Scanner(System.in);
	public static volatile boolean running = true;

        public static void clear() {
                System.out.print("\033[H\033[2J");
                System.out.flush();
        }

        public static void pause() {
		String[] pattern = {".    " , ". .  ", ". . ."};
		Thread t = new Thread(() -> {
			try {
				while (running) {
					//System.out.print("\nPress Enter");
					for (String p : pattern) {
						if (!running) break;
						System.out.print("\rPress Enter " + p);
						Thread.sleep(1500);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		t.start();
		sc.nextLine();

		try {
			running = false;
			t.join();
			t.interrupt();
		} catch (InterruptedException f) { } 
	}

	public static void runSession(Socket socket, DataInputStream in, DataOutputStream out) throws Exception {
			 while (true) {

                                        clear();

                                        System.out.println("==== File Server ====");
                                        System.out.println("1 Upload");
                                        System.out.println("2 List Files");
                                        System.out.println("3 Download");
                                        System.out.println("4 Logout");

                                        String input = sc.nextLine();
				        if (input.length() > 1 || input.matches("//d")) {
						System.out.println("Choose properly. ");
						Thread.sleep(1500);
						continue;
					}
					int option = Integer.parseInt(input);

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

        public static void main(String[] args) throws Exception {

                String ip = args[0];
                int port = Integer.parseInt(args[1]);

                while (true) {

                        clear();

                        System.out.println("==== Secure File Server ====");
                        System.out.println("1 Login");
                        System.out.println("2 Create Account");
                        System.out.println("3 Exit");

			String input = sc.nextLine();
			if (input.length() > 1 || !input.matches("\\d")) {
				System.out.println("Choose properly"); 
				pause();
				continue; 
			} System.out.println();
					

                        int choice = Integer.parseInt(input);

                        if (choice == 3)
                                return;

			if (choice != 1 && choice != 2 && choice != 3) {
				System.out.println("Choose properly"); 
				pause();
				continue; 
			} System.out.println();	

                        try {
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

				runSession(socket, in, out);
			}  catch (IOException e) {
                                System.out.println("Either username is alr used for login / kicked from SERVER / SERVER is offline.");
				Thread.sleep(500);
				pause();
                        }

                }
        }
}
