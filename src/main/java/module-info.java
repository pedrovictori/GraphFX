module GraphFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jgrapht.core;

    opens graphs to javafx.fxml;
    exports graphs;
}