package network;

public class RegisterData extends LoginCredentials {
    private String role;

    public RegisterData(String username, String password, String role) {
        super(username, password);
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
