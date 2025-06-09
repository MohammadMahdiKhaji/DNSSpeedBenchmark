package app.dns.model.util;

import app.dns.model.entity.DNSResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BenchmarkRunner {
    private static Logger logger = LogManager.getLogger(BenchmarkRunner.class);
    private final static ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    private final ProgressListener listener;
    private List<Integer> latencies = new ArrayList<>();
    private static int overallProgress = 0;
    private int localProgress = 0;
    private int countDNSSuccess = 0;
    private int countPingSuccess = 0;
    private String firstDns;
    private String secondDns;
    private int dnsSize;
    private String[] domainArray;
    private int packetCount;
    private String operatingSystem;
    private DNSResult dnsResult = new DNSResult();

    public BenchmarkRunner(String firstDns, String secondDns,
                           int dnsSize, String[] domainArray,
                           int packetCount, String operatingSystem,
                           ProgressListener listener) {
        this.firstDns = firstDns;
        this.secondDns = secondDns;
        this.dnsSize = dnsSize;
        this.domainArray = domainArray;
        this.packetCount = packetCount;
        this.operatingSystem = operatingSystem;
        this.listener = listener;
    }

    public void run() {
        List<CompletableFuture> futures = new ArrayList<>();
        List<BenchmarkThread> threads = new ArrayList<>();

        for (String targetDomain : domainArray) {
            BenchmarkThread benchmarkThread = new BenchmarkThread(targetDomain, firstDns, secondDns, packetCount, operatingSystem);
            threads.add(benchmarkThread);
            threadPool.execute(benchmarkThread);
            futures.add(CompletableFuture.runAsync(benchmarkThread, threadPool));
        }

        futures.forEach(CompletableFuture::join);
        setResults(threads);
    }

    public void setResults(List<BenchmarkThread> threads) {

        for (BenchmarkThread benchmarkThread : threads) {

            incrementLocalProgress();
            incrementOverallProgress();

            listener.updateTaskProgress((double) getOverallProgress() / (((dnsSize * (dnsSize-1))/2) * domainArray.length));
            if (benchmarkThread.isDnsSuccessful()) {
                incrementCountDNSSuccess();
            }
            if (benchmarkThread.isOverallSuccessful()) {
                incrementCountPingSuccess();
                addLatency(benchmarkThread.getLatency());
            }
        }

        dnsResult.setFirstDnsServer(firstDns);
        dnsResult.setSecondDnsServer(secondDns);
        dnsResult.setSuccessPercentage((double) getCountPingSuccess() / domainArray.length * 100);
        dnsResult.setLatencyScore(getCountPingSuccess() == 0 ? 0.0 :
                        (calculateAverage() * 0.3) +
                        (calculatePercentileValue(50) * 0.4) +
                        (calculatePercentileValue(90) * 0.2) +
                        (calculatePercentileValue(99) * 0.1)
        );
        dnsResult.setDnsSuccessPercentage((double) getCountDNSSuccess() / domainArray.length * 100);

        logger.info("DNS resolver servers: {} & {}, success percentage: {}%, avg latency: {} ms, dns lookup success percentage: {}%",
                dnsResult.getFirstDnsServer(),
                dnsResult.getSecondDnsServer(),
                String.format("%.2f", dnsResult.getSuccessPercentage()),
                String.format("%.2f", dnsResult.getLatencyScore()),
                String.format("%.2f", dnsResult.getDnsSuccessPercentage()));
    }

    private double calculateAverage() {
        if (latencies.isEmpty()) return 0.0;
        return latencies.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private long calculatePercentileValue(int percentile) {
        if (latencies.isEmpty()) return 0;

        Collections.sort(latencies);

        if (percentile == 100) {
            return latencies.get(latencies.size() - 1);
        }

        int index = (int) Math.ceil(percentile / 100.0 * latencies.size()) - 1;
        index = Math.max(0, Math.min(index, latencies.size() - 1));
        return latencies.get(index);
    }

    public DNSResult getDnsResult() {
        return dnsResult;
    }

    public static synchronized void incrementOverallProgress() {
        overallProgress++;
    }

    public static synchronized int getOverallProgress() {
        return overallProgress;
    }

    public synchronized void incrementLocalProgress() {
        localProgress++;
    }

    public synchronized int getLocalProgress() {
        return localProgress;
    }

    public synchronized void addLatency(int latency) {
        latencies.add(latency);
    }

        public synchronized List<Integer> getLatencies() {
        return latencies;
    }

    public synchronized void incrementCountDNSSuccess() {
        countDNSSuccess++;
    }

    public synchronized int getCountDNSSuccess() {
        return countDNSSuccess;
    }

    public synchronized void incrementCountPingSuccess() {
        countPingSuccess++;
    }

    public synchronized int getCountPingSuccess() {
        return countPingSuccess;
    }
}
