package code;

import javafx.scene.paint.Color;

import java.util.Map;
import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class PlantingController {
    private Map<CropType, Integer> cropStorage = new HashMap<>();
    private final int MAX_CROPS = 100;

    class GrowthData {
        Timeline timeline;
        CropType cropType;

        public GrowthData(Timeline timeline, CropType cropType) {
            this.timeline = timeline;
            this.cropType = cropType;
        }
    }

    public void simulatePlantGrowth(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            return; // Already growing
        }

        CropType currentCrop = CropSelection.getSelectedCropType();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> parcel.setFill(currentCrop.getStage1Color())),
                new KeyFrame(Duration.seconds(4), e -> parcel.setFill(currentCrop.getStage2Color())),
                new KeyFrame(Duration.seconds(6), e -> {
                    parcel.setFill(currentCrop.getStage3Color());
                    parcel.setUserData(new GrowthData(null, currentCrop));
                })
        );

        parcel.setUserData(new GrowthData(timeline, currentCrop));
        timeline.play();
    }

    public void openCropSelectionModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Select Crop Type");

        VBox modalLayout = new VBox(10);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setAlignment(Pos.CENTER);

        Button wheatButton = new Button("Wheat");
        wheatButton.setOnAction(e -> {
            CropSelection.setSelectedCropType(CropType.WHEAT);
            modalStage.close();
            System.out.println("Selected crop type: Wheat");
        });

        Button carrotsButton = new Button("Carrots");
        carrotsButton.setOnAction(e -> {
            CropSelection.setSelectedCropType(CropType.CARROTS);
            modalStage.close();
            System.out.println("Selected crop type: Carrots");
        });

        Button potatoesButton = new Button("Potatoes");
        potatoesButton.setOnAction(e -> {
            CropSelection.setSelectedCropType(CropType.POTATOES);
            modalStage.close();
            System.out.println("Selected crop type: Potatoes");
        });

        modalLayout.getChildren().addAll(wheatButton, carrotsButton, potatoesButton);

        Scene scene = new Scene(modalLayout, 200, 150);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    public void harvestCrop(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            GrowthData data = (GrowthData) parcel.getUserData();
            if (data.timeline == null) { // Fully grown
                // Harvest the crop
                int currentCount = cropStorage.getOrDefault(data.cropType, 0);
                if (currentCount < MAX_CROPS) {
                    cropStorage.put(data.cropType, currentCount + 1);
                    GameEconomy.addMoney(5);  // Reward for harvesting
                }
                // Reset the parcel
                parcel.setFill(Color.LIGHTGREEN);
                parcel.setUserData(null);
            }
        }
    }

    public Map<CropType, Integer> getCropStorage() {
        return cropStorage;
    }

    public int getMaxCrops() {
        return MAX_CROPS;
    }

    public void updateCropStorage(CropType cropType, int newAmount) {
        cropStorage.put(cropType, newAmount);
    }
}