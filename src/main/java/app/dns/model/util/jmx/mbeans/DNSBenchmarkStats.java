package app.dns.model.util.jmx.mbeans;

public class DNSBenchmarkStats implements DNSBenchmarkStatsMXBean {
    private String version = "1.0.0";

    public DNSBenchmarkStats() {
    }
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void sayHello() {
        System.out.println("hello");
    }
}
