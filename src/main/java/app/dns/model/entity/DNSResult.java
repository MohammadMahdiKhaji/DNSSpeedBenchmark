package app.dns.model.entity;

public class DNSResult {
    private String firstDnsServer;
    private String secondDnsServer;
    private double successPercentage;
    private double latencyScore;
    private double dnsSuccessPercentage;

    public DNSResult() {
    }

    public DNSResult(String firstDnsServer, String secondDnsServer, double successPercentage, double latencyScore, double dnsSuccessPercentage) {
        this.firstDnsServer = firstDnsServer;
        this.secondDnsServer = secondDnsServer;
        this.successPercentage = successPercentage;
        this.latencyScore = latencyScore;
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

    public double getLatencyScore() {
        return latencyScore;
    }

    public DNSResult setLatencyScore(double latencyScore) {
        this.latencyScore = latencyScore;
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
                ", latencyScore=" + latencyScore +
                ", dnsSuccessPercentage=" + dnsSuccessPercentage +
                '}';
    }
}
