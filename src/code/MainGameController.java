package code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
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
    private Timeline saleTimer;
    private Sale currentSale;

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
        setupSaleTimer();
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
        financeButton.setOnAction(event -> openFinanceModal());
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

    private int getCurrentPrice(String itemType, String itemName) {
        if (currentSale != null && currentSale.isActive() &&
                currentSale.getItemType().equals(itemType) &&
                currentSale.getItemName().equals(itemName)) {
            return currentSale.getSalePrice();
        }
        return itemType.equals("Animal") ? GameEconomy.BUY_PRICE * 3 : GameEconomy.BUY_PRICE;
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

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modalStage.close());

        content.getChildren().addAll(
                summaryTitle,
                totalSales,
                totalExpenses,
                netProfit,
                closeButton
        );

        Scene modalScene = new Scene(content, 400, 300);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void setupSaleTimer() {
        saleTimer = new Timeline(
                new KeyFrame(Duration.seconds(20), e -> tryStartSale())  // Changed from 30 to 10 seconds
        );
        saleTimer.setCycleCount(Timeline.INDEFINITE);
        saleTimer.play();
    }

    private void tryStartSale() {
        if (currentSale != null && currentSale.isActive()) {
            return;
        }

        if (Math.random() < 0.5) { // 50% chance
            // Randomly choose between crops and animals
            if (Math.random() < 0.5) {
                CropType[] crops = CropType.values();
                CropType selectedCrop = crops[(int)(Math.random() * crops.length)];
                currentSale = new Sale("Crop", selectedCrop.toString(), GameEconomy.BUY_PRICE);
            } else {
                AnimalType[] animals = AnimalType.values();
                AnimalType selectedAnimal = animals[(int)(Math.random() * animals.length)];
                currentSale = new Sale("Animal", selectedAnimal.toString(), GameEconomy.BUY_PRICE * 3);
            }
            showSaleNotification();
        }
    }

    private void showSaleNotification() {
        Stage notification = new Stage();
        notification.initModality(Modality.NONE);
        notification.setTitle("Sale Alert!");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text itemText = new Text(currentSale.getItemName() + " ON SALE!");
        Text priceText = new Text("Original Price: $" + currentSale.getOriginalPrice());
        priceText.setStrikethrough(true);
        Text saleText = new Text("Sale Price: $" + currentSale.getSalePrice());
        saleText.setFill(Color.RED);
        Text timerText = new Text("90 seconds remaining");

        content.getChildren().addAll(itemText, priceText, saleText, timerText);

        Scene scene = new Scene(content);
        notification.setScene(scene);
        notification.show();

        // Update timer
        Timeline countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    long remaining = currentSale.getTimeRemaining() / 1000;
                    timerText.setText(remaining + " seconds remaining");
                    if (remaining <= 0) {
                        notification.close();
                        currentSale = null;
                    }
                })
        );
        countdown.setCycleCount((int)currentSale.getTimeRemaining() / 1000);
        countdown.play();
    }

    private void saveGame() {
        // Create grid data array
        ParcelData[][] gridData = new ParcelData[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle parcel = (Rectangle) getNodeFromGridPane(farmGrid, col, row);
                if (parcel != null) {
                    ParcelData data = new ParcelData(ParcelData.ParcelType.EMPTY);

                    if (parcel.getUserData() instanceof PlantingController.GrowthData) {
                        PlantingController.GrowthData growthData =
                                (PlantingController.GrowthData) parcel.getUserData();
                        if (growthData.timeline == null) {
                            data.setType(ParcelData.ParcelType.READY_CROP);
                        } else {
                            data.setType(ParcelData.ParcelType.GROWING_CROP);
                        }
                        data.setCropType(growthData.cropType);
                    }
                    else if (parcel.getUserData() instanceof AnimalController.GrowthData) {
                        AnimalController.GrowthData growthData =
                                (AnimalController.GrowthData) parcel.getUserData();
                        if (growthData.isAdult) {
                            data.setType(ParcelData.ParcelType.ADULT_ANIMAL);
                        } else {
                            data.setType(ParcelData.ParcelType.GROWING_ANIMAL);
                        }
                        data.setAnimalType(growthData.animalType);
                        data.setGrowthStage(growthData.feedingStage);
                        data.setAdult(growthData.isAdult);
                    }

                    gridData[row][col] = data;
                }
            }
        }

        GameSaveData saveData = new GameSaveData(
                GameEconomy.getPlayerMoney(),
                plantingController.getCropStorage(),
                animalController.getAnimalStorage(),
                animalController.getFeedStorage(),
                animalController.getResourceStorage(),
                Finance.getTransactions(),
                gridData
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

            for (Map.Entry<Resource, Integer> entry : saveData.getResourceStorage().entrySet()) {
                animalController.updateResourceStorage(entry.getKey(), entry.getValue());
            }

            // Update UI
            setMoney(saveData.getPlayerMoney());

            // Restore transactions
            Finance.setTransactions(saveData.getTransactions());

            // Restore grid state
            ParcelData[][] gridData = saveData.getGridData();
            if (gridData != null) {
                for (int row = 0; row < GRID_SIZE; row++) {
                    for (int col = 0; col < GRID_SIZE; col++) {
                        ParcelData data = gridData[row][col];
                        Rectangle parcel = (Rectangle) getNodeFromGridPane(farmGrid, col, row);

                        if (parcel != null && data != null) {
                            switch (data.getType()) {
                                case READY_CROP:
                                    parcel.setFill(data.getCropType().getStage3Color());
                                    parcel.setUserData(new PlantingController.GrowthData(null, data.getCropType()));
                                    break;
                                case GROWING_CROP:
                                    parcel.setFill(data.getCropType().getStage1Color());
                                    Timeline cropTimeline = new Timeline(
                                            new KeyFrame(Duration.seconds(2), e ->
                                                    parcel.setFill(data.getCropType().getStage2Color())),
                                            new KeyFrame(Duration.seconds(4), e -> {
                                                parcel.setFill(data.getCropType().getStage3Color());
                                                parcel.setUserData(new PlantingController.GrowthData(null, data.getCropType()));
                                            })
                                    );
                                    parcel.setUserData(new PlantingController.GrowthData(cropTimeline, data.getCropType()));
                                    cropTimeline.play();
                                    break;
                                case GROWING_ANIMAL:
                                case ADULT_ANIMAL:
                                    if (data.isAdult()) {
                                        parcel.setFill(data.getAnimalType().getStage3Color());
                                    } else {
                                        parcel.setFill(data.getAnimalType().getStage1Color());
                                    }
                                    AnimalController.GrowthData animalData = new AnimalController.GrowthData(null, data.getAnimalType());
                                    animalData.feedingStage = data.getGrowthStage();
                                    animalData.isAdult = data.isAdult();
                                    parcel.setUserData(animalData);
                                    break;
                                default:
                                    parcel.setFill(Color.LIGHTGREEN);
                                    parcel.setUserData(null);
                            }
                        }
                    }
                }
            }
        }
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
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
                // Add transaction record
                Finance.recordSellTransaction("Resource", resource.toString(), 1, resource.getSellPrice());
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
        Button buyButton = new Button();
        Button transferButton = new Button("Transfer to Feed");

        int cropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        cropLabel.setText(cropType + ": " + cropCount + "/" + plantingController.getMaxCrops());

        // Update button text based on sale status
        Timeline updatePrice = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    int price = getCurrentPrice("Crop", cropType.toString());
                    if (price < GameEconomy.BUY_PRICE) {
                        buyButton.setText("Buy ($" + GameEconomy.BUY_PRICE + ") $" + price);
                        buyButton.setStyle("-fx-text-fill: red;");
                    } else {
                        buyButton.setText("Buy ($" + GameEconomy.BUY_PRICE + ")");
                        buyButton.setStyle("");
                    }
                })
        );
        updatePrice.setCycleCount(Timeline.INDEFINITE);
        updatePrice.play();

        sellButton.setOnAction(e -> handleCropSell(cropType, cropLabel, moneyDisplay));
        buyButton.setOnAction(e -> {
            int price = getCurrentPrice("Crop", cropType.toString());
            int currentCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
            if (currentCount < plantingController.getMaxCrops() && GameEconomy.spendMoney(price)) {
                plantingController.updateCropStorage(cropType, currentCount + 1);
                cropLabel.setText(cropType + ": " + (currentCount + 1) + "/" + plantingController.getMaxCrops());
                updateMoneyDisplays(moneyDisplay);
                Finance.recordBuyTransaction("Crop", cropType.toString(), 1, price);
            }
        });

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
            // Add transaction record
            Finance.recordSellTransaction("Crop", cropType.toString(), 1, GameEconomy.SELL_PRICE);
        }
    }

    private void handleCropBuy(CropType cropType, Label cropLabel, Text moneyDisplay) {
        int currentCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
        if (currentCount < plantingController.getMaxCrops() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE)) {
            plantingController.updateCropStorage(cropType, currentCount + 1);
            cropLabel.setText(cropType + ": " + (currentCount + 1) + "/" + plantingController.getMaxCrops());
            updateMoneyDisplays(moneyDisplay);
            // Add transaction record
            Finance.recordBuyTransaction("Crop", cropType.toString(), 1, GameEconomy.BUY_PRICE);
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
            // Add transaction record
            Finance.recordSellTransaction("Animal", animalType.toString(), 1, GameEconomy.SELL_PRICE * 2);
        }
    }

    private void handleAnimalBuy(AnimalType animalType, Label animalLabel, Text moneyDisplay) {
        int currentCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        if (currentCount < animalController.getMaxAnimals() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE * 3)) {
            animalController.updateAnimalStorage(animalType, currentCount + 1);
            animalLabel.setText(animalType + ": " + (currentCount + 1) + "/" + animalController.getMaxAnimals());
            updateMoneyDisplays(moneyDisplay);
            // Add transaction record
            Finance.recordBuyTransaction("Animal", animalType.toString(), 1, GameEconomy.BUY_PRICE * 3);
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