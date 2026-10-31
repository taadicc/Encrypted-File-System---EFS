package etf.unibl.org.java;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ValidationCertificate {

    public static boolean isVerificateByCA(User user) {
        String certPath = "certs/" + user.getUsername() + ".crt";
        String result = OpensslCMD.izvrsi("openssl verify -CAfile rootca.pem " + certPath);
        return result.contains(": OK");
    }

    public static boolean isExpired(User user) {
        String certPath = "certs/" + user.getUsername() + ".crt";
        String result = OpensslCMD.izvrsi("openssl x509 -in " + certPath + " -noout -checkend 0");

        // Ako rezultat sadrži "Certificate will expire", onda jeste istekao
        // Ako sadrži "Certificate will not expire", onda je validan
        if (result.contains("Certificate will expire")) {
            return true; // istekao
        } else if (result.contains("Certificate will not expire")) {
            return false; // nije istekao
        }
        // fallback - ako ništa ne prepoznamo, tretiramo kao grešku (sigurnosno)
        return true;
    }


    public static boolean isRevoked(User user) {
        String crlPath = "crl/crl.pem";
        File crlFile = new File(crlPath);

        // Ako ne postoji CRL, pretpostavi da nijedan certifikat nije povučen
        if (!crlFile.exists()) {
            return false;
        }

        String certPath = "certs/" + user.getUsername() + ".crt";
        String command = "openssl verify -crl_check -CAfile rootca.pem -CRLfile " + crlPath + " " + certPath;
        String result = OpensslCMD.izvrsi(command);

        return !result.contains(": OK"); // ako je OK, nije povučen
    }


    public static boolean isUsageValid(User user) {
        String certPath = "certs/" + user.getUsername() + ".crt";
        String result = OpensslCMD.izvrsi("openssl x509 -in " + certPath + " -noout -text");
        return result.contains("TLS Web Client Authentication"); // primer upotrebe prema politici
    }

    public static String extractCommonName(String certPath) {
        String result = OpensslCMD.izvrsi("openssl x509 -in " + certPath + " -noout -subject");
        // npr. subject= /C=BA/ST=RS/O=ETF/CN=marko
        if (result.contains("CN=")) {
            return result.substring(result.indexOf("CN=") + 3).split("[\\s,/]+")[0];
        }
        return null;
    }

    public static boolean isCommonNameMatching(User user, String expectedCommonName) {
        String certPath = "certs/" + user.getUsername() + ".crt";
        String actualCN = extractCommonName(certPath);
        return expectedCommonName != null && expectedCommonName.equals(actualCN);
    }


}
