package app.dns.model.util.core;

import app.dns.model.entity.DNSResult;
import app.dns.model.util.BenchmarkRunner;
import app.dns.model.util.ProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DNSBenchmark {
    private static Logger logger = LogManager.getLogger(DNSBenchmark.class);
    private final static Properties properties = new Properties();
    private String OS;
    private ProgressListener progressListener;

    public DNSBenchmark(ProgressListener progressListener) {
        this.progressListener = progressListener;

        OS = System.getProperty("os.name").toLowerCase();

        try (InputStream inputStream = getClass().getResourceAsStream("/config.properties")) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            logger.error("Configuration file not found", e);
            throw new RuntimeException("Configuration file missing", e);
        } catch (IOException e) {
            logger.error("Error reading configuration file", e);
            throw new RuntimeException("Could not read configuration file", e);
        }
    }

    public List<DNSResult> execute(String[] dnsResolversAddresses, String[] domains, int packetCount) {
        return testDNSPerformance(dnsResolversAddresses, domains, packetCount, OS);
    }

    private boolean flushCache() {
        try {
            if (OS.contains("win")) {
                Runtime.getRuntime().exec("ipconfig /flushdns");
                logger.info("System resolver cache flushed.");
                return true;
            } else if (OS.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{
                        "/bin/sh", "-c", "sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder"
                });
                logger.info("System resolver cache flushed.");
                return true;
            }  else if (OS.contains("nix") || OS.contains("nux") || OS.contains("linux")) {
                Runtime.getRuntime().exec(new String[]{
                        "/bin/sh", "-c", "sudo systemd-resolve --flush-caches || sudo service nscd restart || sudo service dnsmasq restart || sudo systemctl restart NetworkManager"
                });
                logger.info("System resolver cache flushed.");
                return true;
            }
        } catch (IOException e) {
            logger.error("Flushing failed, message : {}", e.getMessage());
        }
        return false;
    }

    private List<DNSResult> testDNSPerformance(String[] dnsAddressesArray, String[] domainArray, int packetCount, String operatingSystem) {
        List<DNSResult> dnsResults = new ArrayList<>();
        List<BenchmarkRunner> runners = new ArrayList<>();

        flushCache();

        for (int i=0; i<dnsAddressesArray.length; i++) {
            for (int j = i + 1; j < dnsAddressesArray.length; j++) {
                logger.info("Starting benchmark for DNS resolvers: {} & {}", dnsAddressesArray[i], dnsAddressesArray[j]);
                BenchmarkRunner runner = new BenchmarkRunner(
                        dnsAddressesArray[i], dnsAddressesArray[j],
                        dnsAddressesArray.length, domainArray,
                        packetCount, operatingSystem,
                        progressListener);
                runners.add(runner);
                runner.run();
            }
        }

        for (BenchmarkRunner runner : runners) {
            dnsResults.add(runner.getResults());
        }

        return dnsResults;
    }
}