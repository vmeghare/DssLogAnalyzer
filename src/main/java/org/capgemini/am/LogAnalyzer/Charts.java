package org.capgemini.am.LogAnalyzer;

import java.awt.Dimension;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import org.apache.log4j.Logger;

public class Charts {

    final static Logger chartsLog = Logger.getLogger(Charts.class);
    ChartPanel chartPanel;

    public Charts(String s, String thirdParty, Map<String, Long[]> corrlationIDS) {
        //super(s);
        XYDataset xydataset = createDataset(thirdParty, corrlationIDS);
        JFreeChart jfreechart = createChart(xydataset);
        ChartPanel chartpanel = new ChartPanel(jfreechart);
        chartpanel.setPreferredSize(new Dimension(800, 600));
        chartpanel.setMouseZoomable(true);
        chartPanel = chartpanel;
        chartPanel.setVisible(true);
        //setContentPane(chartpanel);
        chartsLog.info("INFO logs");
    }

    private static XYDataset createDataset(String thirdParty, Map<String, Long[]> corrlationIDS) {
        TimeSeries timeseries = null;
        //Day day = new Day();
        //Set<Entry<String, Map<String, Long[]>>> thirdParties = data.thirdPartyResponse.entrySet();
        //Iterator<Entry<String, Map<String, Long[]>>> iterator = thirdParties.iterator();
        //while(iterator.hasNext()){				
        //Entry<String, Map<String, Long[]>> thirdParty = iterator.next();
        //Map<String, Long[]> corrlationIDS = thirdParty.getValue();

        Set<Entry<String, Long[]>> corrlationIDs = corrlationIDS.entrySet();
        Iterator<Entry<String, Long[]>> iteratorCorrelation = corrlationIDs.iterator();
        timeseries = new TimeSeries(thirdParty);
        while (iteratorCorrelation.hasNext()) {
            Entry<String, Long[]> corrlationID = iteratorCorrelation.next();
            timeseries.addOrUpdate(new FixedMillisecond(new Date(corrlationID.getValue()[0])), corrlationID.getValue()[1]);
        }
        //break;
        //}
        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(timeseries);

        chartsLog.info("INFO logs");
        return timeseriescollection;
    }

    private static JFreeChart createChart(XYDataset xydataset) {
        String s = "TimeSeries";
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(s, "Time", "ResponseTime(ms)", xydataset, true, true, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setInsets(new RectangleInsets(0.0D, 0.0D, 0.0D, 20D));
        xyplot.getDomainAxis().setLowerMargin(0.0D);

        chartsLog.info("INFO logs");
        return jfreechart;
    }

    public static ChartPanel createChart(String thirdParty, Map<String, Long[]> corrlationIDS) {
        Charts timeseriesdemo4 = new Charts(thirdParty, thirdParty, corrlationIDS);
        //timeseriesdemo4.pack();
        //RefineryUtilities.centerFrameOnScreen(timeseriesdemo4);

        chartsLog.info("INFO logs");
        return timeseriesdemo4.chartPanel;
    }
}