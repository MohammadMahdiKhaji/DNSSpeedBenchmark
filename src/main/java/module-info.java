module app.dns {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j.core;
    requires java.desktop;
    requires org.dnsjava;
    requires org.jfree.jfreechart;

    opens app.dns to javafx.fxml;
    opens app.dns.controller to javafx.fxml;
    opens app.dns.model to javafx.fxml;
    exports app.dns;
    exports app.dns.model;
}