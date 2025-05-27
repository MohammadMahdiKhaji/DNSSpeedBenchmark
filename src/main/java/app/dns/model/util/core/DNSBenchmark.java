package app.dns.model.util.core;

import app.dns.model.entity.DNSResult;
import app.dns.model.entity.Type;
import app.dns.model.util.BenchmarkRunner;
import app.dns.model.util.ProgressListener;
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
            if (flushCache()) {
                return testDNSPerformance(dnsArray, domainArray, packetCount, OS);
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
                return true;
            } else if (OS.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{
                        "/bin/sh", "-c", "sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder"
                });
                return true;
            }  else if (OS.contains("nix") || OS.contains("nux") || OS.contains("linux")) {
                Runtime.getRuntime().exec(new String[]{
                        "/bin/sh", "-c", "sudo systemd-resolve --flush-caches || sudo service nscd restart || sudo service dnsmasq restart || sudo systemctl restart NetworkManager"
                });
                return true;
            }
            logger.info("System resolver cache flushed.");
        } catch (IOException e) {
            logger.error("Flushing failed, message : {}", e.getMessage());
        }
        return false;
    }

    private List<DNSResult> testDNSPerformance(String[] dnsArray, String[] domainArray, int packetCount, String operatingSystem) {
        List<DNSResult> dnsResults = new ArrayList<>();
        List<BenchmarkRunner> runners = new ArrayList<>();

        for (int i=0; i<dnsArray.length; i++) {
            logger.info("Starting benchmark for DNS resolver: {}", dnsArray[i]);
            BenchmarkRunner runner = new BenchmarkRunner(progressListener);
            runners.add(runner);
            runner.executeDNSTest(dnsArray[i], dnsArray.length, domainArray, packetCount, operatingSystem);
        }

        for (BenchmarkRunner runner : runners) {
            while(true) {
                if (runner.getDnsResult() != null) {
                    dnsResults.add(runner.getDnsResult());
                    break;
                }
            }
        }
        return dnsResults;
    }
}