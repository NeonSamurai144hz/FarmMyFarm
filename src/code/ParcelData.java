package code;

import java.io.Serializable;

public class ParcelData implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ParcelType {
        EMPTY, GROWING_CROP, READY_CROP, GROWING_ANIMAL, ADULT_ANIMAL
    }

    private ParcelType type;
    private CropType cropType;
    private AnimalType animalType;
    private int growthStage;
    private boolean isAdult;

    public ParcelData(ParcelType type) {
        this.type = type;
    }

    // Getters and setters
    public ParcelType getType() { return type; }
    public CropType getCropType() { return cropType; }
    public AnimalType getAnimalType() { return animalType; }
    public int getGrowthStage() { return growthStage; }
    public boolean isAdult() { return isAdult; }

    public void setType(ParcelType type) { this.type = type; }
    public void setCropType(CropType cropType) { this.cropType = cropType; }
    public void setAnimalType(AnimalType animalType) { this.animalType = animalType; }
    public void setGrowthStage(int growthStage) { this.growthStage = growthStage; }
    public void setAdult(boolean adult) { isAdult = adult; }
}