package code;

import javafx.scene.paint.Color;

public enum AnimalType {
    PIG(Color.PINK, Color.LIGHTPINK, Color.HOTPINK),
    COW(Color.WHITE, Color.LIGHTGRAY, Color.GRAY),
    CHICKEN(Color.LIGHTYELLOW, Color.YELLOW, Color.GOLD);

    private final Color stage1Color;
    private final Color stage2Color;
    private final Color stage3Color;

    AnimalType(Color stage1, Color stage2, Color stage3) {
        this.stage1Color = stage1;
        this.stage2Color = stage2;
        this.stage3Color = stage3;
    }

    public Color getStage1Color() { return stage1Color; }
    public Color getStage2Color() { return stage2Color; }
    public Color getStage3Color() { return stage3Color; }
}
