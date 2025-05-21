package app.dns.model.util.jchart;

import app.dns.model.entity.DNSResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Charts {
    private static final Charts charts = new Charts();

    private Charts() {
    }

    public static Charts getInstance() {
        return charts;
    }

    public void generateDNSPerformanceChart(List<DNSResult> resultsUnsorted) {
        resultChart(sortedResults(resultsUnsorted));
    }

    private List<DNSResult> sortedResults(List<DNSResult> resultsUnsorted) {
        resultsUnsorted.sort(Comparator
                .comparingDouble(DNSResult::getSuccessPercentage).reversed()
                .thenComparingDouble(DNSResult::getAverageLatency));
        return resultsUnsorted;
    }

    private void resultChart(List<DNSResult> resultsSorted) {

        double adjustment=0.1;
        for (int i = 0; i < resultsSorted.size() - 1; i++) {
            if (isOverlapping(resultsSorted.get(i), resultsSorted.get(i + 1))) {
//                adjustment+=0.1;
                resultsSorted.get(i+1).setSuccessPercentage(resultsSorted.get(i+1).getSuccessPercentage() - adjustment);
            }
        }

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
                false,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        XYShapeRenderer renderer = new XYShapeRenderer() {
            @Override
            public Paint getItemPaint(int row, int col) {
                String currentDNS = resultsSorted.get(col).getDnsServer();

                if (currentDNS.equals(resultsSorted.get(0).getDnsServer()) || currentDNS.equals(resultsSorted.get(1).getDnsServer())) {
                    return Color.GREEN;
                } else if (currentDNS.equals(resultsSorted.get(2).getDnsServer()) || currentDNS.equals(resultsSorted.get(3).getDnsServer())) {
                    return Color.YELLOW;
                } else {
                    return Color.RED;
                }
            }
        };

        plot.setRenderer(renderer);
        ChartPanel chartPanel = new ChartPanel(chart);

        JFrame frame = new JFrame("DNS Benchmark Visualization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(chartPanel);
        frame.setSize(800, 800);
        frame.setVisible(true);
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof XYItemEntity itemEntity) {
                    int itemIndex = itemEntity.getItem();
                    DNSResult result = resultsSorted.get(itemIndex);
                    JOptionPane.showMessageDialog(null,
                            String.format("DNS Server: %s\nSuccess: %.2f%%\nLatency: %.2f ms\nLookup Success: %.2f%%",
                                    result.getDnsServer(),
                                    result.getSuccessPercentage(),
                                    result.getAverageLatency(),
                                    result.getDnsSuccessPercentage()),
                            "DNS Node Details",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
            }
        });
    }

    public boolean isOverlapping(DNSResult firstDot, DNSResult secondDot) {
        if (firstDot.getAverageLatency() == secondDot.getAverageLatency() &&
                firstDot.getSuccessPercentage() == secondDot.getSuccessPercentage()) {
            return true;
        }
        return false;
    }
}
