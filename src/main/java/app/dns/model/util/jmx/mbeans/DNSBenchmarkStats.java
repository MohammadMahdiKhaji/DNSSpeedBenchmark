package app.dns.model.util.jmx.mbeans;

import app.dns.model.util.properties.Configs;

public class DNSBenchmarkStats implements DNSBenchmarkStatsMXBean {
    private String version = "1.0.0";
    private int threadSize = 0;

    public DNSBenchmarkStats() {
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int getThreadSize() {
        return Configs.getThreadPoolSize();
    }
}
