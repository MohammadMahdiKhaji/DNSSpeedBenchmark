package app.dns.model.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BenchmarkThread implements Runnable {
    private static Logger logger = LogManager.getLogger(BenchmarkThread.class);
    private final static int TIMEOUT_MILLISECONDS = 150;
    private String targetDomain;
    private String firstDns;
    private String secondDns;
    private int packetCount;
    private String OS;
    private int latency;
    private boolean overallSuccessful;
    private boolean dnsSuccessful;
    private volatile boolean done;
    private CountDownLatch doneSignal;

    public BenchmarkThread(String targetDomain, String firstDns, String secondDns,
                           int packetCount, String OS, CountDownLatch doneSignal) {
        this.targetDomain = targetDomain;
        this.firstDns = firstDns;
        this.secondDns = secondDns;
        this.packetCount = packetCount;
        this.OS = OS;
        this.doneSignal = doneSignal;
        this.overallSuccessful = false;
        this.dnsSuccessful = false;
        this.done = false;
    }
    @Override
    public void run() {
        try {
            String targetIP;
            logger.info("Starting benchmark for domain: {}; using the first DNS: {}", targetDomain, firstDns);
            targetIP = lookup(targetDomain, firstDns);
            if (benchmark(targetIP, firstDns)) {
                return;
            }

            logger.info("Starting benchmark for domain: {}; using the second DNS: {}", targetDomain, secondDns);
            targetIP = lookup(targetDomain, secondDns);
            if (benchmark(targetIP, secondDns)) {
                return;
            }

            logger.info("Benchmark for domain: {}; using DNS resolvers: {} & {} is over", targetDomain, firstDns, secondDns);
        } catch (UnknownHostException e) {
            logger.error("DNS failed: " + e.getMessage());
        } catch (TextParseException e) {
            logger.error("Task failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ping failed: " + e.getMessage());
        } finally {
//            finally does get called even if there are return statements in try or catch blocks.
            if (doneSignal != null) {
                doneSignal.countDown();
            }
        }
    }

    public boolean benchmark(String targetIP, String dns) throws IOException {
        Duration duration;
        Instant start;
        boolean results;

        if (targetIP != null) {
            start = Instant.now();
            results = processReachability(targetIP);
            duration = Duration.between(start, Instant.now());
            if (results) {
                latency = (int) duration.toMillis();
                overallSuccessful = true;
                done = true;
                logger.info("Benchmark for domain: {}; using DNS resolvers: {} & {} is over", targetDomain, firstDns, secondDns);
                return true;
            }
        }
        logger.info("Benchmark for domain: {}; using DNS resolver: {} has failed", targetDomain, dns);
        return false;
    }

    public String lookup(String targetDomain, String dns) throws UnknownHostException, TextParseException {
        Resolver resolver = new SimpleResolver(dns);
        Lookup lookup = new Lookup(targetDomain, Type.A, DClass.IN);
        lookup.setDefaultCache(null, DClass.IN);
        lookup.setResolver(resolver);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            logger.info("Lookup result using DNS resolver: {}, and Domain: {}: {}", dns, targetDomain, lookup.getAnswers()[0].rdataToString());
            dnsSuccessful = true;
            return lookup.getAnswers()[0].rdataToString();
        }
        return null;
    }

    public Integer processPing(String targetIP) throws IOException, InterruptedException {
        if (targetIP != null) {
            ProcessBuilder processBuilder = new ProcessBuilder(pingCMD(targetIP, packetCount, OS));
            Process process = processBuilder.start();
            process.waitFor(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            process.destroy();
            return getAvg(process.getInputStream());
        }
        return null;
    }

    public boolean processReachability(String targetIP) throws IOException {
        if (targetIP != null) {
            String[] targetIPInArray = targetIP.split("\\.");
            byte[] ipBytes = new byte[targetIPInArray.length];
            for (int i=0; i<targetIPInArray.length; i++) {
                //Byte.parseByte() throws an exception for values >127,
                //while (byte) Integer.parseInt() allows 0–255 and wraps it to a signed byte,
                //which is suitable for IPs.
                ipBytes[i] = (byte) Integer.parseInt(targetIPInArray[i]);
            }
            //InetAddress doesn't exactly run an OS-level ping command, check documentation
            return InetAddress.getByAddress(ipBytes).isReachable(TIMEOUT_MILLISECONDS);
        }
        return false;
    }

    public String[] pingCMD(String ip, int packetCount, String OS) {
        if (OS.contains("win")) {
            return new String[]{"ping", "-n", String.valueOf(packetCount), ip};
        } else if (OS.contains("mac") || OS.contains("nix") || OS.contains("nux")) {
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
            } else if ((OS.contains("mac") || OS.contains("nix") || OS.contains("nux")) && line.contains("min/avg/max")) {
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

    public boolean isDone() {
        return done;
    }
}
