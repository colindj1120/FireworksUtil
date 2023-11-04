module com.fireworks.fireworkssimulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.hyperion.fireworks;


    opens com.fireworks to javafx.fxml;
    exports com.fireworks;
}