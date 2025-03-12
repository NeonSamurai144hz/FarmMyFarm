package code;

public enum Weather {
    SUNNY(1.0, "☀"),
    RAINY(0.7, "🌧"),
    DROUGHT(1.5, "🏜");

    private final double growthMultiplier;
    private final String symbol;

    Weather(double growthMultiplier, String symbol) {
        this.growthMultiplier = growthMultiplier;
        this.symbol = symbol;
    }

    public double getGrowthMultiplier() { return growthMultiplier; }
    public String getSymbol() { return symbol; }
}