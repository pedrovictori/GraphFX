module GraphFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jgrapht.core;

    opens core to javafx.fxml;
    exports core;
}