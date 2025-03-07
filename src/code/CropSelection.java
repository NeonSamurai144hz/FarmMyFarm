package code;

public class CropSelection {
    private static CropType selectedCropType = CropType.WHEAT;

    public static CropType getSelectedCropType() {
        return selectedCropType;
    }

    public static void setSelectedCropType(CropType cropType) {
        selectedCropType = cropType;
    }
}