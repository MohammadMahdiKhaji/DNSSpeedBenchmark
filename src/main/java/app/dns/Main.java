package app.dns;

import app.dns.model.entity.Type;
import app.dns.model.util.DNSBenchmark;
import app.dns.model.util.jchart.Charts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;

public class Main {
    protected static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        logger.info("Starting...........");
        DNSBenchmark dnsBenchmark = new DNSBenchmark();
        SwingUtilities
                .invokeLater(() ->
                        Charts.getInstance()
                                .generateDNSPerformanceChart(
                                        dnsBenchmark.execute(Type.EA_SERVERS, 2)));
    }
}
