package code;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

import javax.swing.plaf.basic.BasicButtonUI;

public class HUDController {
    @FXML
    private BasicButtonUI statisticsButton;
    @FXML
    private BasicButtonUI storeButton;
    @FXML
    private BasicButtonUI storageButton;
    @FXML
    private Text usernameText;
    @FXML
    private Text levelText;
    @FXML
    private Text moneyText;
    @FXML
    private Text expText;

    @FXML
    public void initialize() {
        // Initialize HUD with default or placeholder values
        usernameText.setText("Player");
        levelText.setText("Level: 1");
        moneyText.setText("Money: " + moneyText.getText());
        expText.setText("Exp to next level: 0/100");
    }

    // Methods to update HUD values
    public void setUsername(String username) {
        usernameText.setText(username);
    }

    public void setLevel(int level) {
        levelText.setText("Level: " + level);
    }

    public void setMoney(int money) {
        moneyText.setText("Money: " + money);
    }

    public void setExp(int currentExp, int nextLevelExp) {
        expText.setText("Exp to next level: " + currentExp + "/" + nextLevelExp);
    }
    public void setStatisticsButton(BasicButtonUI statisticsButton) {
        System.err.println("DEBUG: Setting statistics button");
    }
    public void setStorageButton(BasicButtonUI statisticsButton) {
        System.err.println("DEBUG: Setting statistics button");
    }
    public void setStoreButton(BasicButtonUI statisticsButton) {
        System.err.println("DEBUG: Setting statistics button");
    }
}