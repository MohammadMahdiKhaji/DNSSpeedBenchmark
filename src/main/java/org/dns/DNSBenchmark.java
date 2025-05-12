package org.dns;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DNSBenchmark {
    private static Logger logger = LogManager.getLogger(DNSBenchmark.class);
    private static String OS = null;
    private final static Properties properties = new Properties();

    public DNSBenchmark() throws IOException {
        OS = System.getProperty("os.name").toLowerCase();

        FileInputStream fileInputStream = new FileInputStream("src/main/resources/util/config.properties");
        properties.load(fileInputStream);
    }

    public List<DNSResult> execute(int serverType) {
        String dnsResolvers = properties.getProperty("DNS.resolvers");
        String[] dnsArray = null;
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
                String domains = properties.getProperty("EA.target_domains");
                final String[] domainArray;
                if (domains != null) {
                    domainArray = Arrays.stream(domains.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);
                    if (flushCache()) {
                        return testDNSPerformance(dnsArray, domainArray, 2);
                    }
                } else {
                    logger.error("target_domains not found in config.");
                }
                break;
            case Type.MICROSOFT_SERVERS:
                logger.info("loading..");
                break;
            case Type.ROCKSTAR_SERVERS:
                logger.info("loading..");
                break;
        }
        return null;
    }

    private boolean flushCache() {
        try {
            if (OS.contains("win")) {
                Runtime.getRuntime().exec("ipconfig /flushdns");
                logger.info("DNS cache flushed.");
            }
            return true;
        } catch (IOException e) {
            logger.error("Flushing failed, message : {}", e.getMessage());
            return false;   //bad practice
        }
    }

    private String[] pingCMD(String ip, int packetCount) {
        if (OS.contains("win")) {
            return new String[]{"ping", "-n", String.valueOf(packetCount), ip};
        }
        logger.error("Creating ping command failed.");
        return null;
    }

    private  List<DNSResult> testDNSPerformance(String[] dnsArray, String[] domainArray, int packetCount) {
        List<DNSResult> dnsResults = new ArrayList<>();
        for (String dns : dnsArray) {
            logger.info("Starting benchmark for DNS resolver: {}", dns);

            AtomicInteger latency = new AtomicInteger(0);
            AtomicInteger countSuccess = new AtomicInteger(0);
            AtomicInteger countLatency = new AtomicInteger(0);

            ExecutorService subExecutor = Executors.newFixedThreadPool(domainArray.length);
            List<Callable<Void>> subtasks = new ArrayList<>();

            try {
                SimpleResolver resolver = new SimpleResolver(dns);
                for (String targetDomain : domainArray) {
                    subtasks.add(() -> {
                        Lookup lookup = new Lookup(targetDomain, org.xbill.DNS.Type.A);
                        lookup.setDefaultResolver(resolver);
                        lookup.setResolver(resolver);
                        lookup.run();
                        if (lookup.getResult() == Lookup.SUCCESSFUL) {
                            try {
                                ProcessBuilder processBuilder = new ProcessBuilder(pingCMD(lookup.getAnswers()[0].rdataToString(), packetCount));
                                Process process = processBuilder.start();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (OS.contains("win") && line.contains("Average =")) {
                                        int avgIndex = line.indexOf("Average =") + "Average =".length();
                                        String avg = line.substring(avgIndex).replaceAll("[^\\d]", "");
                                        latency.addAndGet(Integer.parseInt(avg));
                                        countLatency.incrementAndGet();
                                    }
                                }
                                process.waitFor();
                            } catch (Exception e) {
                                logger.error("Ping failed: " + e.getMessage());
                            } finally {
                                countSuccess.incrementAndGet();
                            }
                        }
                        return null;
                    });
                }
                subExecutor.invokeAll(subtasks);
                subExecutor.shutdown();

                DNSResult dnsResult = new DNSResult(
                        dns,
                        (double) countSuccess.get() / domainArray.length * 100,
                        countLatency.get() == 0 ? 0.0 : (double) latency.get() / countLatency.get());
                dnsResults.add(dnsResult);

                logger.info("DNS Server: {}, success percentage: {}%, avg latency: {} ms",
                        dnsResult.getDnsServer(),
                        String.format("%.2f", dnsResult.getSuccessPercentage()),
                        String.format("%.2f", dnsResult.getAverageLatency()));
            } catch (UnknownHostException e) {
                logger.error("DNS failed: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.error("Task failed: " + e.getMessage());
            }
        }
        return dnsResults;
    }
}