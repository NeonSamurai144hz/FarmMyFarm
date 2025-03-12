package code;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Finance {
    private static ArrayList<Transaction> transactions = new ArrayList<>();

    public static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String itemType;
        private final String itemName;
        private final int amount;
        private final int price;
        private final TransactionType type;
        private final LocalDateTime dateTime;

        public Transaction(String itemType, String itemName, int amount, int price, TransactionType type) {
            this.itemType = itemType;
            this.itemName = itemName;
            this.amount = amount;
            this.price = price;
            this.type = type;
            this.dateTime = LocalDateTime.now();
        }

        public String getItemType() { return itemType; }
        public String getItemName() { return itemName; }
        public int getAmount() { return amount; }
        public int getPrice() { return price; }
        public TransactionType getType() { return type; }
        public LocalDateTime getDateTime() { return dateTime; }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return String.format("%s: %s %s x%d for $%d",
                    dateTime.format(formatter),
                    type, itemName, amount, price);
        }
    }

    public enum TransactionType implements Serializable {
        SELL,
        BUY
    }

    public static void addTransaction(String itemType, String itemName, int amount, int price, TransactionType type) {
        transactions.add(new Transaction(itemType, itemName, amount, price, type));
    }

    public static void recordBuyTransaction(String itemType, String itemName, int amount, int price) {
        addTransaction(itemType, itemName, amount, price, TransactionType.BUY);
    }

    public static void recordSellTransaction(String itemType, String itemName, int amount, int price) {
        addTransaction(itemType, itemName, amount, price, TransactionType.SELL);
    }

    public static ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public static void setTransactions(ArrayList<Transaction> loadedTransactions) {
        transactions = new ArrayList<>(loadedTransactions);
    }

    public static int getTotalSales() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL)
                .mapToInt(t -> t.getAmount() * t.getPrice())  // multiply amount by price
                .sum();
    }

    public static int getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.BUY)
                .mapToInt(t -> t.getAmount() * t.getPrice())  // multiply amount by price
                .sum();
    }

    public static Map<String, Integer> getItemSalesByType() {
        Map<String, Integer> sales = new HashMap<>();
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL)
                .forEach(t -> {
                    String key = t.getItemType();
                    sales.put(key, sales.getOrDefault(key, 0) + t.getPrice());
                });
        return sales;
    }

    public static Map<String, Integer> getItemSalesByName() {
        Map<String, Integer> sales = new HashMap<>();
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL)
                .forEach(t -> {
                    String key = t.getItemName();
                    sales.put(key, sales.getOrDefault(key, 0) + t.getAmount());
                });
        return sales;
    }
}