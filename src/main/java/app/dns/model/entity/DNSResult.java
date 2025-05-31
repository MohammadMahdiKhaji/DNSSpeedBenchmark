package app.dns.model.entity;

public class DNSResult {
    private String firstDnsServer;
    private String secondDnsServer;
    private double successPercentage;
    private double averageLatency;
    private double dnsSuccessPercentage;

    public DNSResult() {
    }

    public DNSResult(String firstDnsServer, String secondDnsServer, double successPercentage, double averageLatency, double dnsSuccessPercentage) {
        this.firstDnsServer = firstDnsServer;
        this.secondDnsServer = secondDnsServer;
        this.successPercentage = successPercentage;
        this.averageLatency = averageLatency;
        this.dnsSuccessPercentage = dnsSuccessPercentage;
    }

    public String getFirstDnsServer() {
        return firstDnsServer;
    }

    public DNSResult setFirstDnsServer(String firstDnsServer) {
        this.firstDnsServer = firstDnsServer;
        return this;
    }

    public String getSecondDnsServer() {
        return secondDnsServer;
    }

    public DNSResult setSecondDnsServer(String secondDnsServer) {
        this.secondDnsServer = secondDnsServer;
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
                "firstDnsServer='" + firstDnsServer + '\'' +
                ", secondDnsServer='" + secondDnsServer + '\'' +
                ", successPercentage=" + successPercentage +
                ", averageLatency=" + averageLatency +
                ", dnsSuccessPercentage=" + dnsSuccessPercentage +
                '}';
    }
}
