package app.dns.model.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class BenchmarkThread implements Runnable {
    private static Logger logger = LogManager.getLogger(BenchmarkThread.class);
    private String targetDomain;
    private String dns;
    private int packetCount;
    private String OS;
    private int latency;
    private boolean overallSuccessful;
    private boolean dnsSuccessful;

    public BenchmarkThread(String targetDomain, String dns, int packetCount, String OS) {
        this.targetDomain = targetDomain;
        this.dns = dns;
        this.packetCount = packetCount;
        this.OS = OS;
        this.overallSuccessful = false;
        this.dnsSuccessful = false;
    }
    @Override
    public void run() {
        try {
            logger.info("Starting benchmark for domain: {}; using DNS: {}", targetDomain, dns);
            String targetIP  = lookup(targetDomain, dns);
            if (targetIP != null) {
                ProcessBuilder processBuilder = new ProcessBuilder(pingCMD(targetIP, packetCount, OS));
                Process process = processBuilder.start();
                Integer avg = getAvg(process.getInputStream());

                if (avg != null) {
                    latency = avg;
                    overallSuccessful = true;
                }
                process.destroy();
            }
        } catch (UnknownHostException e) {
            logger.error("DNS failed: " + e.getMessage());
        } catch (TextParseException e) {
            logger.error("Task failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ping failed: " + e.getMessage());
        }
    }

    public String lookup(String targetDomain, String dns) throws UnknownHostException, TextParseException {
        Resolver resolver = new SimpleResolver(dns);
        Lookup lookup = new Lookup(targetDomain, Type.A, DClass.IN);
        lookup.setDefaultCache(null, DClass.IN);
        lookup.setResolver(resolver);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            logger.info("Lookup result using DNS: {}, and Domain: {}: {}", dns, targetDomain, lookup.getAnswers()[0].rdataToString());
            dnsSuccessful = true;
            return lookup.getAnswers()[0].rdataToString();
        }
        return null;
    }

    public String[] pingCMD(String ip, int packetCount, String OS) {
        if (OS.contains("win")) {
            return new String[]{"ping", "-n", String.valueOf(packetCount), ip};
        } else if (OS.contains("mac")) {
            return new String[]{"ping", "-c", String.valueOf(packetCount), ip};
        }
        logger.error("Creating ping command failed.");
        return null;
    }

    public Integer getAvg(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (OS.contains("win") && line.contains("Average =")) {
                int avgIndex = line.indexOf("Average =") + "Average =".length();
                String avg = line.substring(avgIndex).replaceAll("[^\\d]", "");
                return Integer.parseInt(avg);
            } else if (OS.contains("mac") && line.contains("/avg/")) {
                int avgIndex = line.indexOf("min/avg/max/stddev = ") + "min/avg/max/stddev = ".length();
                String avg = line.substring(avgIndex).replaceAll("^\\d+\\.\\d+/|(\\d+\\.\\d+)/\\d+\\.\\d+/\\d+\\.\\d+ ms$", "$1");
                return  ((int) Math.round(Double.parseDouble(avg)));
            }
        }
        return null;
    }
    public boolean isOverallSuccessful() {
        return overallSuccessful;
    }

    public boolean isDnsSuccessful() {
        return dnsSuccessful;
    }

    public int getLatency() {
        return latency;
    }
}
