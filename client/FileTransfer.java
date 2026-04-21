import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import java.io.*;
import java.util.Scanner;

public class FileTransfer {

    private static final String KEY = "1234567890123456";
    private static final int BUFFER = 4096;
    private static Scanner sc = new Scanner(System.in);

    // ---------------- SINGLE-FILE UPLOAD ----------------
    public static void upload(DataOutputStream out) throws Exception {
        out.writeInt(1);

        // Use JFileChooser for single file selection
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to upload");

        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected.");
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.exists()) {
            System.out.println("File not found.");
            return;
        }

        out.writeUTF(file.getName());
        out.writeLong(file.length());
        long totalBytes = file.length();
        long transferred = 0;

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER];
        int bytes;

        while ((bytes = fis.read(buffer)) != -1) {
            byte[] enc = cipher.update(buffer, 0, bytes);
            if (enc != null) {
                out.writeInt(enc.length);
                out.write(enc);
                transferred += enc.length;
                int percent = (int)((transferred * 100) / totalBytes);
                System.out.print("\rUpload progress " + percent + "%");
            }
        }

        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            out.writeInt(finalBytes.length);
            out.write(finalBytes);
            transferred += finalBytes.length;
        }

        out.writeInt(-1);
        fis.close();

        System.out.println("\rUpload progress 100%");
	double fileSize = (double)totalBytes / (1024 * 1024);
        System.out.printf("Upload complete. File Size : %.2f mb%n.", fileSize);
    }

    // ---------------- LIST FILES ----------------
    public static void listFiles(DataInputStream in, DataOutputStream out) throws Exception {
        out.writeInt(2);
        int count = in.readInt();
        if (count == 0) {
            System.out.println("\nNo files stored.");
            return;
        }
        System.out.println("\nStored Files:\n");
        for (int i = 0; i < count; i++)
            System.out.println("- " + in.readUTF());
    }

    // ---------------- MULTI-FILE DOWNLOAD ----------------
    public static void download(DataInputStream in, DataOutputStream out) throws Exception {
        out.writeInt(2);
        int fileCount = in.readInt();
        if (fileCount == 0) {
            System.out.println("\nNo files available.");
            return;
        }

        String[] files = new String[fileCount];
        System.out.println("\nAvailable Files:\n");
        for (int i = 0; i < fileCount; i++) {
            files[i] = in.readUTF();
            System.out.println((i + 1) + ". " + files[i]);
        }

        System.out.print("\nEnter file numbers to download (comma separated): ");
        String[] choices = sc.nextLine().split(",");

        for (String choiceStr : choices) {
            int fileChoice;
            try {
                fileChoice = Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (fileChoice < 1 || fileChoice > fileCount) continue;

            String selected = files[fileChoice - 1];
            out.writeInt(3);
            out.writeUTF(selected);

            long size = in.readLong();
            if (size == -1) {
                System.out.println("File not found: " + selected);
                continue;
            }

            long transferred = 0;
            FileOutputStream fos = new FileOutputStream(selected);

            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            while (true) {
                int len = in.readInt();
                if (len == -1) break;

                byte[] enc = new byte[len];
                in.readFully(enc);

                byte[] dec = cipher.update(enc);
                if (dec != null) {
                    fos.write(dec);
                    transferred += dec.length;
                    int percent = (int)((transferred * 100) / size);
                    System.out.print("\rDownloading " + selected + ": " + percent + "%");
                }
            }

            byte[] finalDec = cipher.doFinal();
            if (finalDec != null) {
                fos.write(finalDec);
                transferred += finalDec.length;
            }

            fos.close();
            System.out.println("\rDownloading " + selected + ": 100%");
	    double fileSize = (double)size / (1024 * 1024);
            System.out.printf("Upload complete for %s. File Size : %.2f mb%n.", selected, fileSize);
        }
    }
}

