package app.dns.model.util;

import app.dns.model.entity.DNSResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {
    private static Logger logger = LogManager.getLogger(BenchmarkRunner.class);
    private final static ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    private final ProgressListener listener;
    private static int overallProgress = 0;
    private int localProgress = 0;
    private int totalLatency = 0;
    private int countDNSSuccess = 0;
    private int countPingSuccess = 0;
    private DNSResult dnsResult = new DNSResult();

    public BenchmarkRunner(ProgressListener listener) {
        this.listener = listener;
    }

    public void executeDNSTest(String dns, int dnsSize, String[] domainArray, int packetCount, String operatingSystem) {

        List<BenchmarkThread> threads = new ArrayList<>();
        for (String targetDomain : domainArray) {
            BenchmarkThread benchmarkThread = new BenchmarkThread(targetDomain, dns, packetCount, operatingSystem);
            threads.add(benchmarkThread);
            threadPool.execute(benchmarkThread);
        }

        for (BenchmarkThread benchmarkThread : threads) {

            while (true) {
                if (benchmarkThread.isDone())
                    break;
            }

            incrementLocalProgress();
            incrementOverallProgress();

            listener.updateTaskProgress((double) getOverallProgress() / (dnsSize * domainArray.length));
            if (benchmarkThread.isDnsSuccessful()) {
                incrementCountDNSSuccess();
            }
            if (benchmarkThread.isOverallSuccessful()) {
                incrementCountPingSuccess();
                addTotalLatency(benchmarkThread.getLatency());
            }
        }

        dnsResult.setDnsServer(dns);
        dnsResult.setSuccessPercentage((double) getCountPingSuccess() / domainArray.length * 100);
        dnsResult.setAverageLatency(getCountPingSuccess() == 0 ? 0.0 : (double) getTotalLatency() / getCountPingSuccess());
        dnsResult.setDnsSuccessPercentage((double) getCountDNSSuccess() / domainArray.length * 100);

        logger.info("DNSResovler Server: {}, success percentage: {}%, avg latency: {} ms, dns lookup success percentage: {}%",
                dnsResult.getDnsServer(),
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
