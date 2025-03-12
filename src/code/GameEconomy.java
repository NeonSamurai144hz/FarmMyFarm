package code;

public class GameEconomy {
    private static int playerMoney = 500;   //starting money
    public static final int SELL_PRICE = 10; //in the name
    public static final int BUY_PRICE = 5;  //in the name
    private static MainGameController gameController;

    public static void setGameController(MainGameController controller) {
        gameController = controller;
    }

    public static int getPlayerMoney() {
        return playerMoney;
    }

    public static void setPlayerMoney(int amount) {
        playerMoney = amount;
        if (gameController != null) {
            gameController.setMoney(playerMoney);
        }
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
}