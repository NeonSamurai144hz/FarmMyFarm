package code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class FarmGridController {

    @FXML
    private GridPane farmGrid;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: Initialize method called!");

        if (farmGrid == null) {
            System.err.println("ERROR: GridPane is NULL!");
            return;
        }

        for (Node node : farmGrid.getChildren()) {
            if (node instanceof Pane) {
                addClickHandler((Pane) node);
            }
        }
    }

    private void addClickHandler(Pane plot) {
        plot.setPickOnBounds(true);
        plot.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick(plot);
            }
            event.consume();
        });
    }


    private void handleLeftClick(Pane plot) {
        String style = plot.getStyle();
        if (style != null && style.contains("darkgreen")) {
            System.out.println(CropSelection.getSelectedCropType() + " harvested!");
            plot.setStyle("-fx-background-color: lightgreen;");
            return;
        }

        if (plot.getUserData() instanceof Timeline) {
            return;
        }

        System.out.println("Planting " + CropSelection.getSelectedCropType() + " on plot.");

        Timeline growthTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> plot.setStyle("-fx-background-color: yellowgreen;")),
                new KeyFrame(Duration.seconds(4), e -> plot.setStyle("-fx-background-color: green;")),
                new KeyFrame(Duration.seconds(6), e -> {
                    plot.setStyle("-fx-background-color: darkgreen;");
                    plot.setUserData(null);
                })
        );
        plot.setUserData(growthTimeline);
        growthTimeline.play();
    }
}