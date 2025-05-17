package app.dns.model.util;

import app.dns.model.entity.DNSResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DNSBenchmark {
    private static Logger logger = LogManager.getLogger(DNSBenchmark.class);
    private final static Properties properties = new Properties();
    private final ProgressListener listener;
    private static String OS = null;

    public DNSBenchmark() {
        this.listener = null;

        OS = System.getProperty("os.name").toLowerCase();

        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            logger.error("Configuration file not found", e);
            throw new RuntimeException("Configuration file missing", e);
        } catch (IOException e) {
            logger.error("Error reading configuration file", e);
            throw new RuntimeException("Could not read configuration file", e);
        }
    }

    public DNSBenchmark(ProgressListener listener) {
        this.listener = listener;

        OS = System.getProperty("os.name").toLowerCase();

        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            logger.error("Configuration file not found", e);
            throw new RuntimeException("Configuration file missing", e);
        } catch (IOException e) {
            logger.error("Error reading configuration file", e);
            throw new RuntimeException("Could not read configuration file", e);
        }
    }

    public List<DNSResult> execute(int serverType, int packetCount) {
        String dnsResolvers = properties.getProperty("DNS.resolvers");
        String[] dnsArray = null;
        String domains = null;
        if (dnsResolvers != null) {
            dnsArray = Arrays.stream(dnsResolvers.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            logger.info("DNS loaded from config.");
        } else {
            logger.error("Resolvers could not be found in config.");
        }

        switch (serverType) {
            case app.dns.model.entity.Type.EA_SERVERS:
                logger.info("Loading EA domain and sub-domains.");
                domains = properties.getProperty("EA.target_domains");
                break;
            case app.dns.model.entity.Type.MICROSOFT_SERVERS:
                logger.info("loading..");
                break;
            case app.dns.model.entity.Type.ROCKSTAR_SERVERS:
                logger.info("loading..");
                break;
        }

        final String[] domainArray;
        if (domains != null) {
            domainArray = Arrays.stream(domains.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            if (flushCache()) {
                return testDNSPerformance(dnsArray, domainArray, packetCount);
            }
        } else {
            logger.error("target_domains not found in config.");
        }
        return null;
    }

    private boolean flushCache() {
        try {
            if (OS.contains("win")) {
                Runtime.getRuntime().exec("ipconfig /flushdns");
                logger.info("DNS cache flushed.");
                return true;
            } else if (OS.contains("mac")) {
                Runtime.getRuntime().exec("sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder");
                logger.info("DNS cache flushed on macOS.");
                return true;
            }
        } catch (IOException e) {
            logger.error("Flushing failed, message : {}", e.getMessage());
        }
        return false;
    }

    private String[] pingCMD(String ip, int packetCount) {
        if (OS.contains("win")) {
            return new String[]{"ping", "-n", String.valueOf(packetCount), ip};
        } else if (OS.contains("mac")) {
            return new String[]{"ping", "-c", String.valueOf(packetCount), ip};
        }
        logger.error("Creating ping command failed.");
        return null;
    }

    int countProgress = 0;

    private List<DNSResult> testDNSPerformance(String[] dnsArray, String[] domainArray, int packetCount) {

        List<DNSResult> dnsResults = new ArrayList<>();

        for (String dns : dnsArray) {
            logger.info("Starting benchmark for DNS resolver: {}", dns);
            int latency = 0;
            int countSuccess = 0;
            int countLatency = 0;

            List<BenchmarkThread> benchmarkThreads = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            for (String targetDomain : domainArray) {
                BenchmarkThread benchmarkThread = new BenchmarkThread(targetDomain, dns, packetCount, OS);
                Thread thread = new Thread(benchmarkThread);
                threads.add(thread);
                benchmarkThreads.add(benchmarkThread);
                thread.start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                    countProgress++;
                    listener.updateProgress((double) countProgress / (dnsArray.length * domainArray.length));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (BenchmarkThread benchmarkThread : benchmarkThreads) {
                if (benchmarkThread.isDnsSuccessful()) {
                    countSuccess++;
                }
                if (benchmarkThread.isOverallSuccessful()) {
                    countLatency++;
                    latency += benchmarkThread.getLatency();
                }
            }

            DNSResult dnsResult = new DNSResult(
                    dns,
                    (double) countLatency / domainArray.length * 100,
                    countLatency == 0 ? 0.0 : (double) latency / countLatency,
                    (double) countSuccess / domainArray.length * 100);
            dnsResults.add(dnsResult);

            logger.info("DNS Server: {}, success percentage: {}%, avg latency: {} ms, dns lookup success percentage: {}%",
                    dnsResult.getDnsServer(),
                    String.format("%.2f", dnsResult.getSuccessPercentage()),
                    String.format("%.2f", dnsResult.getAverageLatency()),
                    String.format("%.2f", dnsResult.getDnsSuccessPercentage()));
        }
        return dnsResults;
    }
}