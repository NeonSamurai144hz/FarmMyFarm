package code;

public class AnimalSelection {
    private static AnimalType selectedAnimalType;

    public static AnimalType getSelectedAnimalType() {
        return selectedAnimalType;
    }

    public static void setSelectedAnimalType(AnimalType animalType) {
        selectedAnimalType = animalType;
    }
}