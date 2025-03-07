package code;

public class AnimalSelection {
    private static AnimalType selectedAnimalType = AnimalType.PIG;

    public static AnimalType getSelectedAnimalType() {
        return selectedAnimalType;
    }

    public static void setSelectedAnimalType(AnimalType animalType) {
        selectedAnimalType = animalType;
    }
}
