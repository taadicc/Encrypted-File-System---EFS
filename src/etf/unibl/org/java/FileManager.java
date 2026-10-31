package etf.unibl.org.java;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class FileManager {

    public static void saveTextToFile(String text, String path, boolean append) {
        try {
            Path filePath = Paths.get(path);
            OpenOption option = append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
            Files.writeString(filePath, text + System.lineSeparator(), StandardOpenOption.CREATE, option);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static File createTemporaryFile(String content) {
        try {
            Path tempPath = Files.createTempFile("tempText", ".txt");
            Files.writeString(tempPath, content, StandardOpenOption.TRUNCATE_EXISTING);
            return tempPath.toFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void createUserHomeDirectory(String username) {
        createDirectoryIfNotExists("efs/" + username);
    }

    public static void createUserSubdirectory(String relativePath) {
        String fullPath = "efs/" + relativePath;
        createDirectoryIfNotExists(fullPath);
        System.out.println("✅ Direktorijum kreiran: " + fullPath);
    }

    private static void createDirectoryIfNotExists(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            System.err.println("⚠️ Greška pri kreiranju direktorijuma: " + e.getMessage());
        }
    }

    public static void displayDirectoryTree(String rootPath, String indent) {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            System.out.println("📂 [PRAZNO] - " + path.toString());
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    System.out.println(indent + "📁 " + entry.getFileName());
                    displayDirectoryTree(entry.toString(), indent + "   ");
                } else {
                    System.out.println(indent + "📄 " + entry.getFileName());
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Greška pri prikazu sadržaja: " + e.getMessage());
        }
    }

    public static void deleteUserDirectory(String relativePath) throws Exception {
        Path dirPath = Paths.get("efs", relativePath);
        if (!Files.exists(dirPath)) {
            throw new FileNotFoundException("Direktorijum ne postoji.");
        }

        deleteRecursively(dirPath.toFile());
        System.out.println("🗑️ Obrisan direktorijum: " + dirPath);
    }

    private static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Neuspješno brisanje: " + file.getPath());
        }
    }

    public static boolean isAllowedFileType(String fileName) {
        return Arrays.stream(new String[]{".txt", ".pdf", ".jpg", ".jpeg", ".png"})
                .anyMatch(fileName::endsWith);
    }

    public static void saveMetadataFile(String dgstAlg, String symmAlg, String symmKey, String username) throws IOException {
        String metadata = String.join(":", dgstAlg, symmAlg, symmKey, username);
        Path metadataPath = Paths.get("efs", username, "metapodaci.txt");
        Files.writeString(metadataPath, metadata + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String[] readEnvelopeMetadata(String username, String fileSubDir, String remoteHostPath) {
        String pathStr = (fileSubDir != null)
                ? Paths.get("efs", username, fileSubDir, "envelopa.txt").toString()
                : Paths.get(remoteHostPath, "envelopa.txt").toString();

        try (BufferedReader reader = new BufferedReader(new FileReader(pathStr))) {
            String line = reader.readLine();
            return (line != null) ? line.split(":") : new String[0];
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String locateEncryptedFile(String relativeFolderPath) {
        File folder = new File("efs", relativeFolderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("❌ Folder ne postoji ili nije validan.");
            return null;
        }

        File[] encryptedFiles = folder.listFiles((dir, name) -> name.endsWith(".enc"));
        return (encryptedFiles != null && encryptedFiles.length > 0) ? encryptedFiles[0].getName() : null;
    }
}
