module DNSSpeedBenchmark {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j.core;
    requires java.desktop;
    requires org.dnsjava;
    requires org.jfree.jfreechart;
    requires com.fasterxml.jackson.databind;
    requires java.management;
    requires java.rmi;

    opens app.dns to javafx.fxml;
    opens app.dns.controller to javafx.fxml;
    opens app.dns.model to javafx.fxml;
    opens app.dns.model.util.core to javafx.fxml;

    exports app.dns;
    exports app.dns.model;
    exports app.dns.model.util.core;
    exports app.dns.model.util.jmx.mbeans;
}