package etf.unibl.org.java;

import java.io.File;

public class EncryptAndDecrypt {

    // Enkripcija javnim ključem
    public static void encrypt(String sender, String recipient, String date, String destinationDir) {
        String inputFile = "efs/" + sender + "/metapodaci.txt";
        String outputFile;
        String keyFile;

        if (destinationDir == null) {
            outputFile = "efs/shared/" + recipient + "/from_" + sender + "/envelopa.txt";
            keyFile = "keys/" + recipient + "Pub.key";
        } else {
            outputFile = "efs/" + sender + "/" + destinationDir + "/envelopa.txt";
            keyFile = "keys/" + sender + "Pub.key";
        }


        String command = "openssl rsautl -encrypt -in " + inputFile +
                " -out " + outputFile +
                " -inkey " + keyFile + " -pubin";

        try {
            OpensslCMD.izvrsi(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dekripcija privatnim ključem
    public static void decrypt(User user, String folderName, String destinationPath, boolean isShared) {
        String inputFile;
        String outputFile;

        if (isShared) {
            inputFile = "efs/shared/" + user.getUsername() + "/" + folderName + "/envelopa.txt";
            outputFile = destinationPath + "/envelopa.txt";
        } else {
            inputFile = "efs/" + user.getUsername() + "/" + folderName + "/envelopa.txt";

            outputFile = destinationPath + "/envelopa.txt";
        }

        File output = new File(outputFile);
        File parentDir = output.getParentFile();

        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) {
                System.out.println("📁 Folder kreiran: " + parentDir.getPath());
            } else {
                System.out.println("⚠️ Greška pri kreiranju foldera: " + parentDir.getPath());
            }
        }

        String command = "openssl rsautl -decrypt -in " + inputFile +
                " -out " + outputFile +
                " -inkey keys/" + user.getUsername() + "Priv.pem" +
                " -passin pass:" + user.getPassword();

        try {
            OpensslCMD.izvrsi(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
