package backend;

public class AuthManager {

    public static boolean authenticate(String role, String password) {
        return switch (role.toLowerCase()) {
            case "admin" -> password.equals("admin123");
            case "user" -> password.equals("user123");
            default -> false;
        };
    }
}