package etf.unibl.org.java;

import java.io.File;

public class Hash {

    public static final String[] SUPPORTED_ALGORITHMS = {"-sha512", "-sha256", "-sha384"};
    private static final String DEFAULT_ALGORITHM = "-sha512";

    public static String computeHash(String text) {
        return executeHashCommand(text, DEFAULT_ALGORITHM);
    }

    public static boolean isHashMatching(String expectedHash, String text) {
        String actualHash = computeHash(text);
        return expectedHash != null && expectedHash.equals(actualHash);
    }

    private static String executeHashCommand(String text, String algorithm) {
        File tempFile = FileManager.createTemporaryFile(text);
        if (tempFile == null) {
            System.err.println("❌ Neuspjelo kreiranje privremenog fajla za heširanje.");
            return null;
        }

        String command = "openssl dgst " + algorithm + " " + tempFile.getAbsolutePath();
        String output = OpensslCMD.izvrsi(command);

        if (output == null || !output.contains(" ")) {
            System.err.println("⚠️ Neispravan rezultat OpenSSL komande: " + output);
            return null;
        }

        String[] parts = output.trim().split(" ");
        return parts.length > 1 ? parts[1] : null;
    }
}
