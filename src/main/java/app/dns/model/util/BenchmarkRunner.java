package app.dns.model.util;

import app.dns.model.entity.DNSResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BenchmarkRunner {
    private static Logger logger = LogManager.getLogger(BenchmarkRunner.class);
    private final static ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    private final ProgressListener listener;
    private static int overallProgress = 0;
    private int localProgress = 0;
    private int totalLatency = 0;
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
                addTotalLatency(benchmarkThread.getLatency());
            }
        }

        dnsResult.setFirstDnsServer(firstDns);
        dnsResult.setSecondDnsServer(secondDns);
        dnsResult.setSuccessPercentage((double) getCountPingSuccess() / domainArray.length * 100);
        dnsResult.setAverageLatency(getCountPingSuccess() == 0 ? 0.0 : (double) getTotalLatency() / getCountPingSuccess());
        dnsResult.setDnsSuccessPercentage((double) getCountDNSSuccess() / domainArray.length * 100);

        logger.info("DNS resolver servers: {} & {}, success percentage: {}%, avg latency: {} ms, dns lookup success percentage: {}%",
                dnsResult.getFirstDnsServer(),
                dnsResult.getSecondDnsServer(),
                String.format("%.2f", dnsResult.getSuccessPercentage()),
                String.format("%.2f", dnsResult.getAverageLatency()),
                String.format("%.2f", dnsResult.getDnsSuccessPercentage()));
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

    public synchronized void addTotalLatency(int value) {
        totalLatency += value;
    }

    public synchronized int getTotalLatency() {
        return totalLatency;
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
