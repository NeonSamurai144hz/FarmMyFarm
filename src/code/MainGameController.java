package code;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private Text usernameText;
    @FXML private Text levelText;
    @FXML private Text moneyText;
    @FXML private Text expText;
    @FXML private Button achievmentsButton;
    @FXML private Button storeStorageButton;
    @FXML private Button chooseCropButton;
    @FXML private Button chooseAnimalButton; // Add this to your FXML

    @FXML private GridPane farmGrid;

    private static final int GRID_SIZE = 16;
    private double cellSize = 32;

    private PlantingController plantingController = new PlantingController();
    private AnimalController animalController = new AnimalController();
    private boolean isAnimalMode = false; // Flag to determine if we're placing animals or crops

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GameEconomy.setGameController(this);
        initializeHUD();
        initializeResponsiveSizing();
        createGrid();
        setupButtonListeners();
        setMoney(GameEconomy.getPlayerMoney());  // Initialize money display
    }

    private void initializeHUD() {
        usernameText.setText("Player");
        levelText.setText("Level: 1");
        moneyText.setText("Money: " + GameEconomy.getPlayerMoney());
        expText.setText("Exp to next level: 0/100");
    }

    private void initializeResponsiveSizing() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double gridWidth = screenBounds.getWidth() * 0.99;
        double gridHeight = screenBounds.getHeight() * 0.8;
        cellSize = gridWidth / GRID_SIZE;
        double calculatedGridHeight = cellSize * GRID_SIZE;
        if (calculatedGridHeight > gridHeight) {
            cellSize = gridHeight / GRID_SIZE;
        }
    }

    private void createGrid() {
        farmGrid.getChildren().clear();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle parcel = createParcel();
                farmGrid.add(parcel, col, row);
            }
        }
    }

    private Rectangle createParcel() {
        Rectangle parcel = new Rectangle(cellSize, cellSize);
        parcel.setFill(Color.LIGHTGREEN);
        parcel.setStroke(Color.BLACK);
        parcel.setStrokeWidth(1);

        parcel.setOnMouseClicked(event -> {
            if (isAnimalMode) {
                handleAnimalParcelClick(parcel);
            } else {
                handleCropParcelClick(parcel);
            }
        });

        return parcel;
    }

    private void handleCropParcelClick(Rectangle parcel) {
        if (parcel.getUserData() instanceof PlantingController.GrowthData) {
            plantingController.harvestCrop(parcel);
        } else if (parcel.getUserData() == null) {
            plantingController.simulatePlantGrowth(parcel);
        }
    }

    private void handleAnimalParcelClick(Rectangle parcel) {
        if (parcel.getUserData() instanceof AnimalController.GrowthData) {
            animalController.collectAnimalProduct(parcel);
        } else if (parcel.getUserData() == null) {
            animalController.placeAnimal(parcel);
        }
    }

    private void setupButtonListeners() {
        achievmentsButton.setOnAction(event -> System.out.println("Statistics Button Clicked"));
        storeStorageButton.setOnAction(event -> openStorageStoreModal());
        chooseCropButton.setOnAction(event -> {
            isAnimalMode = false;
            plantingController.openCropSelectionModal();
        });
        chooseAnimalButton.setOnAction(event -> {
            isAnimalMode = true;
            animalController.openAnimalSelectionModal();
        });
    }

    private void openStorageStoreModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Storage & Store");

        VBox modalContent = new VBox(10);
        modalContent.setPadding(new Insets(20));
        modalContent.setAlignment(Pos.CENTER);

        Text moneyDisplay = new Text("Money: $" + GameEconomy.getPlayerMoney());
        modalContent.getChildren().add(moneyDisplay);

        // Crop section
        Label cropSectionLabel = new Label("Crops");
        cropSectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        modalContent.getChildren().add(cropSectionLabel);

        for (CropType cropType : CropType.values()) {
            HBox cropRow = new HBox(10);
            cropRow.setAlignment(Pos.CENTER);

            Label cropLabel = new Label();

            Button sellButton = new Button("Sell ($" + GameEconomy.SELL_PRICE + ")");
            Button buyButton = new Button("Buy ($" + GameEconomy.BUY_PRICE + ")");

            // Update labels with initial values
            int initialCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
            cropLabel.setText(cropType + ": " + initialCropCount + "/" + plantingController.getMaxCrops());

            sellButton.setOnAction(e -> {
                int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
                if (currentCropCount > 0) {
                    plantingController.updateCropStorage(cropType, currentCropCount - 1);
                    GameEconomy.addMoney(GameEconomy.SELL_PRICE);
                    cropLabel.setText(cropType + ": " + (currentCropCount - 1) + "/" + plantingController.getMaxCrops());
                    moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
                }
            });

            buyButton.setOnAction(e -> {
                int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
                if (currentCropCount < plantingController.getMaxCrops() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE)) {
                    plantingController.updateCropStorage(cropType, currentCropCount + 1);
                    cropLabel.setText(cropType + ": " + (currentCropCount + 1) + "/" + plantingController.getMaxCrops());
                    moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
                }
            });

            cropRow.getChildren().addAll(cropLabel, sellButton, buyButton);
            modalContent.getChildren().add(cropRow);
        }

        // Animal section
        Label animalSectionLabel = new Label("Animals");
        animalSectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        modalContent.getChildren().add(animalSectionLabel);

        for (AnimalType animalType : AnimalType.values()) {
            HBox animalRow = new HBox(10);
            animalRow.setAlignment(Pos.CENTER);

            Label animalLabel = new Label();

            Button sellButton = new Button("Sell ($" + GameEconomy.SELL_PRICE * 2 + ")");
            Button buyButton = new Button("Buy ($" + GameEconomy.BUY_PRICE * 3 + ")");

            // Update labels with initial values
            int initialAnimalCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
            animalLabel.setText(animalType + ": " + initialAnimalCount + "/" + animalController.getMaxAnimals());

            sellButton.setOnAction(e -> {
                int currentAnimalCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
                if (currentAnimalCount > 0) {
                    animalController.updateAnimalStorage(animalType, currentAnimalCount - 1);
                    GameEconomy.addMoney(GameEconomy.SELL_PRICE * 2);
                    animalLabel.setText(animalType + ": " + (currentAnimalCount - 1) + "/" + animalController.getMaxAnimals());
                    moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
                }
            });

            buyButton.setOnAction(e -> {
                int currentAnimalCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
                if (currentAnimalCount < animalController.getMaxAnimals() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE * 3)) {
                    animalController.updateAnimalStorage(animalType, currentAnimalCount + 1);
                    animalLabel.setText(animalType + ": " + (currentAnimalCount + 1) + "/" + animalController.getMaxAnimals());
                    moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
                }
            });

            animalRow.getChildren().addAll(animalLabel, sellButton, buyButton);
            modalContent.getChildren().add(animalRow);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modalStage.close());
        modalContent.getChildren().add(closeButton);

        Scene modalScene = new Scene(modalContent);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

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






//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.geometry.Rectangle2D;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Text;
//import javafx.stage.Modality;
//import javafx.stage.Screen;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.ResourceBundle;
//
//public class MainGameController implements Initializable {
//    // HUD Elements
//    @FXML private Text usernameText;
//    @FXML private Text levelText;
//    @FXML private Text moneyText;
//    @FXML private Text expText;
//    @FXML private Button statisticsButton;
//    @FXML private Button storeButton;
//    @FXML private Button storageButton;
//
//    // Farm Grid Elements
//    @FXML private GridPane farmGrid;
//
//    private static final int GRID_SIZE = 16;
//    private double cellSize = 32;
//
//    private Map<CropType, Integer> cropStorage = new HashMap<>();
//    private final int MAX_CROPS = 100;
//
//    private CropType selectedCropType = CropType.WHEAT;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        // Initialize screen-responsive sizing
//        initializeResponsiveSizing();
//
//        // Initialize HUD
//        initializeHUD();
//
//        // Create Farm Grid
//        createGrid();
//
//        // Setup Button Listeners
//        setupButtonListeners();
//    }
//
//    private void initializeHUD() {
//        usernameText.setText("Player");
//        levelText.setText("Level: 1");
//        moneyText.setText("Money: 500");
//        expText.setText("Exp to next level: 0/100");
//    }
//
//    private void initializeResponsiveSizing() {
//        // Get screen dimensions
//        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
//
//        // Calculate cell size to fill width and leave room for HUD
//        double gridWidth = screenBounds.getWidth() * 0.99; // Use 99% of screen width
//        double gridHeight = screenBounds.getHeight() * 0.8; // Use 80% of screen height
//
//        // Calculate cell size based on width
//        cellSize = gridWidth / GRID_SIZE;
//
//        // Adjust height if needed to ensure proper proportion
//        double calculatedGridHeight = cellSize * GRID_SIZE;
//        if (calculatedGridHeight > gridHeight) {
//            cellSize = gridHeight / GRID_SIZE;
//        }
//
//        System.out.println("Screen Width: " + screenBounds.getWidth());
//        System.out.println("Screen Height: " + screenBounds.getHeight());
//        System.out.println("Calculated Cell Size: " + cellSize);
//        System.out.println("Grid Width: " + (cellSize * GRID_SIZE));
//        System.out.println("Grid Height: " + (cellSize * GRID_SIZE));
//    }
//
//    private void createGrid() {
//        farmGrid.getChildren().clear(); // Clear any existing grid
//
//        for (int row = 0; row < GRID_SIZE; row++) {
//            for (int col = 0; col < GRID_SIZE; col++) {
//                Rectangle parcel = createParcel();
//                farmGrid.add(parcel, col, row);
//            }
//        }
//    }
//
//    private Rectangle createParcel() {
//        Rectangle parcel = new Rectangle(cellSize, cellSize);
//        parcel.setFill(Color.LIGHTGREEN);  // Default empty land color
//        parcel.setStroke(Color.GREEN);
//        parcel.setStrokeWidth(0.5);
//
//        // Plant growth simulation on click
//        parcel.setOnMouseClicked(event -> simulatePlantGrowth((Rectangle) event.getSource()));
//
//        return parcel;
//    }
//
//    private class GrowthData {
//        Timeline timeline;
//        CropType cropType;
//
//        public GrowthData(Timeline timeline, CropType cropType) {
//            this.timeline = timeline;
//            this.cropType = cropType;
//        }
//    }
//
//    private void simulatePlantGrowth(Rectangle parcel) {
//        // If the cell is fully grown, increment cropCount and reset it to not planted.
//        if (parcel.getFill().equals(Color.DARKGREEN)) {
//            cropCount++;  // Increment harvested crop count.
//            System.out.println("Crop harvested! Total count: " + cropCount);
//            parcel.setFill(Color.LIGHTGREEN);
//            return;
//        }
//
//        // If a growth timeline is already running, ignore additional clicks.
//        if (parcel.getUserData() instanceof Timeline) {
//            return;
//        }
//
//        // Create a Timeline to change the color with a 2-second delay between each stage.
//        Timeline growthTimeline = new Timeline(
//                new KeyFrame(Duration.seconds(2), event -> parcel.setFill(Color.YELLOWGREEN)),
//                new KeyFrame(Duration.seconds(4), event -> parcel.setFill(Color.GREEN)),
//                new KeyFrame(Duration.seconds(6), event -> {
//                    parcel.setFill(Color.DARKGREEN);
//                    // Once fully grown, clear the timeline from user data.
//                    parcel.setUserData(null);
//                })
//        );
//
//        // Start the timeline and store it in the parcel's user data.
//        growthTimeline.play();
//        parcel.setUserData(growthTimeline);
//    }
//
//    private void openStorageModal() {
//        // Create a new Stage for the modal window.
//        Stage modalStage = new Stage();
//        modalStage.initModality(Modality.APPLICATION_MODAL); // Blocks events to other windows.
//        modalStage.setTitle("Storage");
//
//        // Define held amount and maximum capacity.
//        int heldCrops = cropCount; // You could also use another variable if storage differs from harvested count.
//        int maxCrops = 100;        // Fixed maximum for demonstration purposes.
//
//        // Create a label displaying the crops information.
//        Label cropsLabel = new Label("Crops: " + heldCrops + " / " + maxCrops);
//
//        // Create a close button.
//        Button closeButton = new Button("Close");
//        closeButton.setOnAction(e -> modalStage.close());
//
//        // Create a layout container for the modal.
//        VBox modalLayout = new VBox(10);
//        modalLayout.setPadding(new Insets(20));
//        modalLayout.setAlignment(Pos.CENTER);
//        modalLayout.getChildren().addAll(cropsLabel, closeButton);
//
//        // Set the scene and show the modal.
//        Scene modalScene = new Scene(modalLayout, 300, 150);
//        modalStage.setScene(modalScene);
//        modalStage.showAndWait();
//    }
//
//        private void setupButtonListeners() {
//        // Placeholder button click handlers
//        statisticsButton.setOnAction(event -> System.out.println("Statistics Button Clicked"));
//        storeButton.setOnAction(event -> System.out.println("Store Button Clicked"));
//        storageButton.setOnAction(event -> openStorageModal());
//    }
//
//    // Methods to update HUD values
//    public void setUsername(String username) {
//        usernameText.setText(username);
//    }
//
//    public void setLevel(int level) {
//        levelText.setText("Level: " + level);
//    }
//
//    public void setMoney(int money) {
//        moneyText.setText("Money: " + money);
//    }
//
//    public void setExp(int currentExp, int nextLevelExp) {
//        expText.setText("Exp to next level: " + currentExp + "/" + nextLevelExp);
//    }
//}