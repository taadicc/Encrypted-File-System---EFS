package etf.unibl.org.java;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
public class CertificateManager {

    public static final String CONFIG_PATH = "./openssl.cnf";

    public static void createCertificate(User user) {
        String username = user.getUsername();
        String email = username + "@email.com";

        String privKey = "./keys/" + username + "Priv.pem";
        String csr = "./requests/" + username + "Request.csr";
        String certOut = "./certs/" + username + ".crt";

        // Koristi se user.getCommonName() umesto username
        String subject = "/C=BA/ST=RS/L=Banjaluka/O=Elektrotehnicki fakultet/OU=ETF/CN="
                + user.getCommonName() + "/emailAddress=" + email;

        try {
            //System.out.println("Generisanje privatnog ključa za korisnika: " + username);
            KeysOp.genrsaPrivateKey(user);
            //System.out.println("Generisanje CSR za korisnika: " + username);
            OpensslCMD.izvrsi(String.format("openssl req -new -key %s -passin pass:%s -out %s -subj \"%s\"", privKey, username, csr, subject));

            //System.out.println("Izvršavanje OpenSSL komande za izdavanje sertifikata");
            OpensslCMD.izvrsi(String.format("openssl ca -in %s -out %s -config %s -passin pass:sigurnost -batch", csr, certOut, CONFIG_PATH));

            File certFile = new File(certOut);
            if (certFile.exists()) {
                System.out.println("Sertifikat uspješno generisan: " + certFile.getAbsolutePath());
            } else {
                System.out.println("Sertifikat nije generisan.");
            }

        } catch (Exception e) {
            System.err.println("Greška prilikom generisanja sertifikata: " + e.getMessage());
        }
    }

}
