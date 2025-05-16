package app.dns.model.entity;

public class DNSResult {
    private String dnsServer;
    private double successPercentage;
    private double averageLatency;
    private double dnsSuccessPercentage;

    public DNSResult() {
    }

    public DNSResult(String dnsServer, double successPercentage, double averageLatency, double dnsSuccessPercentage) {
        this.dnsServer = dnsServer;
        this.successPercentage = successPercentage;
        this.averageLatency = averageLatency;
        this.dnsSuccessPercentage = dnsSuccessPercentage;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public DNSResult setDnsServer(String dnsServer) {
        this.dnsServer = dnsServer;
        return this;
    }

    public double getSuccessPercentage() {
        return successPercentage;
    }

    public DNSResult setSuccessPercentage(double successPercentage) {
        this.successPercentage = successPercentage;
        return this;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public DNSResult setAverageLatency(double averageLatency) {
        this.averageLatency = averageLatency;
        return this;
    }

    public double getDnsSuccessPercentage() {
        return dnsSuccessPercentage;
    }

    public DNSResult setDnsSuccessPercentage(double dnsSuccessPercentage) {
        this.dnsSuccessPercentage = dnsSuccessPercentage;
        return this;
    }

    @Override
    public String toString() {
        return "DNSResult{" +
                "dnsServer='" + dnsServer + '\'' +
                ", successPercentage=" + successPercentage +
                ", averageLatency=" + averageLatency +
                ", dnsSuccessPercentage=" + dnsSuccessPercentage +
                '}';
    }
}
