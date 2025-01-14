import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.reflect.TypeToken;

class FinanceManager {
    private Map<String, List<Transaction>> userFinances = new HashMap<>();
    private Map<String, Double> categoryBudgets = new HashMap<>();
    private static final String FINANCES_FILE = "finances.json";
    private static final List<String> INCOME_CATEGORIES = Arrays.asList(
            "Зарплата", "Инвестиции", "Переводы от других людей", "Прочее"
    );

    private static final List<String> EXPENSE_CATEGORIES = Arrays.asList(
            "Еда", "Транспорт", "Жилье", "Связь и интернет",
            "Одежда", "Здоровье", "Развлечения", "Образование",
            "Переводы другим людям", "Прочее"
    );


    public FinanceManager() {
        loadFinances();
    }

    public void userMenu(Scanner scanner, User user, AuthManager authManager) {
        boolean isLoggedIn = true;
        while (isLoggedIn) {
            System.out.println("\nМеню управления финансами:");
            System.out.println("1. Просмотреть финансы");
            System.out.println("2. Добавить доход");
            System.out.println("3. Добавить расход");
            System.out.println("4. Перевести деньги другому пользователю");
            System.out.println("5. Просмотр и управление бюджетами");
            System.out.println("6. Выйти");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewFinances(user);
                    break;
                case "2":
                    addIncome(scanner, user);
                    saveFinances();
                    break;
                case "3":
                    addExpense(scanner, user);
                    saveFinances();
                    break;
                case "4":
                    transferMoney(scanner, user, authManager);
                    saveFinances();
                    break;
                case "5":
                    manageBudgets(scanner);
                    saveFinances();
                    break;
                case "6":
                    System.out.println("Выход в главное меню.");
                    isLoggedIn = false;
                    break;
                default:
                    System.out.println("Ошибка: выберите корректное действие.");
            }
        }
    }

    private void viewFinances(User user) {
        List<Transaction> finances = userFinances.getOrDefault(user.getUsername(), new ArrayList<>());

        double totalIncome = finances.stream()
                .filter(t -> t.getType().equals("income"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = finances.stream()
                .filter(t -> t.getType().equals("expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double balance = totalIncome - totalExpense;
        System.out.printf("\nВаш текущий баланс: %.2f%n", balance);

        if (finances.isEmpty()) {
            System.out.println("Нет записей о финансах.");
            return;
        }

        Map<String, Double> incomeSummary = new LinkedHashMap<>();
        Map<String, Double> expenseSummary = new LinkedHashMap<>();

        for (Transaction transaction : finances) {
            if (transaction.getType().equals("income")) {
                incomeSummary.merge(transaction.getCategory(), transaction.getAmount(), Double::sum);
            } else {
                expenseSummary.merge(transaction.getCategory(), transaction.getAmount(), Double::sum);
            }
        }

        System.out.printf("Доходы (Всего: %.2f):%n", totalIncome);
        if (incomeSummary.isEmpty()) {
            System.out.println("  Нет доходов.");
        } else {
            incomeSummary.forEach((category, total) ->
                    System.out.printf("  %s: %.2f%n", category, total)
            );
        }

        System.out.printf("Расходы (Всего: %.2f):%n", totalExpense);
        if (expenseSummary.isEmpty()) {
            System.out.println("  Нет расходов.");
        } else {
            expenseSummary.forEach((category, total) -> {
                double budget = categoryBudgets.getOrDefault(category, 0.0);
                double remaining = budget - total;
                String status = remaining >= 0 ? "Остаток" : "Превышение";
                System.out.printf("  %s: %.2f (Бюджет: %.2f, %s: %.2f)%n",
                        category, total, budget, status, Math.abs(remaining));
            });
        }
    }

    private void addIncome(Scanner scanner, User user) {
        System.out.println("Выберите категорию дохода:");
        for (int i = 0; i < INCOME_CATEGORIES.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, INCOME_CATEGORIES.get(i));
        }
        System.out.print("Введите номер категории: ");
        int categoryIndex = Integer.parseInt(scanner.nextLine()) - 1;

        if (categoryIndex < 0 || categoryIndex >= INCOME_CATEGORIES.size()) {
            System.out.println("Ошибка: некорректный выбор категории.");
            return;
        }
        String category = INCOME_CATEGORIES.get(categoryIndex);

        System.out.print("Введите сумму дохода: ");
        double amount = Double.parseDouble(scanner.nextLine());

        Transaction transaction = new Transaction("income", category, amount);
        userFinances.computeIfAbsent(user.getUsername(), k -> new ArrayList<>()).add(transaction);

        System.out.println("Доход добавлен успешно.");
    }

    private void addExpense(Scanner scanner, User user) {
        System.out.println("Выберите категорию расхода:");
        for (int i = 0; i < EXPENSE_CATEGORIES.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, EXPENSE_CATEGORIES.get(i));
        }
        System.out.print("Введите номер категории: ");
        int categoryIndex = Integer.parseInt(scanner.nextLine()) - 1;

        if (categoryIndex < 0 || categoryIndex >= EXPENSE_CATEGORIES.size()) {
            System.out.println("Ошибка: некорректный выбор категории.");
            return;
        }
        String category = EXPENSE_CATEGORIES.get(categoryIndex);

        System.out.print("Введите сумму расхода: ");
        double amount = Double.parseDouble(scanner.nextLine());

        Transaction transaction = new Transaction("expense", category, amount);
        userFinances.computeIfAbsent(user.getUsername(), k -> new ArrayList<>()).add(transaction);

        System.out.println("Расход добавлен успешно.");
    }

    private void transferMoney(Scanner scanner, User sender, AuthManager authManager) {
        System.out.print("Введите логин получателя: ");
        String recipientUsername = scanner.nextLine();

        if (recipientUsername.equals(sender.getUsername())) {
            System.out.println("Ошибка: нельзя переводить деньги самому себе.");
            return;
        }

        User recipient = authManager.getUserByUsername(recipientUsername);
        if (recipient == null) {
            System.out.println("Ошибка: пользователь с таким логином не найден.");
            return;
        }

        System.out.print("Введите сумму перевода: ");
        double amount = Double.parseDouble(scanner.nextLine());

        if (amount <= 0) {
            System.out.println("Ошибка: сумма перевода должна быть больше нуля.");
            return;
        }

        // Записываем расход у отправителя
        Transaction senderTransaction = new Transaction("expense", "Переводы другим людям", amount);
        userFinances.computeIfAbsent(sender.getUsername(), k -> new ArrayList<>()).add(senderTransaction);

        // Записываем доход у получателя
        Transaction recipientTransaction = new Transaction("income", "Переводы от других людей", amount);
        userFinances.computeIfAbsent(recipientUsername, k -> new ArrayList<>()).add(recipientTransaction);

        System.out.printf("Успешный перевод %.2f от %s к %s.%n",
                amount, sender.getUsername(), recipient.getUsername());
    }

    public void manageBudgets(Scanner scanner) {
        // Существующая логика управления бюджетами
    }

    private void loadFinances() {
        Type financeType = new TypeToken<Map<String, List<Transaction>>>() {}.getType();
        Map<String, List<Transaction>> loadedFinances = FileManager.loadFromFile(FINANCES_FILE, financeType);
        if (loadedFinances != null) {
            userFinances = loadedFinances;
        }
    }

    private void saveFinances() {
        FileManager.saveToFile(FINANCES_FILE, userFinances);
    }
}
