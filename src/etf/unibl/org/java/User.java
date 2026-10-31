package etf.unibl.org.java;

public class User {
    private String username;
    private String password;
    private String commonName;
    private String email;

    public User() {}

    public User(String username, String password, String commonName, String email) {
        this.username = username;
        this.password = password;
        this.commonName = commonName;
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getCommonName() {
        return commonName;
    }
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
