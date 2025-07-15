package app.dns;

import app.dns.model.util.jmx.JMXServer;
import app.dns.model.util.properties.Configs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MainApp extends Application {
    private static Logger logger = LogManager.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/app/dns/DNSBenchmark.fxml"));
        primaryStage.setResizable(false);
        primaryStage.setTitle("DNS Viewer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            Configs.getInstance().loadValues();
            JMXServer.getInstance().startJMXServer();
            launch(args);
        } catch (IOException e) {
            logger.error("Error reading configuration file", e);
            throw new RuntimeException("Could not read configuration file", e);
        }
    }
}
