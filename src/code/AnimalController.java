package code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class AnimalController {
    private Map<AnimalType, Integer> animalStorage = new HashMap<>();
    private final int MAX_ANIMALS = 50;

    class GrowthData {
        Timeline timeline;
        AnimalType animalType;

        public GrowthData(Timeline timeline, AnimalType animalType) {
            this.timeline = timeline;
            this.animalType = animalType;
        }
    }

    public void placeAnimal(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            return;
        }

        AnimalType currentAnimal = AnimalSelection.getSelectedAnimalType();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> parcel.setFill(currentAnimal.getStage1Color())),
                new KeyFrame(Duration.seconds(4), e -> parcel.setFill(currentAnimal.getStage2Color())),
                new KeyFrame(Duration.seconds(6), e -> {
                    parcel.setFill(currentAnimal.getStage3Color());
                    parcel.setUserData(new GrowthData(null, currentAnimal));
                })
        );

        parcel.setUserData(new GrowthData(timeline, currentAnimal));
        timeline.play();
    }

    public void openAnimalSelectionModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Select Animal Type");

        VBox modalLayout = new VBox(10);
        modalLayout.setPadding(new Insets(20));
        modalLayout.setAlignment(Pos.CENTER);

        Button pigButton = new Button("Pig");
        pigButton.setOnAction(e -> {
            AnimalSelection.setSelectedAnimalType(AnimalType.PIG);
            modalStage.close();
            System.out.println("Selected animal type: Pig");
        });

        Button cowButton = new Button("Cow");
        cowButton.setOnAction(e -> {
            AnimalSelection.setSelectedAnimalType(AnimalType.COW);
            modalStage.close();
            System.out.println("Selected animal type: Cow");
        });

        Button chickenButton = new Button("Chicken");
        chickenButton.setOnAction(e -> {
            AnimalSelection.setSelectedAnimalType(AnimalType.CHICKEN);
            modalStage.close();
            System.out.println("Selected animal type: Chicken");
        });

        modalLayout.getChildren().addAll(pigButton, cowButton, chickenButton);

        Scene scene = new Scene(modalLayout, 200, 150);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    public void collectAnimalProduct(Rectangle parcel) {
        if (parcel.getUserData() instanceof GrowthData) {
            GrowthData data = (GrowthData) parcel.getUserData();
            if (data.timeline == null) {
                int currentCount = animalStorage.getOrDefault(data.animalType, 0);
                if (currentCount < MAX_ANIMALS) {
                    animalStorage.put(data.animalType, currentCount + 1);
                    GameEconomy.addMoney(10);
                }
                parcel.setFill(data.animalType.getStage2Color());
                Timeline newTimeline = new Timeline(
                        new KeyFrame(Duration.seconds(8), e -> {
                            parcel.setFill(data.animalType.getStage3Color());
                            parcel.setUserData(new GrowthData(null, data.animalType));
                        })
                );
                parcel.setUserData(new GrowthData(newTimeline, data.animalType));
                newTimeline.play();
            }
        }
    }

    public Map<AnimalType, Integer> getAnimalStorage() {
        return animalStorage;
    }

    public int getMaxAnimals() {
        return MAX_ANIMALS;
    }

    public void updateAnimalStorage(AnimalType animalType, int newAmount) {
        animalStorage.put(animalType, newAmount);
    }
}
