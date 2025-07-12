package app.dns.model.core;

import app.dns.model.entity.DNSResult;
import app.dns.model.BenchmarkRunner;
import app.dns.model.util.ProgressListener;
import app.dns.model.util.properties.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class DNSBenchmark {
    private static Logger logger = LogManager.getLogger(DNSBenchmark.class);
    private String OS;
    private ProgressListener progressListener;

    public DNSBenchmark(ProgressListener progressListener) {
        this.progressListener = progressListener;
        OS = System.getProperty("os.name").toLowerCase();
    }

    public List<DNSResult> execute(String[] dnsResolversAddresses, String[] domains) {
        return testDNSPerformance(dnsResolversAddresses, domains, OS);
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

    private List<DNSResult> testDNSPerformance(String[] dnsAddressesArray, String[] domainArray, String operatingSystem) {
        List<DNSResult> dnsResults = new ArrayList<>();
        List<BenchmarkRunner> runners = new ArrayList<>();

        flushCache();

        for (int i=0; i<domainArray.length; i++) {
            try {
                if (!InetAddress.getByName(domainArray[i]).isReachable(Configs.getReachabilityTimeout())) {
                    domainArray = remove(domainArray, i);
                }
            } catch (IOException e) {
                logger.error("Pinging failed, message : {}", e.getMessage());
            }
        }

        for (int i=0; i<dnsAddressesArray.length; i++) {
            for (int j = i + 1; j < dnsAddressesArray.length; j++) {
                logger.info("Starting benchmark for DNS resolvers: {} & {}", dnsAddressesArray[i], dnsAddressesArray[j]);
                BenchmarkRunner runner = new BenchmarkRunner(
                        dnsAddressesArray[i], dnsAddressesArray[j],
                        dnsAddressesArray.length, domainArray,
                        operatingSystem, progressListener);
                runners.add(runner);
                runner.run();
            }
        }

        for (BenchmarkRunner runner : runners) {
            dnsResults.add(runner.getResults());
        }

        return dnsResults;
    }

    public static String[] remove(String[] arr, int in) {

        if (arr == null || in < 0 || in >= arr.length) {
            return arr;
        }

        String[] arr2 = new String[arr.length - 1];
        for (int i = 0, k = 0; i < arr.length; i++) {
            if (i == in)
                continue;

            arr2[k++] = arr[i];
        }

        return arr2;
    }
}