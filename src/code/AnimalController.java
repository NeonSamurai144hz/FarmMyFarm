package code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class AnimalController {
    private Map<AnimalType, Integer> animalStorage = new HashMap<>();
    private Map<CropType, Integer> feedStorage = new HashMap<>();
    private Map<Resource, Integer> resourceStorage = new HashMap<>();
    private final int MAX_RESOURCES = 100;
    private final int MAX_ANIMALS = 50;
    private final int MAX_FEED = 100;

    public class GrowthData {
        Timeline timeline;
        AnimalType animalType;
        int feedingStage;
        boolean isAdult;

        public GrowthData(Timeline timeline, AnimalType animalType) {
            this.timeline = timeline;
            this.animalType = animalType;
            this.feedingStage = 0;
            this.isAdult = false;
        }
    }

    public AnimalController() {
        for (Resource resource : Resource.values()) {
            resourceStorage.put(resource, 0);
        }
        // Initialize feed storage for required crops
        for (CropType cropType : CropType.values()) {
            feedStorage.put(cropType, 0);
        }
        // Initialize animal storage for each animal type
        for (AnimalType animalType : AnimalType.values()) {
            animalStorage.put(animalType, 0);
        }
    }

    /**
     * Places an animal on a parcel.
     */
    public boolean placeAnimal(Rectangle parcel) {
        System.out.println("AnimalController: Attempting to place animal");
        if (parcel.getUserData() instanceof GrowthData) {
            System.out.println("AnimalController: Parcel already contains animal data");
            return false;
        }

        AnimalType currentAnimal = AnimalSelection.getSelectedAnimalType();
        System.out.println("AnimalController: Selected animal type: " + currentAnimal);
        int currentCount = animalStorage.getOrDefault(currentAnimal, 0);
        System.out.println("AnimalController: Available animals in storage: " + currentCount);

        if (currentCount <= 0) {
            showAlert("Not enough animals", "You don't have any " + currentAnimal.toString().toLowerCase() + ".");
            return false;
        }

        animalStorage.put(currentAnimal, currentCount - 1);
        parcel.setFill(currentAnimal.getStage1Color());
        GrowthData data = new GrowthData(null, currentAnimal);
        parcel.setUserData(data);
        System.out.println("AnimalController: Animal placed successfully");
        return true;
    }

    /**
     * Feeds the animal on a parcel, incrementing its feeding stage.
     */
    public void feedAnimal(Rectangle parcel) {
        if (!(parcel.getUserData() instanceof GrowthData)) {
            System.out.println("AnimalController: No animal data found on parcel.");
            return;
        }

        GrowthData data = (GrowthData) parcel.getUserData();
        System.out.println("AnimalController: Current feedingStage = " + data.feedingStage);
        if (data.isAdult) {
            showAlert("Adult Animal", "This animal is fully grown and ready for collection!");
            return;
        }
        if (data.timeline != null && data.timeline.getStatus() == Timeline.Status.RUNNING) {
            System.out.println("AnimalController: Feed animation already running.");
            return;
        }
        if (checkAndConsumeFeed(data.animalType)) {
            data.feedingStage++;
            System.out.println("AnimalController: FeedingStage incremented to " + data.feedingStage);
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        parcel.setFill(data.animalType.getStage2Color());
                        System.out.println("AnimalController: Set to stage2 color");
                    }),
                    new KeyFrame(Duration.seconds(4), e -> {
                        data.timeline = null; // Do not reset feedingStage by replacing GrowthData
                        System.out.println("AnimalController: End of feed timeline, feedingStage = " + data.feedingStage);
                        if (isReadyToGrow(data)) {
                            setAnimalToAdult(parcel, data);
                        }
                    })
            );
            data.timeline = timeline;
            timeline.play();
        } else {
            showAlert("Missing Feed", "You need:\n" + getFeedRequirementText(data.animalType));
        }
    }

    private boolean isReadyToGrow(GrowthData data) {
        System.out.println("AnimalController: Checking readiness for " + data.animalType);
        switch (data.animalType) {
            case PIG: return data.feedingStage >= 2;
            case COW: return data.feedingStage >= 3;
            case CHICKEN: return data.feedingStage >= 1;
            default: return false;
        }
    }

    private void setAnimalToAdult(Rectangle parcel, GrowthData data) {
        data.isAdult = true;
        parcel.setFill(data.animalType.getStage3Color());
        showAlert("Animal Grown", "This " + data.animalType.toString().toLowerCase() + " is now fully grown and ready for collection!");
    }

    /**
     * Collects the product from an adult animal.
     */
    public void collectAnimalProduct(Rectangle parcel) {
        if (!(parcel.getUserData() instanceof GrowthData)) {
            return;
        }

        GrowthData data = (GrowthData) parcel.getUserData();
        if (!data.isAdult) {
            showAlert("Not Ready", "This animal needs more feeding to grow!");
            return;
        }

        Resource resource = Resource.getResourceForAnimal(data.animalType);
        if (resource != null && resourceStorage.get(resource) < MAX_RESOURCES) {
            resourceStorage.put(resource, resourceStorage.get(resource) + 1);
            data.feedingStage = 0; // Reset feeding stage
            data.isAdult = false;
            parcel.setFill(data.animalType.getStage1Color());
            showAlert("Resource Collected", "You collected 1 " + resource.toString().toLowerCase() + "!");
        }
    }

    public boolean confirmAnimalSell(AnimalType animalType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sell Animal");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to sell this " + animalType.toString().toLowerCase() + "?");
        return alert.showAndWait().orElse(null) == ButtonType.OK;
    }

    public Map<Resource, Integer> getResourceStorage() {
        return resourceStorage;
    }

    public void updateResourceStorage(Resource resource, int amount) {
        if (amount <= MAX_RESOURCES) {
            resourceStorage.put(resource, amount);
        }
    }

    public int getMaxResources() {
        return MAX_RESOURCES;
    }

    private int getProductValue(AnimalType animalType) {
        switch (animalType) {
            case PIG: return GameEconomy.SELL_PRICE * 3;
            case COW: return GameEconomy.SELL_PRICE * 4;
            case CHICKEN: return GameEconomy.SELL_PRICE * 2;
            default: return GameEconomy.SELL_PRICE;
        }
    }

    /**
     * Checks feed storage and consumes required feed for the given animal.
     */
    private boolean checkAndConsumeFeed(AnimalType animalType) {
        Map<CropType, Integer> requiredCrops = new HashMap<>();
        switch (animalType) {
            case PIG:
                requiredCrops.put(CropType.POTATOES, 1);
                requiredCrops.put(CropType.CARROTS, 1);
                break;
            case COW:
                requiredCrops.put(CropType.WHEAT, 2);
                break;
            case CHICKEN:
                requiredCrops.put(CropType.CARROTS, 1);
                break;
        }
        for (Map.Entry<CropType, Integer> requirement : requiredCrops.entrySet()) {
            if (feedStorage.getOrDefault(requirement.getKey(), 0) < requirement.getValue()) {
                System.out.println("AnimalController: Insufficient feed for " + requirement.getKey());
                return false;
            }
        }
        for (Map.Entry<CropType, Integer> requirement : requiredCrops.entrySet()) {
            CropType cropType = requirement.getKey();
            int currentAmount = feedStorage.get(cropType);
            feedStorage.put(cropType, currentAmount - requirement.getValue());
        }
        System.out.println("AnimalController: Feed consumed for " + animalType);
        return true;
    }

    private String getFeedRequirementText(AnimalType animalType) {
        switch (animalType) {
            case PIG: return "1 Potato and 1 Carrot";
            case COW: return "2 Wheat";
            case CHICKEN: return "1 Carrot";
            default: return "";
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Map<AnimalType, Integer> getAnimalStorage() {
        return animalStorage;
    }

    public void updateAnimalStorage(AnimalType animalType, int newAmount) {
        if (newAmount <= MAX_ANIMALS) {
            animalStorage.put(animalType, newAmount);
        }
    }

    public int getMaxAnimals() {
        return MAX_ANIMALS;
    }

    public Map<CropType, Integer> getFeedStorage() {
        return feedStorage;
    }

    public void updateFeedStorage(CropType cropType, int amount) {
        if (amount <= MAX_FEED) {
            feedStorage.put(cropType, amount);
        }
    }

    public int getFeedAmount(CropType cropType) {
        return feedStorage.getOrDefault(cropType, 0);
    }

    public int getMaxFeed() {
        return MAX_FEED;
    }
}
