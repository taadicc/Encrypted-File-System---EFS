package etf.unibl.org.java;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println("""
                    ===================================
                        Dobrodošli u EFS sistem
                    ===================================
                    1. Prijava
                    2. Registracija
                    -----------------------------------
                    Izbor: 
                    """);

            String option = scanner.nextLine().trim();
            User korisnik = null;

            switch (option) {
                //case "1" -> korisnik = UserManager.prijava();
                case "1" -> {
                    korisnik = UserManager.prijava();
                    if (korisnik != null) {
                        // automatski prikaži stablo korisnikovog home direktorijuma
                        System.out.println("\n📁 Vaš home direktorijum:");
                        FileManager.displayDirectoryTree("efs/" + korisnik.getUsername(), "");
                    }
                }
                case "2" -> {
                    UserManager.registracija();
                    continue;
                }
                /*case "0" -> {
                    System.out.println("👋 Doviđenja!");
                    return;
                }
                 */
                default -> {
                    System.out.println("❌ Neispravan izbor. Izlaz...");
                    continue;
                }
            }

            if (korisnik != null) {
                prikaziGlavniMeni(korisnik);
            }
        }
    }

    private static void prikaziGlavniMeni(User user) throws Exception {
        while (true) {
            System.out.println("""
                ------------------------------------
                    GLAVNI MENI
                ------------------------------------
                1. Home direktorijum
                2. Dijeljeni direktorijum
                0. Odjava
                ------------------------------------
                Izbor: 
                """);

            String izbor = scanner.nextLine().trim();
            switch (izbor) {
                case "1" -> meniHomeDirektorijum(user);
                case "2" -> meniSharedDirektorijum(user);
                case "0" -> {
                    System.out.println("✅ Odjavljeni ste!");
                    return;
                }
                default -> System.out.println("❌ Nepoznata opcija.");
            }
        }
    }

    private static void meniHomeDirektorijum(User user) {
        while (true) {
            System.out.println("""
                -------- HOME DIREKTORIJUM --------
                1. Prikaži sadržaj
                2. Dodaj datoteku
                3. Preuzmi datoteku
                4. Kreiraj folder
                5. Obriši folder
                0. Povratak
                -----------------------------------
                Izbor: 
                """);

            String izbor = scanner.nextLine().trim();
            switch (izbor) {
                case "1" -> FileManager.displayDirectoryTree("efs/" + user.getUsername(), "");
                case "2" -> dodajFajlHome(user);
                case "3" -> preuzmiFajlHome(user);
                case "4" -> {
                    System.out.print("Unesite putanju za novi folder: ");
                    String folder = scanner.nextLine();
                    FileManager.createUserSubdirectory(user.getUsername() + File.separator + folder);
                }
                case "5" -> {
                    System.out.print("Unesite folder za brisanje: ");
                    String folder = scanner.nextLine();
                    try {
                        FileManager.deleteUserDirectory(user.getUsername() + File.separator + folder);
                    } catch (Exception e) {
                        System.err.println("⚠️ " + e.getMessage());
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("❗ Nevažeća opcija.");
            }
        }
    }

    private static void dodajFajlHome(User user) {
        try {
            System.out.print("Putanja do lokalne datoteke: ");
            String source = scanner.nextLine();
            String fileName = source.substring(source.lastIndexOf(File.separator) + 1);

            if (!FileManager.isAllowedFileType(fileName)) {
                System.out.println("❌ Nepodržan format fajla (.txt, .pdf, .jpg, .jpeg, .png)");
                return;
            }

            System.out.print("Putanja unutar home direktorijuma za snimanje: ");
            String dest = scanner.nextLine();

            String symmAlg = EncryptionAES.ALGORITHMS[random.nextInt(3)];
            String dgstAlg = Hash.SUPPORTED_ALGORITHMS[random.nextInt(3)];
            String key = EncryptionAES.encryptFile(source, user, symmAlg, null, null, dest);

            Signature.sign(user, null, dgstAlg, source, null, "digitalniPotpis.sgn", dest);
            FileManager.saveMetadataFile(dgstAlg, symmAlg, key, user.getUsername());
            EncryptAndDecrypt.encrypt(user.getUsername(), null, null, dest);

            new File("efs/" + user.getUsername() + "/metapodaci.txt").delete();

            System.out.println("✅ Fajl je uspješno dodat i zaštićen.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preuzmiFajlHome(User user) {
        try {
            System.out.print("Putanja do datoteke (home): ");
            String source = scanner.nextLine();
            //String fullPath = user.getUsername() + File.separator + source;

            System.out.print("Putanja na lokalnom računaru: ");
            String destination = scanner.nextLine();

            EncryptAndDecrypt.decrypt(user, source, destination, false);
            String[] metadata = FileManager.readEnvelopeMetadata(user.getUsername(), null, destination);
            String fullPath=source;
            String fullSourceHome = user.getUsername() + File.separator + source;
            String encryptedFile = FileManager.locateEncryptedFile(fullSourceHome);

            String folderOnly = source.contains(File.separator) ?
                    source.substring(0, source.lastIndexOf(File.separator)) : "";
            if ("Verified OK".equals(Signature.verifySign(user.getUsername(), null, source, metadata[0], fullPath, encryptedFile))) {
                System.out.println("✅ Verifikacija potpisa uspješna.");
                EncryptionAES.decryptCipher(user.getUsername(), fullSourceHome, metadata[1], metadata[2], destination, encryptedFile.split(".enc")[0], false);
            } else {
                System.out.println("❌ Digitalni potpis nije validan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void meniSharedDirektorijum(User user) throws Exception {
        while (true) {
            System.out.println("""
                ------- SHARED DIREKTORIJUM -------
                1. Prikaži sadržaj
                2. Dodaj datoteku za drugog korisnika
                3. Preuzmi datoteku
                0. Povratak
                -----------------------------------
                Izbor:
                """);

            String izbor = scanner.nextLine().trim();
            switch (izbor) {
                case "1" -> FileManager.displayDirectoryTree("efs/shared", "");
                case "2" -> dodajFajlShared(user);
                case "3" -> preuzmiFajlShared(user);
                case "0" -> { return; }
                default -> System.out.println("❗ Nevažeća opcija.");
            }
        }
    }

    private static void dodajFajlShared(User user) {
        try {
            System.out.print("Korisničko ime primaoca: ");
            String receiver = scanner.nextLine();

            if (!UserManager.zauzetUsername(receiver) || receiver.equals(user.getUsername())) {
                System.out.println("❌ Nevalidno korisničko ime primaoca.");
                return;
            }

            System.out.print("Putanja do fajla: ");
            String file = scanner.nextLine();
            String folderName = "from_" + user.getUsername();

            String symmAlg = EncryptionAES.ALGORITHMS[random.nextInt(3)];
            String dgstAlg = Hash.SUPPORTED_ALGORITHMS[random.nextInt(3)];
            String key = EncryptionAES.encryptFile(file, user, symmAlg, receiver, folderName, null);

            Signature.sign(user, receiver, dgstAlg, file, folderName, "digitalniPotpis.sgn", null);
            FileManager.saveMetadataFile(dgstAlg, symmAlg, key, user.getUsername());
            EncryptAndDecrypt.encrypt(user.getUsername(), receiver, folderName, null);

            System.out.println("✅ Fajl uspješno dodat i šifrovan za korisnika: " + receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preuzmiFajlShared(User user) {
        try {
            System.out.print("Unesite naziv fajla u shared direktorijumu: ");
            String file = scanner.nextLine();

            System.out.print("Putanja na lokalni sistem: ");
            String destination = scanner.nextLine();

            String sender = file.split("_")[0];
            String fullPath = "shared" + File.separator + user.getUsername() + File.separator + file;
            String encFile = FileManager.locateEncryptedFile(fullPath);

            EncryptAndDecrypt.decrypt(user, file, destination, true);
            String[] metadata = FileManager.readEnvelopeMetadata(user.getUsername(), null, destination);

            if ("Verified OK".equals(Signature.verifySign(sender, user.getUsername(), file, metadata[0], null, encFile))) {
                System.out.println("✅ Verifikacija potpisa uspješna.");
                EncryptionAES.decryptCipher(user.getUsername(), file, metadata[1], metadata[2], destination, encFile.split(".enc")[0], true);
            } else {
                System.out.println("❌ Digitalni potpis nije validan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}