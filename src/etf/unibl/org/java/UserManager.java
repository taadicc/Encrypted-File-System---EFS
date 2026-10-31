package etf.unibl.org.java;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

public class UserManager {

    public static void registracija() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Registracija\n---------------");

        String username;
        do {
            System.out.print("Username: ");
            username = scanner.nextLine().trim();
        } while (zauzetUsername(username) && printZauzet());

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Common Name: ");
        String commonName = scanner.nextLine().trim();

        User user = new User(username, password, commonName, username + "@email.com");

        CertificateManager.createCertificate(user);
        registrujKorisnika(user);
        FileManager.createUserHomeDirectory(user.getUsername());

        System.out.println("Uspješno ste se registrovali!\n");
    }

    private static boolean printZauzet() {
        System.out.println("Zauzet username! Pokušajte ponovo.");
        return true;
    }

    public static void registrujKorisnika(User user) {
        String podaci = String.join(":", user.getUsername(), user.getPassword(), user.getCommonName(), user.getEmail());
        String hash = Hash.computeHash(podaci);

        FileManager.saveTextToFile(hash, "users.txt", true);
        FileManager.saveTextToFile(user.getUsername(), "registered.txt", true);
    }

    public static User prijava() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Prijava\n---------------");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Naziv sertifikata (npr. korisnik.crt): ");
        String certFilename = scanner.nextLine().trim();
        File certFile = new File("certs/" + certFilename);

        if (!certFile.exists()) {
            System.out.println("❌ Sertifikat ne postoji!");
            return null;
        }

        System.out.print("Common Name: ");
        String commonName = scanner.nextLine().trim();

        User user = new User(username, password, commonName, username + "@email.com");

        // Validacija sertifikata
        if (!ValidationCertificate.isVerificateByCA(user)) {
            System.out.println("❌ Sertifikat nije izdat od strane rootCA!");
            return null;
        }

        if (ValidationCertificate.isExpired(user)) {
            System.out.println("❌ Sertifikat je istekao!");
            return null;
        }

        if (ValidationCertificate.isRevoked(user)) {
            System.out.println("❌ Sertifikat je povučen!");
            return null;
        }

        if (!ValidationCertificate.isUsageValid(user)) {
            System.out.println("❌ Sertifikat ne zadovoljava definisanu politiku!");
            return null;
        }

        if (!ValidationCertificate.isCommonNameMatching(user, commonName)) {
            System.out.println("❌ Common Name ne odgovara onom iz sertifikata!");
            return null;
        }

        if (provjeraKorisnika(user)) {
            System.out.println("✅ Uspješna prijava!\n");
            return user;
        }

        System.out.println("❌ Neuspješna prijava! Podaci nisu ispravni.\n");
        return null;
    }


    public static boolean provjeraKorisnika(User user) throws Exception {
        String podaci = String.join(":", user.getUsername(), user.getPassword(), user.getCommonName(), user.getEmail());
        List<String> hashovi = Files.readAllLines(new File("users.txt").toPath());
        return hashovi.stream().anyMatch(hash -> Hash.isHashMatching(hash, podaci));
    }

    public static boolean zauzetUsername(String username) throws Exception {
        List<String> korisnici = Files.readAllLines(new File("registered.txt").toPath());
        return korisnici.contains(username);
    }
}
