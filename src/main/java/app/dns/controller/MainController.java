package app.dns.controller;

import app.dns.Charts;
import app.dns.DNSBenchmark;
import app.dns.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MainController {
    private static Logger logger = LogManager.getLogger(MainController.class);
    private final static Properties properties = new Properties();
    private static int selectedDomainType = 0;
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
    public void initialize() {
        ObservableList<String> dns = FXCollections.observableArrayList(loadDNSFromConfig());
        dnsServersView.setItems(dns);

        for (MenuItem item : loadDomainTypes()) {
            item.setOnAction(event -> onMenuItemSelected(item));
//            item.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    onMenuItemSelected(item);
//                }
//            });
            menuButtonDomains.getItems().add(item);
        }

        startBenchmarkButton.setOnAction(event -> startBenchmark(selectedDomainType));
    }
    public MainController() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/util/config.properties");
        properties.load(fileInputStream);
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
        domainsView.setItems(filteredDomains);
    }

    public String[] filterDomainsByType(String domainType) {
        String domains = null;
        switch (new Type().getNumberByName(domainType)) {
            case Type.EA_SERVERS:
                logger.info("Loading EA domain and sub-domains.");
                domains = properties.getProperty("EA.target_domains");
                selectedDomainType = Type.EA_SERVERS;
                break;
            case Type.MICROSOFT_SERVERS:
                logger.info("loading..");
                break;
            case Type.ROCKSTAR_SERVERS:
                logger.info("loading..");
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
    
    public void startBenchmark(int i) {
        logger.info("Starting...........");
        DNSBenchmark dnsBenchmark = new DNSBenchmark();
        SwingUtilities.invokeLater(() -> Charts.getInstance().generateDNSPerformanceChart(dnsBenchmark.execute(i,1)));
    }
}
