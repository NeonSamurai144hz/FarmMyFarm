package code;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FarmGridController {
    @FXML
    private GridPane Map;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: Initialize method called!");

        if (Map == null) {
            System.err.println("ERROR: GridPane is NULL!");
            return;
        }

        // Add click handler to existing children
        for (Node node : Map.getChildren()) {
            if (node instanceof Pane) {
                addClickHandler((Pane)node);
            }
        }

        // Add global GridPane click handler for additional debugging
        Map.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onGridPaneClicked);
    }

    private void addClickHandler(Pane plot) {
        plot.setPickOnBounds(true);

        plot.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Integer row = GridPane.getRowIndex(plot);
            Integer col = GridPane.getColumnIndex(plot);

            System.out.println("DEBUG: Plot clicked at row: " +
                    (row != null ? row : "N/A") +
                    ", col: " + (col != null ? col : "N/A"));

            handlePlotClick(plot);
            event.consume();
        });
    }

    private void onGridPaneClicked(MouseEvent event) {
        Node clickedNode = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;

        if (clickedNode != null) {
            Integer row = GridPane.getRowIndex(clickedNode);
            Integer col = GridPane.getColumnIndex(clickedNode);

            System.out.println("DEBUG: GridPane global click at row: " +
                    (row != null ? row : "N/A") +
                    ", col: " + (col != null ? col : "N/A"));
        }
    }

    private void handlePlotClick(Pane plot) {
        System.out.println("DEBUG: Handling plot click");

        // Add your plot interaction logic here
        // For now, just print a message
        System.out.println("Plot clicked and processed!");
    }
}