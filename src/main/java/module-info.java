module com.example.hundirlaflota {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens com.example.hundirlaflota to javafx.fxml;
    exports com.example.hundirlaflota;
    exports com.example.hundirlaflota.controller;
    opens com.example.hundirlaflota.controller to javafx.fxml;
}