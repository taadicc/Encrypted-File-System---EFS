package etf.unibl.org.java;

import java.io.File;

public class Signature {

    public static void sign(User sender, String receiver, String alg, String filePath, String pom, String signatureFileName, String destinationDir) {
        String outputDir;
        String fileToSign;
        String outputSignature;

        String fileName = new File(filePath).getName();

        if (receiver != null) {
            outputDir = String.join("/", "efs", "shared", receiver, "from_" + sender.getUsername());
        } else {
            outputDir = String.join("/", "efs", sender.getUsername(), destinationDir);
        }

        fileToSign = String.join("/", outputDir, fileName + ".enc");
        outputSignature = String.join("/", outputDir, signatureFileName);

        String command = String.format(
                "openssl dgst %s -sign keys/%sPriv.pem -passin pass:%s -out %s %s",
                alg, sender.getUsername(), sender.getPassword(), outputSignature, fileToSign
        );

        try {
            OpensslCMD.izvrsi(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String verifySign(String sender, String receiver, String sharedFolderName, String alg, String userDir, String encryptedFileName) {
        String dirPath;
        String fileToVerify;
        String signatureFile;
        String senderUsername;

        if (receiver != null) {
            // Dijeljeni direktorijum
            dirPath = String.join("/", "efs", "shared", receiver, sharedFolderName);
            senderUsername = sharedFolderName.replace("from_", "");
        } else {
            // Home direktorijum
            dirPath = String.join("/", "efs", sender, sharedFolderName);  // Prvo korisničko ime, pa naziv foldera
            senderUsername = sender;  // Direktno korisničko ime, bez dodatnih dijelova u putanji
        }

        // Putanje za fajl koji treba verifikovati i potpis
        fileToVerify = String.join("/", dirPath, encryptedFileName);
        signatureFile = String.join("/", dirPath, "digitalniPotpis.sgn");

        // Kreiranje komande za OpenSSL
        String command = String.format(
                "openssl dgst %s -verify keys/%sPub.key -signature %s %s",
                alg, senderUsername, signatureFile, fileToVerify
        );

        // Ispis za debug
        //System.out.println("TEST:");
        //System.out.println("DirPath: " + dirPath);
        //System.out.println("Sender: " + senderUsername);
        //System.out.println("File: " + fileToVerify);
        //System.out.println("Signature: " + signatureFile);
        //System.out.println("Command: " + command);

        try {
            return OpensslCMD.izvrsi(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }
}
