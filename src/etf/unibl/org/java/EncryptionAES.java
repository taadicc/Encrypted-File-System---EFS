package etf.unibl.org.java;

import java.io.File;

public class EncryptionAES {

    public static final String[] ALGORITHMS = { "-aes128", "-aes-128-cbc", "-aes-128-cfb" };

    public static String generateSymmetricKey() {
        return OpensslCMD.izvrsi("openssl rand -base64 32").trim();
    }

    public static String encryptFile(String inputPath, User user, String algorithm, String receiver, String date, String destinationFolder) {
        String key = generateSymmetricKey();

        String fileName = new File(inputPath).getName();
        String outputPath = buildOutputPath(user.getUsername(), fileName, receiver, date, destinationFolder);

        createParentFolder(outputPath);

        String command = "openssl enc " + algorithm + " -in " + inputPath + " -out " + outputPath + " -k " + key;

        try {
            OpensslCMD.izvrsi(command);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return key;
    }

    public static void decryptCipher(String username, String folderName, String algorithm, String key, String outputDir, String originalFileName, boolean isShared) {
        String inputPath = isShared
                ? "efs/shared/" + username + "/" + folderName + "/" + originalFileName + ".enc"
                : "efs/" + folderName + "/" + originalFileName + ".enc";

        String outputPath = outputDir + "/" + originalFileName;

        createParentFolder(outputPath);

        String command = "openssl enc " + algorithm + " -d -in " + inputPath + " -out " + outputPath + " -k " + key;

        try {
            OpensslCMD.izvrsi(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildOutputPath(String sender, String fileName, String receiver, String date, String destFolder) {
        if (receiver != null) {
            return "efs/shared/" + receiver + "/from_" + sender + "/" + fileName + ".enc";
        } else {
            return "efs/" + sender + "/" + destFolder + "/" + fileName + ".enc";
        }
    }

    private static void createParentFolder(String filePath) {
        File parent = new File(filePath).getParentFile();
        if (!parent.exists()) {
            if (parent.mkdirs()) {
                System.out.println("📁 Kreiran folder: " + parent.getPath());
            } else {
                System.out.println("⚠️ Greška pri kreiranju foldera: " + parent.getPath());
            }
        }
    }
}
