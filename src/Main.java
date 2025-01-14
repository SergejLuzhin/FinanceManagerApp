import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthManager authManager = new AuthManager();
        FinanceManager financeManager = new FinanceManager();

        System.out.println("Добро пожаловать в приложение для управления личными финансами!");

        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\nМеню:");
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("3. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("Введите логин: ");
                    String login = scanner.nextLine();
                    System.out.print("Введите пароль: ");
                    String password = scanner.nextLine();

                    if (authManager.login(login, password)) {
                        System.out.println("Успешный вход!\n");
                        User currentUser = authManager.getCurrentUser();
                        financeManager.userMenu(scanner, currentUser, authManager);
                    } else {
                        System.out.println("Ошибка: неверный логин или пароль.");
                    }
                    break;
                case "2":
                    System.out.print("Введите логин: ");
                    String newLogin = scanner.nextLine();
                    System.out.print("Введите пароль: ");
                    String newPassword = scanner.nextLine();

                    if (authManager.register(newLogin, newPassword)) {
                        System.out.println("Регистрация успешна! Теперь войдите в систему.");
                    } else {
                        System.out.println("Ошибка: пользователь с таким логином уже существует.");
                    }
                    break;
                case "3":
                    System.out.println("Выход из приложения. До свидания!");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Ошибка: выберите корректное действие.");
            }
        }

        scanner.close();
    }
}
