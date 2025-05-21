package app.dns.controller;

import app.dns.model.entity.DNSResult;
import app.dns.model.util.ProgressListener;
import app.dns.model.util.jchart.Charts;
import app.dns.model.util.DNSBenchmark;
import app.dns.model.entity.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MainController {
    private static Logger logger = LogManager.getLogger(MainController.class);
    private final Properties properties = new Properties();
    @FXML
    private ListView<String> dnsServersView;
    @FXML
    private ListView<String> domainsView;
    @FXML
    private MenuButton menuButtonDomains;
    @FXML
    private Button startBenchmarkButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Spinner<Integer> packetCountSelector;
    @FXML
    public void initialize() {
        ObservableList<String> dns = FXCollections.observableArrayList(loadDNSFromConfig());
        dnsServersView.setItems(dns);

        for (MenuItem item : loadDomainTypes()) {
            item.setOnAction(event -> onMenuItemSelected(item));
            menuButtonDomains.getItems().add(item);
        }

        startBenchmarkButton.setOnAction(event -> startBenchmark());

        packetCountSelector.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1, 1));
    }

    public MainController() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/config.properties");
        properties.load(inputStream);
    }


    public String[] loadDNSFromConfig() {
        String dnsResolvers = properties.getProperty("DNS.resolvers");
        String[] dnsArray = null;
        if (dnsResolvers != null) {
            dnsArray = Arrays.stream(dnsResolvers.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            logger.info("DNS loaded from config.");
            return dnsArray;
        } else {
            logger.error("Resolvers could not be found in config.");
        }
        return null;
    }

    public List<MenuItem> loadDomainTypes() {
        List<MenuItem> menuItems = new ArrayList<>();
        for (String string : new Type().getNames()) {
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
        String domains = null;
        switch (new Type().getNumberByName(domainType)) {
            case Type.EA_SERVERS:
                logger.info("Loading EA domain and sub-domains.");
                domains = properties.getProperty("EA.target_domains");
                break;
            case Type.SPOTIFY_SERVERS:
                logger.info("Loading Spotify domain and sub-domains.");
                domains = properties.getProperty("Spotify.target_domains");
                break;
            case Type.DISCORD_SERVERS:
                logger.info("Loading Discord domain and sub-domains.");
                domains = properties.getProperty("Discord.target_domains");
                break;
        }

        final String[] domainArray;
        if (domains != null) {
            domainArray = Arrays.stream(domains.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            logger.info("Domains loaded from config.");
            return domainArray;
        } else {
            logger.error("target_domains not found in config.");
            return null;
        }
    }

    public void startBenchmark() {
        int domainType = new Type().getNumberByName(menuButtonDomains.getText());
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
            protected List<DNSResult> call() throws Exception {
                DNSBenchmark dnsBenchmark = new DNSBenchmark(new ProgressListener() {
                    @Override
                    public void updateTaskProgress(double progress) {
                        updateProgress(progress, 1.0);
                        logger.info("progress: {}", progress * 100);
                    }
                });
                return dnsBenchmark.execute(domainType, packetCountSelector.getValue());
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
