package code;

import javafx.scene.paint.Color;

public enum CropType {
    WHEAT(Color.LIGHTGREEN, Color.GREEN, Color.DARKGREEN),
    CARROTS(Color.PEACHPUFF, Color.ORANGE, Color.DARKORANGE),
    POTATOES(Color.KHAKI, Color.YELLOW, Color.GOLD);

    private final Color stage1Color;
    private final Color stage2Color;
    private final Color stage3Color;

    CropType(Color stage1, Color stage2, Color stage3) {
        this.stage1Color = stage1;
        this.stage2Color = stage2;
        this.stage3Color = stage3;
    }

    public Color getStage1Color() { return stage1Color; }
    public Color getStage2Color() { return stage2Color; }
    public Color getStage3Color() { return stage3Color; }
}