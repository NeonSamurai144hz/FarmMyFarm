package code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {
    @FXML private Text usernameText;
    @FXML private Text levelText;
    @FXML private Text moneyText;
    @FXML private Text expText;
    @FXML private Button financeButton;
    @FXML private Button storeStorageButton;
    @FXML private Button chooseCropButton;
    @FXML private Button chooseAnimalButton;
    @FXML private GridPane farmGrid;

    private static final int GRID_SIZE = 16;
    private double cellSize = 32;

    // Use separate controllers for crop and animal logic
    private PlantingController plantingController = new PlantingController();
    private AnimalController animalController = new AnimalController();
    private boolean isAnimalMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GameEconomy.setGameController(this);
        initializeHUD();
        initializeResponsiveSizing();
        createGrid();
        setupButtonListeners();
        setMoney(GameEconomy.getPlayerMoney());
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
            // Use getButton() to detect which button was clicked.
            if (isAnimalMode) {
                // Clear any leftover crop data
                if (!(parcel.getUserData() instanceof AnimalController.GrowthData)) {
                    parcel.setUserData(null);
                }
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (parcel.getUserData() instanceof AnimalController.GrowthData) {
                        animalController.feedAnimal(parcel);
                    } else {
                        animalController.placeAnimal(parcel);
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    animalController.collectAnimalProduct(parcel);
                }
            } else {
                if (parcel.getUserData() instanceof PlantingController.GrowthData) {
                    plantingController.harvestCrop(parcel);
                } else {
                    plantingController.simulatePlantGrowth(parcel);
                }
            }
        });

        return parcel;
    }


    private void setupButtonListeners() {
        financeButton.setOnAction(event -> System.out.println("Finance not implemented"));
        storeStorageButton.setOnAction(event -> openStorageStoreModal());
        chooseCropButton.setOnAction(event -> {
            isAnimalMode = false;
            plantingController.openCropSelectionModal();
        });
        chooseAnimalButton.setOnAction(event -> {
            isAnimalMode = true;
            openAnimalSelectionModal();
        });

        // Add save/load buttons to the HUD
        Button saveButton = new Button("Save Game");
        Button loadButton = new Button("Load Game");

        saveButton.setOnAction(event -> saveGame());
        loadButton.setOnAction(event -> loadGame());

        // Create centered button box
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(saveButton, loadButton);

        // Add to layout
        ((VBox) farmGrid.getParent()).getChildren().add(buttonBox);
    }

    private void openFinanceModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Financial History");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Summary section
        Text summaryTitle = new Text("Financial Summary");
        summaryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Text totalSales = new Text("Total Sales: $" + Finance.getTotalSales());
        Text totalExpenses = new Text("Total Expenses: $" + Finance.getTotalExpenses());
        Text netProfit = new Text("Net Profit: $" + (Finance.getTotalSales() - Finance.getTotalExpenses()));

        // Sales by type section
        Text typeTitle = new Text("\nSales by Type");
        typeTitle.setStyle("-fx-font-weight: bold");

        VBox typeContent = new VBox(5);
        for (Map.Entry<String, Integer> entry : Finance.getItemSalesByType().entrySet()) {
            typeContent.getChildren().add(new Text(entry.getKey() + ": $" + entry.getValue()));
        }

        // Sales by item section
        Text itemTitle = new Text("\nSales by Item");
        itemTitle.setStyle("-fx-font-weight: bold");

        VBox itemContent = new VBox(5);
        for (Map.Entry<String, Integer> entry : Finance.getItemSalesByName().entrySet()) {
            itemContent.getChildren().add(new Text(entry.getKey() + ": $" + entry.getValue()));
        }

        // Transaction history section
        Text historyTitle = new Text("\nTransaction History");
        historyTitle.setStyle("-fx-font-weight: bold");

        VBox historyContent = new VBox(5);
        for (Finance.Transaction transaction : Finance.getTransactions()) {
            historyContent.getChildren().add(new Text(transaction.toString()));
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modalStage.close());

        content.getChildren().addAll(
                summaryTitle, totalSales, totalExpenses, netProfit,
                typeTitle, typeContent,
                itemTitle, itemContent,
                historyTitle, historyContent,
                closeButton
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        Scene modalScene = new Scene(scrollPane, 400, 500);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void saveGame() {
        GameSaveData saveData = new GameSaveData(
                GameEconomy.getPlayerMoney(),
                plantingController.getCropStorage(),
                animalController.getAnimalStorage(),
                animalController.getFeedStorage(),
                animalController.getResourceStorage(),
                Finance.getTransactions()
        );
        GameSaveManager.saveGame(saveData);
    }

    private void loadGame() {
        GameSaveData saveData = GameSaveManager.loadGame();
        if (saveData != null) {
            // Restore game state
            GameEconomy.setPlayerMoney(saveData.getPlayerMoney());

            // Restore storages
            for (Map.Entry<CropType, Integer> entry : saveData.getCropStorage().entrySet()) {
                plantingController.updateCropStorage(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<AnimalType, Integer> entry : saveData.getAnimalStorage().entrySet()) {
                animalController.updateAnimalStorage(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<CropType, Integer> entry : saveData.getFeedStorage().entrySet()) {
                animalController.updateFeedStorage(entry.getKey(), entry.getValue());
            }

            // Update UI
            setMoney(saveData.getPlayerMoney());

            // Restore transactions
            Finance.setTransactions(saveData.getTransactions());
        }
    }

    private void openStorageStoreModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Storage & Store");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create all tabs first
        Tab cropsTab = new Tab("Crops");
        Tab animalsTab = new Tab("Animals");
        Tab feedTab = new Tab("Feed");
        Tab resourcesTab = new Tab("Resources");

        // Set content for each tab
        cropsTab.setContent(createCropsContent());
        animalsTab.setContent(createAnimalsContent());
        feedTab.setContent(createFeedContent());
        resourcesTab.setContent(createResourcesContent());

        // Add all tabs to the tabPane
        tabPane.getTabs().addAll(cropsTab, animalsTab, feedTab, resourcesTab);

        Scene modalScene = new Scene(tabPane, 400, 500);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private VBox createResourcesContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text moneyDisplay = new Text("Money: $" + GameEconomy.getPlayerMoney());
        content.getChildren().add(moneyDisplay);

        for (Resource resource : Resource.values()) {
            HBox resourceRow = createResourceRow(resource, moneyDisplay);
            content.getChildren().add(resourceRow);
        }

        return content;
    }

    private HBox createResourceRow(Resource resource, Text moneyDisplay) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);

        Label resourceLabel = new Label(resource + ": " +
                animalController.getResourceStorage().getOrDefault(resource, 0) +
                "/" + animalController.getMaxResources());

        Button sellButton = new Button("Sell ($" + resource.getSellPrice() + ")");
        sellButton.setOnAction(e -> {
            int count = animalController.getResourceStorage().get(resource);
            if (count > 0) {
                animalController.updateResourceStorage(resource, count - 1);
                GameEconomy.addMoney(resource.getSellPrice());
                resourceLabel.setText(resource + ": " + (count - 1) +
                        "/" + animalController.getMaxResources());
                updateMoneyDisplays(moneyDisplay);
            }
        });

        row.getChildren().addAll(resourceLabel, sellButton);
        return row;
    }

    private VBox createCropsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text moneyDisplay = new Text("Money: $" + GameEconomy.getPlayerMoney());
        content.getChildren().add(moneyDisplay);

        for (CropType cropType : CropType.values()) {
            HBox cropRow = createCropRow(cropType, moneyDisplay);
            content.getChildren().add(cropRow);
        }

        return content;
    }

    private HBox createCropRow(CropType cropType, Text moneyDisplay) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);

        Label cropLabel = new Label();
        Button sellButton = new Button("Sell ($" + GameEconomy.SELL_PRICE + ")");
        Button buyButton = new Button("Buy ($" + GameEconomy.BUY_PRICE + ")");
        Button transferButton = new Button("Transfer to Feed");

        int cropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        cropLabel.setText(cropType + ": " + cropCount + "/" + plantingController.getMaxCrops());

        sellButton.setOnAction(e -> handleCropSell(cropType, cropLabel, moneyDisplay));
        buyButton.setOnAction(e -> handleCropBuy(cropType, cropLabel, moneyDisplay));
        transferButton.setOnAction(e -> {
            int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
            int currentFeedCount = animalController.getFeedAmount(cropType);

            if (currentCropCount > 0 && currentFeedCount < animalController.getMaxFeed()) {
                plantingController.updateCropStorage(cropType, currentCropCount - 1);
                animalController.updateFeedStorage(cropType, currentFeedCount + 1);
                cropLabel.setText(cropType + ": " + (currentCropCount - 1) + "/" + plantingController.getMaxCrops());
            }
        });

        row.getChildren().addAll(cropLabel, sellButton, buyButton, transferButton);
        return row;
    }

    private VBox createAnimalsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text moneyDisplay = new Text("Money: $" + GameEconomy.getPlayerMoney());
        content.getChildren().add(moneyDisplay);

        for (AnimalType animalType : AnimalType.values()) {
            HBox animalRow = createAnimalRow(animalType, moneyDisplay);
            content.getChildren().add(animalRow);
        }

        return content;
    }

    private HBox createAnimalRow(AnimalType animalType, Text moneyDisplay) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);

        Label animalLabel = new Label();
        Button sellButton = new Button("Sell ($" + GameEconomy.SELL_PRICE * 2 + ")");
        Button buyButton = new Button("Buy ($" + GameEconomy.BUY_PRICE * 3 + ")");

        int animalCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        animalLabel.setText(animalType + ": " + animalCount + "/" + animalController.getMaxAnimals());

        sellButton.setOnAction(e -> handleAnimalSell(animalType, animalLabel, moneyDisplay));
        buyButton.setOnAction(e -> handleAnimalBuy(animalType, animalLabel, moneyDisplay));

        row.getChildren().addAll(animalLabel, sellButton, buyButton);
        return row;
    }

    private VBox createFeedContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text moneyDisplay = new Text("Money: $" + GameEconomy.getPlayerMoney());
        content.getChildren().add(moneyDisplay);

        // Add feed requirements info
        Text requirementsTitle = new Text("Feed Requirements:");
        Text pigReq = new Text("Pig: 1 Potato + 1 Carrot");
        Text cowReq = new Text("Cow: 2 Wheat");
        Text chickenReq = new Text("Chicken: 1 Carrot");
        content.getChildren().addAll(requirementsTitle, pigReq, cowReq, chickenReq, new Text("")); // Empty text for spacing

        Map<CropType, Label> cropLabels = new HashMap<>();
        Map<CropType, Label> feedLabels = new HashMap<>();

        // Create rows for each crop type with transfer button
        for (CropType cropType : CropType.values()) {
            HBox feedRow = new HBox(10);
            feedRow.setAlignment(Pos.CENTER);

            Label cropLabel = new Label();
            Label feedLabel = new Label();

            // Store labels in maps for updating
            cropLabels.put(cropType, cropLabel);
            feedLabels.put(cropType, feedLabel);

            // Update label texts
            updateLabels(cropType, cropLabel, feedLabel);

            Button transferButton = new Button("Transfer to Feed");
            transferButton.setOnAction(e -> {
                int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
                int currentFeedCount = animalController.getFeedAmount(cropType);

                if (currentCropCount > 0 && currentFeedCount < animalController.getMaxFeed()) {
                    plantingController.updateCropStorage(cropType, currentCropCount - 1);
                    animalController.updateFeedStorage(cropType, currentFeedCount + 1);

                    // Update all labels after transfer
                    for (CropType type : CropType.values()) {
                        updateLabels(type, cropLabels.get(type), feedLabels.get(type));
                    }
                }
            });

            feedRow.getChildren().addAll(cropLabel, transferButton, feedLabel);
            content.getChildren().add(feedRow);
        }

        return content;
    }

    private void updateLabels(CropType cropType, Label cropLabel, Label feedLabel) {
        cropLabel.setText(cropType + ": " +
                plantingController.getCropStorage().getOrDefault(cropType, 0) +
                "/" + plantingController.getMaxCrops());

        feedLabel.setText("Feed: " +
                animalController.getFeedAmount(cropType) +
                "/" + animalController.getMaxFeed());
    }

    private void handleCropSell(CropType cropType, Label cropLabel, Text moneyDisplay) {
        int currentCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        if (currentCount > 0) {
            plantingController.updateCropStorage(cropType, currentCount - 1);
            GameEconomy.addMoney(GameEconomy.SELL_PRICE);
            cropLabel.setText(cropType + ": " + (currentCount - 1) + "/" + plantingController.getMaxCrops());
            updateMoneyDisplays(moneyDisplay);
        }
    }

    private void handleCropBuy(CropType cropType, Label cropLabel, Text moneyDisplay) {
        int currentCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        if (currentCount < plantingController.getMaxCrops() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE)) {
            plantingController.updateCropStorage(cropType, currentCount + 1);
            cropLabel.setText(cropType + ": " + (currentCount + 1) + "/" + plantingController.getMaxCrops());
            updateMoneyDisplays(moneyDisplay);
        }
    }

    private void handleCropTransfer(CropType cropType, Label cropLabel, Text feedLabel) {
        int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        int currentFeedCount = animalController.getFeedAmount(cropType);

        if (currentCropCount > 0 && currentFeedCount < animalController.getMaxFeed()) {
            plantingController.updateCropStorage(cropType, currentCropCount - 1);
            animalController.updateFeedStorage(cropType, currentFeedCount + 1);
            cropLabel.setText(cropType + ": " + (currentCropCount - 1) + "/" + plantingController.getMaxCrops());
            feedLabel.setText(cropType + " Feed: " + (currentFeedCount + 1) + "/" + animalController.getMaxFeed());
        }
    }

    private void handleAnimalSell(AnimalType animalType, Label animalLabel, Text moneyDisplay) {
        int currentCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        if (currentCount > 0) {
            animalController.updateAnimalStorage(animalType, currentCount - 1);
            GameEconomy.addMoney(GameEconomy.SELL_PRICE * 2);
            animalLabel.setText(animalType + ": " + (currentCount - 1) + "/" + animalController.getMaxAnimals());
            updateMoneyDisplays(moneyDisplay);
        }
    }

    private void handleAnimalBuy(AnimalType animalType, Label animalLabel, Text moneyDisplay) {
        int currentCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        if (currentCount < animalController.getMaxAnimals() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE * 3)) {
            animalController.updateAnimalStorage(animalType, currentCount + 1);
            animalLabel.setText(animalType + ": " + (currentCount + 1) + "/" + animalController.getMaxAnimals());
            updateMoneyDisplays(moneyDisplay);
        }
    }

    private void updateMoneyDisplays(Text moneyDisplay) {
        moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
        moneyText.setText("Money: " + GameEconomy.getPlayerMoney());
    }

    // Modal for animal selection moved to MainGameController
    private void openAnimalSelectionModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Select Animal Type");

        VBox modalLayout = new VBox(10);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setAlignment(Pos.CENTER);

        for (AnimalType animalType : AnimalType.values()) {
            int count = animalController.getAnimalStorage().getOrDefault(animalType, 0);
            Button animalButton = new Button(animalType.toString() + " (" + count + ")");
            animalButton.setDisable(count <= 0);
            animalButton.setOnAction(e -> {
                AnimalSelection.setSelectedAnimalType(animalType);
                modalStage.close();
                System.out.println("Selected animal: " + animalType);
            });
            modalLayout.getChildren().add(animalButton);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modalStage.close());
        modalLayout.getChildren().add(closeButton);

        Scene scene = new Scene(modalLayout, 200, 200);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    // Modal for crop selection remains unchanged (handled in PlantingController)
    // HUD update methods:
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
