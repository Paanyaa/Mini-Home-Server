import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

        private static final String KEY = "1234567890123456";
        private static final String STORAGE = "server_storage";
        private static final int BUFFER = 4096;

        private Socket socket;

        public ClientHandler(Socket socket) {
                this.socket = socket;
        }

        public void run() {

                String username = null;
                boolean counted = false; // FIX: track if client was counted

                try {

                        DataInputStream in =
                                new DataInputStream(socket.getInputStream());

                        DataOutputStream out =
                                new DataOutputStream(socket.getOutputStream());

                        int action = in.readInt();

                        String user = in.readUTF();
                        String pass = in.readUTF();

                        if (action == 1) {

                                if (!AuthManager.authenticate(user, pass)) {

                                        out.writeBoolean(false);
                                        socket.close();
                                        return;
                                }

                        } else if (action == 2) {

                                if (AuthManager.userExists(user)) {

                                        out.writeBoolean(false);
                                        socket.close();
                                        return;
                                }

                                AuthManager.addUser(user, pass);
                        }

                        username = user;
                        out.writeBoolean(true);

                        synchronized(Server.class) {
                                Server.currentClients++;
                                counted = true; // mark as counted
                        }

                        System.out.println("[CONNECTED] " + username);
                        System.out.println("Active clients: " + Server.currentClients);

                        String userDir = STORAGE + "/" + username;
                        new File(userDir).mkdirs();

                        while (true) {

                                int option = in.readInt();

                                switch (option) {

                                case 1:
                                        uploadFile(in, username, userDir);
                                        break;

                                case 2:
                                        listFiles(out, userDir);
                                        break;

                                case 3:
                                        downloadFile(in, out, userDir);
                                        break;

                                case 4:
                                        System.out.println("[DISCONNECTED] " + username);
                                        socket.close();
                                        return;
                                }
                        }

                } catch (Exception e) {

                        System.out.println("[DISCONNECTED] " + username);
                } finally {

                        try {
                                socket.close();
                        } catch (Exception ignored) {}

                        synchronized(Server.class) {

                                if (counted) { // FIX: only decrement if counted
                                        Server.currentClients--;
                                }

                                System.out.println("Active clients: " + Server.currentClients);
                        }
                }
        }

        private void uploadFile(DataInputStream in , String username, String userDir) throws Exception {

                String name = in.readUTF();
                in.readLong();

                FileOutputStream fos = new FileOutputStream(userDir + "/" + name);

                Cipher decCipher = Cipher.getInstance("AES");
                SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "AES");

                decCipher.init(Cipher.DECRYPT_MODE, key);

                while (true) {

                        int len = in.readInt();

                        if (len == -1)
                                break;

                        byte[] enc = new byte[len];
                        in.readFully(enc);

                        byte[] dec = decCipher.update(enc);

                        if (dec != null)
                                fos.write(dec);
                }

                byte[] finalDec = decCipher.doFinal();

                if (finalDec != null)
                        fos.write(finalDec);

                fos.close();

                System.out.println(username + " uploaded " + name);
        }

        private void listFiles(DataOutputStream out, String userDir) throws Exception {

                File folder = new File(userDir);
                File[] files = folder.listFiles();

                if (files == null || files.length == 0) {

                        out.writeInt(0);
                        return;
                }

                out.writeInt(files.length);

                for (File f: files)
                        out.writeUTF(f.getName());
        }

        private void downloadFile(DataInputStream in , DataOutputStream out, String userDir) throws Exception {

                String fname = in.readUTF();

                File file = new File(userDir + "/" + fname);

                if (!file.exists()) {

                        out.writeLong(-1);
                        return;
                }

                out.writeLong(file.length());

                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec encKey = new SecretKeySpec(KEY.getBytes(), "AES");

                cipher.init(Cipher.ENCRYPT_MODE, encKey);

                FileInputStream fis = new FileInputStream(file);

                byte[] buffer = new byte[BUFFER];
                int bytes;

                while ((bytes = fis.read(buffer)) != -1) {

                        byte[] enc = cipher.update(buffer, 0, bytes);

                        if (enc != null) {

                                out.writeInt(enc.length);
                                out.write(enc);
                        }
                }

                byte[] finalEnc = cipher.doFinal();

                if (finalEnc != null) {

                        out.writeInt(finalEnc.length);
                        out.write(finalEnc);
                }

                out.writeInt(-1);

                fis.close();
	}
}
