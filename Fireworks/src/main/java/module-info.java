module com.hyperion.fireworks {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.hyperion.paintrandomizer;


    opens com.hyperion.fireworks to javafx.fxml;
    exports com.hyperion.fireworks;
}