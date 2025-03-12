package code;

import javafx.scene.control.Alert;
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

    public boolean simulatePlantGrowth(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            return false;
        }

        CropType currentCrop = CropSelection.getSelectedCropType();

            // check for seeds
        int currentCount = cropStorage.getOrDefault(currentCrop, 0);
        if (currentCount <= 0) {
            showAlert("Not enough seeds", "You don't have any " + currentCrop.toString().toLowerCase() + " seeds.");
            return false;
        }

        cropStorage.put(currentCrop, currentCount - 1);

            // growth animation
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
        return true;
    }
        //alert if no seeds in store
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
        //chose crop type
    public void openCropSelectionModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Select Crop Type");

        VBox modalLayout = new VBox(10);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setAlignment(Pos.CENTER);

        for (CropType cropType : CropType.values()) {
            int count = cropStorage.getOrDefault(cropType, 0);
            Button cropButton = new Button(cropType.toString() + " (" + count + ")");

            cropButton.setDisable(count <= 0);

            cropButton.setOnAction(e -> {
                CropSelection.setSelectedCropType(cropType);
                modalStage.close();
                System.out.println("Selected crop type: " + cropType);
            });

            modalLayout.getChildren().add(cropButton);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modalStage.close());
        modalLayout.getChildren().add(closeButton);

        Scene scene = new Scene(modalLayout, 200, 200);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }
        //allows crop harvesting
    public void harvestCrop(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            GrowthData data = (GrowthData) parcel.getUserData();
            if (data.timeline == null) {
                int currentCount = cropStorage.getOrDefault(data.cropType, 0);
                if (currentCount < MAX_CROPS) {
                    cropStorage.put(data.cropType, currentCount + 1);
                }
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