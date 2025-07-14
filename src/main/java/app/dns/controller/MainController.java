package app.dns.controller;

import app.dns.model.entity.DNSResult;
import app.dns.model.util.JSONReader;
import app.dns.model.core.DNSBenchmark;
import app.dns.model.util.ProgressListener;
import app.dns.model.util.jchart.Charts;
import app.dns.model.entity.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MainController {
    private static Logger logger = LogManager.getLogger(MainController.class);
    @FXML
    private ListView<String> dnsResolversView;
    @FXML
    private ListView<String> domainsView;
    @FXML
    private MenuButton menuButtonDomains;
    @FXML
    private Button startBenchmarkButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    public void initialize() {
        dnsResolversView.setItems(FXCollections.observableArrayList(loadDNSResolvers()));

        for (MenuItem item : loadDomainTypes()) {
            item.setOnAction(event -> onMenuItemSelected(item));
            menuButtonDomains.getItems().add(item);
        }

        startBenchmarkButton.setOnAction(event -> startBenchmark());
    }

    public MainController() {}

    public String[] loadDNSResolvers() {
        String[] ipAddresses = JSONReader.getAllDNSResolversAddresses();
        if (ipAddresses != null) {
            logger.info("DNS Resolvers loaded from config.");
            return ipAddresses;
        } else {
            logger.error("Resolvers could not be found in config.");
        }
        return null;
    }

    public List<MenuItem> loadDomainTypes() {
        List<MenuItem> menuItems = new ArrayList<>();
        for (String string : Type.getNames()) {
            menuItems.add(new MenuItem(string));
        }
        return menuItems;
    }

    public void onMenuItemSelected(MenuItem item) {
        String selectedDomainType = item.getText();
        ObservableList<String> filteredDomains = FXCollections.observableArrayList(filterDomainsByType(selectedDomainType));
        this.domainsView.setItems(filteredDomains);
        this.menuButtonDomains.setText(item.getText());
    }

    public String[] filterDomainsByType(String domainType) {
        String[] domains = JSONReader.getDomainsByDomainName(domainType);
        if (domains != null) {
            logger.info("Domains loaded from config.");
            return domains;
        } else {
            logger.error("Domains not found in config.");
            return null;
        }
    }

    public void startBenchmark() {
        int domainType = Type.getNumberByName(menuButtonDomains.getText());
        if (domainType == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Input");
            alert.setHeaderText(null);
            alert.setContentText("Please select a domain type.");
            alert.showAndWait();
            return;
        }

        logger.info("Starting benchmark...");
        startBenchmarkButton.setDisable(true);

        Task<List<DNSResult>> benchmarkTask = new Task<>() {
            @Override
            protected List<DNSResult> call() {
//                    DNSBenchmark dnsBenchmark = new DNSBenchmark((progress) -> {
//                        updateProgress(progress, 1.0);
//                        logger.info("progress: {}", progress * 100);
//                    });
                DNSBenchmark dnsBenchmark = new DNSBenchmark(new ProgressListener() {
                    @Override
                    public void updateTaskProgress(double progress) {
                        updateProgress(progress, 1.0);
                        logger.info("progress: {}", progress * 100);
                    }
                });
                return dnsBenchmark.execute(
                        JSONReader.getAllDNSResolversAddresses(),
                        JSONReader.getDomainsByDomainName(Type.getNameByNumber(domainType)));
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                List<DNSResult> results = getValue();
                Charts.getInstance().generateDNSPerformanceChart(results);
                startBenchmarkButton.setDisable(false);
                updateProgress(0.0, 1.0);
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable error = getException();
                logger.error("Benchmark failed: ", error);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Benchmark Failed");
                alert.setContentText("An error occurred during the benchmark: " + error.getMessage());
                alert.showAndWait();
                startBenchmarkButton.setDisable(false);
            }

            @Override
            protected void updateProgress(double workDone, double max) {
                super.updateProgress(workDone, max);
            }
        };

        progressBar.progressProperty().bind(benchmarkTask.progressProperty());

        Thread thread = new Thread(benchmarkTask);
        thread.setDaemon(true); // Allow the application to exit even if this thread is running
        thread.start();
    }
}
