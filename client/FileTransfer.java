import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Scanner;

public class FileTransfer {

    private static final String KEY = "1234567890123456";
    private static final int BUFFER = 4096;
    private static Scanner sc = new Scanner(System.in);

    public static void upload(DataOutputStream out) throws Exception {

        out.writeInt(1);
        long transferred = 0;  // start at 0

        System.out.print("File path: ");
        String path = sc.nextLine();

        if (path.startsWith("~"))
            path = System.getProperty("user.home") + path.substring(1);

        File file = new File(path);

        if (!file.exists()) {
            System.out.println("File not found.");
            return;
        }

        out.writeUTF(file.getName());
        out.writeLong(file.length());
        long totalBytes = file.length();

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
        System.out.println("Upload complete.");
    }

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

    System.out.print("\nSelect file number: ");
    int fileChoice = Integer.parseInt(sc.nextLine());
    if (fileChoice < 1 || fileChoice > fileCount) return;

    String selected = files[fileChoice - 1];
    out.writeInt(3);
    out.writeUTF(selected);   // ✅ send filename

    long size = in.readLong();   // ✅ receive file size
    if (size == -1) {
        System.out.println("File not found.");
        return;
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
            System.out.print("\rDownload progress " + percent + "%");
        }
    }

    byte[] finalDec = cipher.doFinal();
    if (finalDec != null) {
        fos.write(finalDec);
        transferred += finalDec.length;
    }

    fos.close();

    System.out.println("\rDownload progress 100%");
    System.out.println("Download complete.");
}
}
