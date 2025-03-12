package code;

import java.io.Serializable;

public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemType;  // "Crop" or "Animal"
    private String itemName;
    private int originalPrice;
    private int salePrice;
    private long endTime;

    public Sale(String itemType, String itemName, int originalPrice) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.originalPrice = originalPrice;
        this.salePrice = originalPrice / 2;  // 50% off
        this.endTime = System.currentTimeMillis() + 90000; // Current time + 90 seconds
    }

    public boolean isActive() {
        return System.currentTimeMillis() < endTime;
    }

    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    // Getters
    public String getItemType() { return itemType; }
    public String getItemName() { return itemName; }
    public int getOriginalPrice() { return originalPrice; }
    public int getSalePrice() { return salePrice; }
}