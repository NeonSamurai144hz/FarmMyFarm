import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/resources/main_game.fxml"));

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root,
                screenBounds.getWidth() * 0.9,
                screenBounds.getHeight() * 0.9
        );

        primaryStage.setTitle("Farm to Farm");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}







    // OG Attempt
//            // Load the map FXML
//        FXMLLoader mapLoader = new FXMLLoader(getClass().getResource("/resources/Map.fxml"));
//        Parent mapRoot = mapLoader.load();
//        FarmGridController mapController = mapLoader.getController();
//
//        // Load the HUD FXML
//        FXMLLoader hudLoader = new FXMLLoader(getClass().getResource("/resources/HUD.fxml"));
//        Parent hudRoot = hudLoader.load();
//                hudRoot.setMouseTransparent(false);
//
//        // Create a StackPane to layer the map and HUD
//        StackPane rootPane = new StackPane(mapRoot, hudRoot);
//
//        // Create the scene
//        Scene scene = new Scene(rootPane, 1020, 580);
//
//        // Set up the stage
//        stage.setTitle("Farm to Farm");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void stop() {
//        // Optional: Add cleanup for any resources
//        // For example, if you have a controller with an executor service
//        // mapController.cleanup();
//    }
//    @Override
//    public void start(Stage stage) throws Exception {
//


//    // Player Movement
//        try {
//Parent root = FXMLLoader.load(getClass().getResource("/resources/Main.fxml"));
//Scene scene = new Scene(root);
//            stage.setScene(scene);
//            stage.show();
//        } catch (Exception e) {
//        e.printStackTrace();
//        }




//// working map and plant
//
//private static final int GRID_SIZE = 32;
//private static final int TILE_SIZE = 40;
//private final Tile[][] tiles = new Tile[GRID_SIZE][GRID_SIZE];
//
//@Override
//public void start(Stage primaryStage) {
//    GridPane grid = createGameGrid();
//    StackPane root = new StackPane(grid);
//    Scene scene = new Scene(root, 800, 600);
//
//    primaryStage.setTitle("Farm to Farm");
//    primaryStage.setScene(scene);
//    primaryStage.show();
//}
//
//private GridPane createGameGrid() {
//    GridPane grid = new GridPane();
//    grid.setPadding(new Insets(10));
//    grid.setHgap(1);
//    grid.setVgap(1);
//
//    for (int row = 0; row < GRID_SIZE; row++) {
//        for (int col = 0; col < GRID_SIZE; col++) {
//            Tile tile = new Tile(TILE_SIZE);
//            tiles[row][col] = tile;
//            grid.add(tile, col, row);
//        }
//    }
//    return grid;
//}
//
//public static void main(String[] args) {
//    launch(args);
//}
//
//static class Tile extends StackPane {
//    private final Rectangle rect;
//    private GrowthStage stage;
//
//    public Tile(int size) {
//        rect = new Rectangle(size, size);
//        rect.setFill(Color.BROWN);
//        rect.setStroke(Color.BLACK);
//        getChildren().add(rect);
//        stage = GrowthStage.UNPLANTED;
//
//        setOnMouseClicked(e -> startGrowthCycle());
//    }
//
//    private void startGrowthCycle() {
//        if (stage != GrowthStage.UNPLANTED) return;
//
//        stage = GrowthStage.GROWING;
//        Timeline growthTimeline = new Timeline(
//                new KeyFrame(Duration.seconds(2),
//                        e -> updateAppearance(Color.LIGHTGREEN, GrowthStage.STAGE1)),
//                new KeyFrame(Duration.seconds(4),
//                        e -> updateAppearance(Color.FORESTGREEN, GrowthStage.STAGE2)),
//                new KeyFrame(Duration.seconds(6),
//                        e -> updateAppearance(Color.GOLD, GrowthStage.READY))
//        );
//        growthTimeline.play();
//    }
//
//    private void updateAppearance(Color color, GrowthStage newStage) {
//        rect.setFill(color);
//        stage = newStage;
//    }
//
//    enum GrowthStage {
//        UNPLANTED, GROWING, STAGE1, STAGE2, READY
//    }
//}



// claude - working plant
//@Override
//public void start(Stage primaryStage) throws Exception {
//    // Load the FXML
//    Parent root = FXMLLoader.load(getClass().getResource("/resources/main_game.fxml"));
//
//    // Create the scene
//    Scene scene = new Scene(root, 800, 600);
//
//    // Set up the stage
//    primaryStage.setTitle("Farm to Farm");
//    primaryStage.setScene(scene);
//    primaryStage.show();
//}
//
//public static void main(String[] args) {
//    launch(args);
//}