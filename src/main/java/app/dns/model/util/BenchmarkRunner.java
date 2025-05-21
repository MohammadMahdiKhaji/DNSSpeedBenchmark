package app.dns.model.util;

import app.dns.model.entity.DNSResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkRunner {
    private static Logger logger = LogManager.getLogger(BenchmarkRunner.class);
    private final static ThreadPool threadPool = new ThreadPool(5);
    private final ProgressListener listener;
    private static AtomicInteger countProgress = new AtomicInteger(0);
    private AtomicInteger localProgress = new AtomicInteger(0);
    private AtomicInteger latency = new AtomicInteger(0);
    private AtomicInteger countSuccess = new AtomicInteger(0);
    private AtomicInteger countLatency = new AtomicInteger(0);

    public BenchmarkRunner(ProgressListener listener) {
        this.listener = listener;
    }

    public DNSResult executeDNSTest(String dns, int dnsSize, String[] domainArray, int packetCount, String operatingSystem) {

//        for (String targetDomain : domainArray) {
//            BenchmarkThread benchmarkThread = new BenchmarkThread(targetDomain, dns, packetCount, operatingSystem);
//                    CompletableFuture.runAsync(benchmarkThread, threadPool).thenRun(() -> {
//                localProgress.incrementAndGet();
//                countProgress.incrementAndGet();
//                listener.updateTaskProgress((double) countProgress.get() / (dnsSize * domainArray.length));
//                if (benchmarkThread.isDnsSuccessful()) {
//                    countSuccess.incrementAndGet();
//                }
//                if (benchmarkThread.isOverallSuccessful()) {
//                    countLatency.incrementAndGet();
//                    latency.addAndGet(benchmarkThread.getLatency());
//                }
//            });
//        }
//        while (true) {
//            if (localProgress.get() == domainArray.length)
//                break;
//        }

        List<BenchmarkThread> threads = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String targetDomain : domainArray) {
            BenchmarkThread benchmarkThread = new BenchmarkThread(targetDomain, dns, packetCount, operatingSystem);
            threads.add(benchmarkThread);

            CompletableFuture<Void> future = CompletableFuture.runAsync(benchmarkThread, threadPool);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (BenchmarkThread benchmarkThread : threads) {
            localProgress.incrementAndGet();
            countProgress.incrementAndGet();
            listener.updateTaskProgress((double) countProgress.get() / (dnsSize * domainArray.length));
            if (benchmarkThread.isDnsSuccessful()) {
                countSuccess.incrementAndGet();
            }
            if (benchmarkThread.isOverallSuccessful()) {
                countLatency.incrementAndGet();
                latency.addAndGet(benchmarkThread.getLatency());
            }
        }

        DNSResult dnsResult = new DNSResult(
                dns,
                (double) countLatency.get() / domainArray.length * 100,
                countLatency.get() == 0 ? 0.0 : (double) latency.get() / countLatency.get(),
                (double) countSuccess.get() / domainArray.length * 100);

        logger.info("DNS Server: {}, success percentage: {}%, avg latency: {} ms, dns lookup success percentage: {}%",
                dnsResult.getDnsServer(),
                String.format("%.2f", dnsResult.getSuccessPercentage()),
                String.format("%.2f", dnsResult.getAverageLatency()),
                String.format("%.2f", dnsResult.getDnsSuccessPercentage()));

        return dnsResult;
    }
}
