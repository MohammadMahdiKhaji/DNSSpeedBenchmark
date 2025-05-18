package app.dns.model;

import app.dns.model.entity.Type;
import app.dns.model.util.DNSBenchmark;
import app.dns.model.util.ProgressListener;
import app.dns.model.util.jchart.Charts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.security.Security;

public class Main {
    protected static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        logger.info("Starting...........");
        Security.setProperty("networkaddress.cache.ttl", "0");
        Security.setProperty("networkaddress.cache.negative.ttl", "0");
        DNSBenchmark dnsBenchmark = new DNSBenchmark(new ProgressListener() {
            @Override
            public void updateProgress(double progress) {
                logger.info("progress: {}",progress*100);
            }
        });
        //SwingUtilities uses javax.swing (JWT threads) not javafx this for development purposes
        SwingUtilities
                .invokeLater(() ->
                        Charts.getInstance()
                                .generateDNSPerformanceChart(
                                        dnsBenchmark.execute(Type.EA_SERVERS, 2)));
    }
}
