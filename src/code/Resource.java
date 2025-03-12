package code;

public enum Resource {
    MILK(15),    // From Cow
    EGGS(8),     // From Chicken
    BACON(12);   // From Pig

    private final int sellPrice;

    Resource(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public static Resource getResourceForAnimal(AnimalType animalType) {
        switch (animalType) {
            case COW: return MILK;
            case CHICKEN: return EGGS;
            case PIG: return BACON;
            default: return null;
        }
    }
}