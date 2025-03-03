module com.example.farmmyfarm {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.farmmyfarm to javafx.fxml;
    exports com.example.farmmyfarm;
}