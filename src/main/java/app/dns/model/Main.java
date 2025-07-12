package app.dns.model;

import app.dns.model.entity.DNSResult;
import app.dns.model.entity.Type;
import app.dns.model.util.JSONReader;
import app.dns.model.core.DNSBenchmark;
import app.dns.model.util.ProgressListener;
import app.dns.model.util.jchart.Charts;
import app.dns.model.util.properties.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.security.Security;
import java.util.List;

public class Main {
    protected static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        logger.info("Starting...........");
        Security.setProperty("networkaddress.cache.ttl", "0");
        Security.setProperty("networkaddress.cache.negative.ttl", "0");
        try {
            Configs configs = new Configs();
            configs.loadValues();

            DNSBenchmark dnsBenchmark = new DNSBenchmark(new ProgressListener() {
                @Override
                public void updateTaskProgress(double progress) {
                    logger.info("progress: {}", progress * 100);
                }
            });
            //SwingUtilities uses javax.swing (JWT threads) not javafx this for development purposes
            List<DNSResult> dnsResults = dnsBenchmark.execute(
                    JSONReader.getAllDNSResolversName(),
                    JSONReader.getDomainsByDomainName(Type.getNameByNumber(Type.EA_DOMAINS)));
            SwingUtilities.invokeLater(() -> Charts.getInstance().generateDNSPerformanceChart(dnsResults));

        } catch (IOException e) {
            logger.error("Error reading configuration file", e);
            throw new RuntimeException("Could not read configuration file", e);
        }
    }
}
