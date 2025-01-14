class Transaction {
    private String type;
    private String category;
    private double amount;

    public Transaction(String type, String category, double amount) {
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s - Категория: %s, Сумма: %.2f",
                type.equals("income") ? "Доход" : "Расход", category, amount);
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

}
