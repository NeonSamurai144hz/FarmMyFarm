package code;

public class GameEconomy {
    private static int playerMoney = 500;
    private static MainGameController gameController;

    public static void setGameController(MainGameController controller) {
        gameController = controller;
    }

    public static int getPlayerMoney() {
        return playerMoney;
    }

    public static void addMoney(int amount) {
        playerMoney += amount;
        if (gameController != null) {
            gameController.setMoney(playerMoney);
        }
    }

    public static boolean spendMoney(int amount) {
        if (playerMoney >= amount) {
            playerMoney -= amount;
            if (gameController != null) {
                gameController.setMoney(playerMoney);
            }
            return true;
        }
        return false;
    }

    public static final int SELL_PRICE = 5;
    public static final int BUY_PRICE = 5;
}