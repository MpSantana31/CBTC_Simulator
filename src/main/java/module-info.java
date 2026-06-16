module org.example.cbtc_simulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    exports org.example.cbtc_simulator;
    exports org.example.cbtc_simulator.model;
    exports org.example.cbtc_simulator.dao;
    exports org.example.cbtc_simulator.service;
    exports org.example.cbtc_simulator.view.controller;

    opens org.example.cbtc_simulator to javafx.fxml;
    opens org.example.cbtc_simulator.model to javafx.fxml;
    opens org.example.cbtc_simulator.view.controller to javafx.fxml;
    opens org.example.cbtc_simulator.view to javafx.fxml;
}
