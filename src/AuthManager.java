import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.google.gson.reflect.TypeToken;

class AuthManager {
    private Map<String, User> users = new HashMap<>();
    private User currentUser;
    private static final String USERS_FILE = "users.json";

    public AuthManager() {
        loadUsers();
    }

    // Регистрация нового пользователя
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        String passwordHash = hashPassword(password);
        users.put(username, new User(username, passwordHash));
        saveUsers();
        return true;
    }

    // Авторизация пользователя
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            currentUser = user;
            return true;
        }
        return false;
    }

    // Получение текущего пользователя
    public User getCurrentUser() {
        return currentUser;
    }

    // Получение пользователя по логину
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    // Загрузка пользователей из файла
    private void loadUsers() {
        Type userType = new TypeToken<Map<String, User>>() {}.getType();
        Map<String, User> loadedUsers = FileManager.loadFromFile(USERS_FILE, userType);
        if (loadedUsers != null) {
            users = loadedUsers;
        }
    }

    // Сохранение пользователей в файл
    private void saveUsers() {
        FileManager.saveToFile(USERS_FILE, users);
    }

    // Хэширование пароля
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при хэшировании пароля", e);
        }
    }
}
