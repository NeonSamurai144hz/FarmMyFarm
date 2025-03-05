package code;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {
    // HUD Elements
    @FXML
    private Text usernameText;

    @FXML
    private Text levelText;

    @FXML
    private Text moneyText;

    @FXML
    private Text expText;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button storeButton;

    @FXML
    private Button storageButton;

    // Farm Grid Elements
    @FXML
    private GridPane farmGrid;

    @FXML
    private VBox root;

    private static final int GRID_SIZE = 168;
    private int cellSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize HUD
        initializeHUD();

        // Create Farm Grid
        createGrid();

        // Setup Button Listeners
        setupButtonListeners();
    }

    private void initializeHUD() {
        usernameText.setText("Player");
        levelText.setText("Level: 1");
        moneyText.setText("Money: 500");
        expText.setText("Exp to next level: 0/100");
    }

    private void initializeResponsivesizing() {
        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calculate cell size based on screen width
        double gridWidth = screenBounds.getWidth() * 0.9; // Use 90% of screen width
        cellSize = (int) (gridWidth / GRID_SIZE);
    }

    private void createGrid() {
        farmGrid.getChildren().clear(); // Clear any existing grid

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle parcel = createParcel();
                farmGrid.add(parcel, col, row);
            }
        }
    }

    private Rectangle createParcel() {
        Rectangle parcel = new Rectangle(cellSize, cellSize);
        parcel.setFill(Color.LIGHTGREEN);  // Default empty land color
        parcel.setStroke(Color.DARKGREEN);

        // Plant growth simulation on click
        parcel.setOnMouseClicked(event -> simulatePlantGrowth((Rectangle) event.getSource()));

        return parcel;
    }

    private void simulatePlantGrowth(Rectangle parcel) {
        // Simulate plant growth stages through color changes
        if (parcel.getFill() == Color.LIGHTGREEN) {
            parcel.setFill(Color.YELLOWGREEN);  // Seeded stage
        } else if (parcel.getFill() == Color.YELLOWGREEN) {
            parcel.setFill(Color.GREEN);  // Growing stage
        } else if (parcel.getFill() == Color.GREEN) {
            parcel.setFill(Color.DARKGREEN);  // Mature stage
        } else {
            parcel.setFill(Color.LIGHTGREEN);  // Reset to empty
        }
    }

    private void setupButtonListeners() {
        // Placeholder button click handlers
        statisticsButton.setOnAction(event -> System.out.println("Statistics Button Clicked"));
        storeButton.setOnAction(event -> System.out.println("Store Button Clicked"));
        storageButton.setOnAction(event -> System.out.println("Storage Button Clicked"));
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
}
