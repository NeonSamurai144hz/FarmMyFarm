package code;

import java.io.Serializable;

public class PlayerLevel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int level;
    private int currentExp;
    private int expToNextLevel;
    private static MainGameController gameController;

    public PlayerLevel() {
        this.level = 1;
        this.currentExp = 0;
        this.expToNextLevel = 100; // Base exp needed
    }

    public static void setGameController(MainGameController controller) {
        gameController = controller;
    }

    public void addExp(int exp) {
        currentExp += exp;
        while (currentExp >= expToNextLevel) {
            levelUp();
        }
        updateHUD();
    }

    private void levelUp() {
        level++;
        currentExp -= expToNextLevel;
        expToNextLevel = 100 * level; // Each level requires more exp
        if (gameController != null) {
            showLevelUpNotification();
        }
    }

    private void showLevelUpNotification() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Level Up!");
            alert.setHeaderText(null);
            alert.setContentText("Congratulations! You've reached level " + level + "!");
            alert.show();
        });
    }

    private void updateHUD() {
        if (gameController != null) {
            gameController.setLevel(level);
            gameController.setExp(currentExp, expToNextLevel);
        }
    }

    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getExpToNextLevel() { return expToNextLevel; }

    public void setLevel(int level) { this.level = level; }
    public void setCurrentExp(int exp) { this.currentExp = exp; }
    public void setExpToNextLevel(int exp) { this.expToNextLevel = exp; }
}