package app.dns;

public class DNSResult {
    private String dnsServer;
    private double successPercentage;
    private double averageLatency;

    private transient double points = 0.0;

    public DNSResult() {
    }

    public DNSResult(String dnsServer, double successPercentage, double averageLatency) {
        this.dnsServer = dnsServer;
        this.successPercentage = successPercentage;
        this.averageLatency = averageLatency;
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

    public double getPoints() {
        return points;
    }

    public DNSResult setPoints(double points) {
        this.points = points;
        return this;
    }

    @Override
    public String toString() {
        return "DNSResult{" +
                "dnsServer='" + dnsServer + '\'' +
                ", successPercentage=" + successPercentage +
                ", averageLatency=" + averageLatency +
                ", points=" + points +
                '}';
    }
}
