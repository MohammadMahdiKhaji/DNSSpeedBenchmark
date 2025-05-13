package app.dns;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;

public class Main {
    protected static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        logger.info("Starting...........");
//        DNSBenchmark dnsBenchmark = new DNSBenchmark();
//        SwingUtilities.invokeLater(() -> Charts.getInstance().generateDNSPerformanceChart(dnsBenchmark.execute(Type.EA_SERVERS, 2)));
//        SwingUtilities.invokeLater(() -> Charts.getInstance().generateDNSPerformanceChart());
    }
}
