package org.dns;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class Charts {
    private List<DNSResult> resultsSortedGlobal;
    private static final Charts charts = new Charts();
    private Charts() {}

    public static Charts getInstance() {
        return charts;
    }

    public void generateDNSPerformanceChart(List<DNSResult> resultsUnsorted) {
        resultChart(sortedResults(resultsUnsorted));
    }

    public void generateDNSPerformanceChart() {
        List<DNSResult> resultsUnsorted = new ArrayList<>();
        resultsUnsorted.add(new DNSResult("78.157.42.100", 100.00, 69.73));
        resultsUnsorted.add(new DNSResult("78.157.42.101", 100.00, 49.07));
        resultsUnsorted.add(new DNSResult("10.202.10.10", 100.00, 47.49));
        resultsUnsorted.add(new DNSResult("10.202.10.11", 100.00, 64.40));
        resultsUnsorted.add(new DNSResult("205.171.3.65", 100.00, 97.52));
        resultsUnsorted.add(new DNSResult("205.171.2.65", 100.00, 108.47));
        resultsUnsorted.add(new DNSResult("195.46.39.39", 100.00, 113.33));
        resultsUnsorted.add(new DNSResult("195.46.39.40", 100.00, 78.73));
        resultsUnsorted.add(new DNSResult("80.80.81.81", 96.91, 100.09));
        resultsUnsorted.add(new DNSResult("80.80.80.80", 90.72, 96.18));
        resultsUnsorted.add(new DNSResult("199.85.127.10", 100.00, 54.15));
        resultsUnsorted.add(new DNSResult("199.85.126.10", 100.00, 50.27));
        resultsUnsorted.add(new DNSResult("10.202.10.202", 97.94, 46.46));
        resultsUnsorted.add(new DNSResult("10.202.10.102", 52.58, 11.25));
        resultsUnsorted.add(new DNSResult("178.22.122.100", 100.00, 57.87));
        resultsUnsorted.add(new DNSResult("185.51.200.2", 100.00, 60.01));
        resultsUnsorted.add(new DNSResult("77.88.8.8", 100.00, 60.24));
        resultsUnsorted.add(new DNSResult("77.88.8.1", 100.00, 66.19));
        resultsUnsorted.add(new DNSResult("85.15.1.14", 100.00, 66.36));
        resultsUnsorted.add(new DNSResult("85.15.1.15", 100.00, 60.46));
        resultsUnsorted.add(new DNSResult("9.9.9.9", 100.00, 57.22));
        resultsUnsorted.add(new DNSResult("149.112.112.112", 100.00, 57.97));
        resultsUnsorted.add(new DNSResult("172.29.0.100", 98.97, 61.82));
        resultsUnsorted.add(new DNSResult("172.29.2.100", 54.64, 12.77));
        resultsUnsorted.add(new DNSResult("209.244.0.3", 100.00, 43.82));
        resultsUnsorted.add(new DNSResult("209.244.0.4", 100.00, 43.85));
        resultsUnsorted.add(new DNSResult("64.6.64.6", 100.00, 44.37));
        resultsUnsorted.add(new DNSResult("64.6.65.6", 100.00, 65.05));
        resultsUnsorted.add(new DNSResult("185.55.226.26", 100.00, 43.61));
        resultsUnsorted.add(new DNSResult("185.55.225.25", 98.97, 43.16));
        resultsUnsorted.add(new DNSResult("8.8.8.8", 100.00, 37.81));
        resultsUnsorted.add(new DNSResult("8.8.4.4", 100.00, 42.39));
        resultsUnsorted.add(new DNSResult("1.1.1.1", 100.00, 37.35));
        resultsUnsorted.add(new DNSResult("1.0.0.1", 100.00, 40.19));
        resultsUnsorted.add(new DNSResult("208.67.222.222", 100.00, 58.12));
        resultsUnsorted.add(new DNSResult("208.67.220.220", 100.00, 40.15));
        resultsUnsorted.add(new DNSResult("8.26.56.26", 100.00, 40.06));
        resultsUnsorted.add(new DNSResult("8.20.247.20", 100.00, 41.01));

        resultChart(sortedResults(resultsUnsorted));
    }
    private List<DNSResult> sortedResults(List<DNSResult> resultsUnsorted) {

        double temp = 0;
        for (DNSResult dnsResult : resultsUnsorted) {
            if (temp < dnsResult.getAverageLatency()) {
                temp=dnsResult.getAverageLatency();
            }
        }

        for (DNSResult dnsResult : resultsUnsorted) {
            dnsResult.setPoints(dnsResult.getSuccessPercentage()/100 - dnsResult.getAverageLatency()/temp);
        }
        resultsUnsorted.sort(Comparator.comparing(DNSResult::getPoints).reversed());

//        resultsUnsorted.sort(Comparator
//                .comparing((DNSResult d) -> d.getSuccessPercentage() == 100.0 ? 0 : 1)
//                .thenComparing(DNSResult::getAverageLatency));
//        for (DNSResult dnsResult : resultsUnsorted) {
//            System.out.println(dnsResult.toString());
//        }
        return resultsUnsorted;
    }
    private void resultChart(List<DNSResult> resultsSorted) {
        this.resultsSortedGlobal = resultsSorted;
        XYSeries series = new XYSeries("DNS Servers", false, true);
        for (int i = 0; i < resultsSorted.size(); i++) {
            series.add(resultsSorted.get(i).getSuccessPercentage(), resultsSorted.get(i).getAverageLatency());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "DNS Benchmark",
                "Access Percentage",
                "Latency (ms)",
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        XYShapeRenderer renderer = new XYShapeRenderer() {
            @Override
            public Paint getItemPaint(int row, int col) {
                double currentPoints = resultsSorted.get(col).getPoints();
                double top1 = resultsSorted.get(0).getPoints();
                double top2 = resultsSorted.get(1).getPoints();

                if (Double.compare(currentPoints, top1) == 0 || Double.compare(currentPoints, top2) == 0) {
                    return Color.GREEN;
                } else if (currentPoints < top2 && currentPoints != 0.0) {
                    return Color.YELLOW;
                } else {
                    return Color.RED;
                }
            }
        };

        plot.setRenderer(renderer);
        ChartPanel chartPanel = new ChartPanel(chart);

        JFrame frame = new JFrame("DNS Benchmark Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        chartPanel.addChartMouseListener(new org.jfree.chart.ChartMouseListener() {
            @Override
            public void chartMouseClicked(org.jfree.chart.ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof XYItemEntity itemEntity) {
                    int itemIndex = itemEntity.getItem();
                    DNSResult result = resultsSortedGlobal.get(itemIndex);
                    JOptionPane.showMessageDialog(null,
                            String.format("DNS Server: %s\nSuccess: %.2f%%\nLatency: %.2f ms\nScore: %.2f",
                                    result.getDnsServer(),
                                    result.getSuccessPercentage(),
                                    result.getAverageLatency(),
                                    result.getPoints()),
                            "DNS Node Details",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void chartMouseMoved(org.jfree.chart.ChartMouseEvent event) {
                // Optional: Hover effects
            }
        });
        frame.setVisible(true);
    }

}
