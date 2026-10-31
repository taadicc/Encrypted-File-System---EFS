package etf.unibl.org.java;

public class KeysOp {

    public static void genrsaPrivateKey(User user) {
        String path = "./keys/" + user.getUsername() + "Priv.pem";
        String command = String.format("openssl genrsa -des3 -passout pass:%s -out %s 2048", user.getPassword(), path);
        OpensslCMD.izvrsi(command);
        rsaPublicKey(user);
    }

    public static void rsaPublicKey(User user) {
        String inPath = "keys/" + user.getUsername() + "Priv.pem";
        String outPath = "keys/" + user.getUsername() + "Pub.key";
        String command = String.format("openssl rsa -in %s -passin pass:%s -pubout -out %s", inPath, user.getPassword(), outPath);
        OpensslCMD.izvrsi(command);
    }
}
