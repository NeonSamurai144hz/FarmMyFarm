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
    @FXML private Text weatherText;

    private static final int GRID_SIZE = 16;
    private double cellSize = 32;
    private Timeline saleTimer;
    private Sale currentSale;
    private Timeline weatherTimer;
    private Weather currentWeather;
    private PlayerLevel playerLevel;

    private PlantingController plantingController = new PlantingController();
    private AnimalController animalController = new AnimalController();
    private boolean isAnimalMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GameEconomy.setGameController(this);
        playerLevel = new PlayerLevel();
        PlayerLevel.setGameController(this);

        initializeHUD();
        initializeResponsiveSizing();
        createGrid();
        setupButtonListeners();
        setMoney(GameEconomy.getPlayerMoney());
        setupSaleTimer();
        setupWeatherSystem();
    }

    private void initializeHUD() {
        usernameText.setText("Player");
        levelText.setText("Level: " + playerLevel.getLevel());
        moneyText.setText("Money: " + GameEconomy.getPlayerMoney());
        expText.setText("Exp to next level: " + playerLevel.getCurrentExp() + "/" + playerLevel.getExpToNextLevel());
        weatherText.setText(Weather.SUNNY.getSymbol() + " Sunny");
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

        Button saveButton = new Button("Save Game");
        Button loadButton = new Button("Load Game");

        saveButton.setOnAction(event -> saveGame());
        loadButton.setOnAction(event -> loadGame());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(saveButton, loadButton);

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

        if (Math.random() < 0.5) {
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

    private void setupWeatherSystem() {
        currentWeather = Weather.SUNNY;
        weatherTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> tryChangeWeather())
        );
        weatherTimer.setCycleCount(Timeline.INDEFINITE);
        weatherTimer.play();
    }

    private void tryChangeWeather() {
        if (Math.random() < 0.05) { // 5% chance every second
            Weather newWeather;
            if (currentWeather == Weather.SUNNY) {
                newWeather = Math.random() < 0.5 ? Weather.RAINY : Weather.DROUGHT;
                changeWeather(newWeather);

                Timeline resetTimer = new Timeline(
                        new KeyFrame(Duration.seconds(60), e -> changeWeather(Weather.SUNNY))
                );
                resetTimer.play();
            }
        }
    }

    private void changeWeather(Weather weather) {
        currentWeather = weather;
        PlantingController.setCurrentWeather(weather);
        weatherText.setText(weather.getSymbol() + " " + weather.name().charAt(0) +
                weather.name().substring(1).toLowerCase());

        String color = switch (weather) {
            case SUNNY -> "-fx-fill: orange;";
            case RAINY -> "-fx-fill: blue;";
            case DROUGHT -> "-fx-fill: red;";
        };
        weatherText.setStyle(color);

        showWeatherNotification(weather);
    }

    private void showWeatherNotification(Weather weather) {
        Stage notification = new Stage();
        notification.initModality(Modality.NONE);
        notification.setTitle("Weather Change!");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Text weatherInfo = new Text(weather.getSymbol() + " " + weather.name());
        Text effectInfo = new Text(switch (weather) {
            case SUNNY -> "Normal growing conditions";
            case RAINY -> "Plants grow 30% faster!";
            case DROUGHT -> "Plants grow 50% slower!";
        });

        content.getChildren().addAll(weatherInfo, effectInfo);
        Scene scene = new Scene(content);
        notification.setScene(scene);
        notification.show();

        Timeline autoClose = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> notification.close())
        );
        autoClose.play();
    }

    private void saveGame() {
        GameSaveData saveData = new GameSaveData(
                GameEconomy.getPlayerMoney(),
                plantingController.getCropStorage(),
                animalController.getAnimalStorage(),
                animalController.getFeedStorage(),
                animalController.getResourceStorage(),
                Finance.getTransactions(),
                buildGridData(),
                playerLevel
        );
        GameSaveManager.saveGame(saveData);
    }

    private void loadGame() {
        GameSaveData saveData = GameSaveManager.loadGame();
        if (saveData != null) {
            GameEconomy.setPlayerMoney(saveData.getPlayerMoney());

            for (CropType cropType : CropType.values()) {
                plantingController.updateCropStorage(cropType,
                        saveData.getCropStorage().getOrDefault(cropType, 0));
            }
            for (AnimalType animalType : AnimalType.values()) {
                animalController.updateAnimalStorage(animalType,
                        saveData.getAnimalStorage().getOrDefault(animalType, 0));
            }
            for (CropType cropType : CropType.values()) {
                animalController.updateFeedStorage(cropType,
                        saveData.getFeedStorage().getOrDefault(cropType, 0));
            }
            for (Resource resource : Resource.values()) {
                animalController.updateResourceStorage(resource,
                        saveData.getResourceStorage().getOrDefault(resource, 0));
            }

            Finance.setTransactions(saveData.getTransactions());

            ParcelData[][] gridData = saveData.getGridData();
            if (gridData != null) {
                for (int row = 0; row < GRID_SIZE; row++) {
                    for (int col = 0; col < GRID_SIZE; col++) {
                        Node node = getNodeFromGridPane(farmGrid, col, row);
                        if (node instanceof Rectangle) {
                            Rectangle parcel = (Rectangle) node;
                            ParcelData data = gridData[row][col];
                            updateParcelFromData(parcel, data);
                        }
                    }
                }
            }

            // Load player level
            playerLevel = saveData.getPlayerLevel();
            PlayerLevel.setGameController(this);
            setLevel(playerLevel.getLevel());
            setExp(playerLevel.getCurrentExp(), playerLevel.getExpToNextLevel());
        }
    }

    private ParcelData[][] buildGridData() {
        ParcelData[][] gridData = new ParcelData[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Node node = getNodeFromGridPane(farmGrid, col, row);
                if (node instanceof Rectangle) {
                    Rectangle parcel = (Rectangle) node;
                    ParcelData parcelData;

                    Object userData = parcel.getUserData();
                    if (userData instanceof PlantingController.GrowthData) {
                        PlantingController.GrowthData growthData = (PlantingController.GrowthData) userData;
                        parcelData = new ParcelData(ParcelData.ParcelType.GROWING_CROP);
                        parcelData.setCropType(growthData.cropType);
                        if (growthData.timeline == null) {
                            parcelData.setType(ParcelData.ParcelType.READY_CROP);
                        }
                    } else if (userData instanceof AnimalController.GrowthData) {
                        AnimalController.GrowthData growthData = (AnimalController.GrowthData) userData;
                        parcelData = new ParcelData(ParcelData.ParcelType.GROWING_ANIMAL);
                        parcelData.setAnimalType(growthData.animalType);
                        parcelData.setAdult(growthData.isAdult);
                        if (growthData.isAdult) {
                            parcelData.setType(ParcelData.ParcelType.ADULT_ANIMAL);
                        }
                    } else {
                        parcelData = new ParcelData(ParcelData.ParcelType.EMPTY);
                    }

                    gridData[row][col] = parcelData;
                }
            }
        }
        return gridData;
    }

    private void updateParcelFromData(Rectangle parcel, ParcelData data) {
        if (data == null || data.getType() == ParcelData.ParcelType.EMPTY) {
            parcel.setFill(Color.LIGHTGREEN);
            parcel.setUserData(null);
            return;
        }

        switch (data.getType()) {
            case GROWING_CROP:
                parcel.setFill(data.getCropType().getStage1Color());
                parcel.setUserData(new PlantingController.GrowthData(null, data.getCropType()));
                break;

            case READY_CROP:
                parcel.setFill(data.getCropType().getStage3Color());
                parcel.setUserData(new PlantingController.GrowthData(null, data.getCropType()));
                break;

            case GROWING_ANIMAL:
                parcel.setFill(data.getAnimalType().getStage1Color());
                AnimalController.GrowthData animalData = new AnimalController.GrowthData(null, data.getAnimalType());
                animalData.isAdult = false;
                parcel.setUserData(animalData);
                break;

            case ADULT_ANIMAL:
                parcel.setFill(data.getAnimalType().getStage3Color());
                AnimalController.GrowthData adultData = new AnimalController.GrowthData(null, data.getAnimalType());
                adultData.isAdult = true;
                parcel.setUserData(adultData);
                break;
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

        Tab cropsTab = new Tab("Crops");
        Tab animalsTab = new Tab("Animals");
        Tab feedTab = new Tab("Feed");
        Tab resourcesTab = new Tab("Resources");

        cropsTab.setContent(createCropsContent());
        animalsTab.setContent(createAnimalsContent());
        feedTab.setContent(createFeedContent());
        resourcesTab.setContent(createResourcesContent());

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
                resourceLabel.setText(resource + ": "
                        + (count - 1) +
                        "/" + animalController.getMaxResources());
                updateMoneyDisplays(moneyDisplay);
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

        Text requirementsTitle = new Text("Feed Requirements:");
        Text pigReq = new Text("Pig: 1 Potato + 1 Carrot");
        Text cowReq = new Text("Cow: 2 Wheat");
        Text chickenReq = new Text("Chicken: 1 Carrot");
        content.getChildren().addAll(requirementsTitle, pigReq, cowReq, chickenReq, new Text(""));

        Map<CropType, Label> cropLabels = new HashMap<>();
        Map<CropType, Label> feedLabels = new HashMap<>();

        for (CropType cropType : CropType.values()) {
            HBox feedRow = new HBox(10);
            feedRow.setAlignment(Pos.CENTER);

            Label cropLabel = new Label();
            Label feedLabel = new Label();

            cropLabels.put(cropType, cropLabel);
            feedLabels.put(cropType, feedLabel);

            updateLabels(cropType, cropLabel, feedLabel);

            Button transferButton = new Button("Transfer to Feed");
            transferButton.setOnAction(e -> {
                int currentCropCount = plantingController.getCropStorage().getOrDefault(cropType, 0);
                int currentFeedCount = animalController.getFeedAmount(cropType);

                if (currentCropCount > 0 && currentFeedCount < animalController.getMaxFeed()) {
                    plantingController.updateCropStorage(cropType, currentCropCount - 1);
                    animalController.updateFeedStorage(cropType, currentFeedCount + 1);

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
            int sellPrice = getCurrentPrice("Crop", cropType.toString());

            plantingController.updateCropStorage(cropType, currentCount - 1);
            GameEconomy.addMoney(sellPrice);

            cropLabel.setText(cropType + ": " + (currentCount - 1) + "/" + plantingController.getMaxCrops());
            updateMoneyDisplays(moneyDisplay);

            Finance.recordSellTransaction("Crop", cropType.toString(), 1, sellPrice);

            playerLevel.addExp(5);
        }
    }

    private void handleAnimalSell(AnimalType animalType, Label animalLabel, Text moneyDisplay) {
        int currentCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        if (currentCount > 0) {
            int sellPrice = getCurrentPrice("Animal", animalType.toString());

            animalController.updateAnimalStorage(animalType, currentCount - 1);
            GameEconomy.addMoney(sellPrice);

            animalLabel.setText(animalType + ": " + (currentCount - 1) + "/" + animalController.getMaxAnimals());
            updateMoneyDisplays(moneyDisplay);

            Finance.recordSellTransaction("Animal", animalType.toString(), 1, sellPrice);

            playerLevel.addExp(5);
        }
    }

    private void handleAnimalBuy(AnimalType animalType, Label animalLabel, Text moneyDisplay) {
        int currentCount = animalController.getAnimalStorage().getOrDefault(animalType, 0);
        if (currentCount < animalController.getMaxAnimals() && GameEconomy.spendMoney(GameEconomy.BUY_PRICE * 3)) {
            animalController.updateAnimalStorage(animalType, currentCount + 1);
            animalLabel.setText(animalType + ": " + (currentCount + 1) + "/" + animalController.getMaxAnimals());
            updateMoneyDisplays(moneyDisplay);
            Finance.recordBuyTransaction("Animal", animalType.toString(), 1, GameEconomy.BUY_PRICE * 3);
        }
    }

    private void updateMoneyDisplays(Text moneyDisplay) {
        moneyDisplay.setText("Money: $" + GameEconomy.getPlayerMoney());
        moneyText.setText("Money: " + GameEconomy.getPlayerMoney());
    }

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