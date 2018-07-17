package com.orange.util.others;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Properties;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Directories;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;

public class MemoryUsages {

    private volatile static MemoryUsages instance = null;
    private static Object mutex = new Object();
    
    public static MemoryUsages getInstance(double upperRange) {
    	MemoryUsages result = instance;
    	if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null){
					instance = result = new MemoryUsages();
					instance.init("Memory Usages", upperRange);
				}
			}
		}
		return result;
     }
    
    private ValueAxis axis;
    private double baseupperRange;
    private CustomJFrame frame;
    private TimeSeries series;
    private TimeSeries series1;
  
    protected  MemoryUsages() 
    {
       
    }
    
    private JFreeChart createChart(final XYDataset dataset,double upperRange) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart("Memory Usages","Memory Consume", "Available Memory",dataset, true, true, false );
        final XYPlot plot = result.getXYPlot();
       
        /*
       
        plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(false);
        }
        */
        axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(600000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(0.0, upperRange); 
        return result;
    }
    public CustomJFrame getFrame(){
    	return frame;
    }
    
  
 
    @SuppressWarnings("deprecation")
	protected  void init(final String title,double upperRange) 
    {  try{
    	baseupperRange = upperRange;
    	series 	= new TimeSeries("Used Memory", Second.class);
        series1 = new TimeSeries("Free Memory", Second.class);
      
            
	        if(null != series && null != series1)
	        {
	        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
	        dataset.addSeries(series1);
	      
		    final JFreeChart chart = createChart(dataset,baseupperRange);
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setMouseZoomable(true, false);
	        
	        final JPanel content = new JPanel(new BorderLayout());
	        content.add(chartPanel);
	        frame = new CustomJFrame(title,Icons.helpIcon);
	        frame.setOpacity(0.8f);
	        frame.add(content);
	    	
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    	frame.setBounds(screenSize.width-220,screenSize.height*60/100,200,150);
	    	
	    	Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
			if(properties.getProperty("MemoryPulse").equalsIgnoreCase("YES"))
			{
				frame.setVisible(true);
			}
			else{
				frame.setVisible(false);
			}
	    	
	    	
	    	frame.setAlwaysOnTop (true);
	    	screenSize = null;
	    	
	        }
        }catch(ArrayIndexOutOfBoundsException exp)
        {
         System.out.println("Exception reported .. new frame init");
         frame.dispose();
         instance = null;
         getInstance(upperRange);
        }
        
        
    }
    
    public void setMemVal(double UM,double FM){
    	try{
    	series.add(new Second(), UM);
    	series1.add(new Second(), FM);
    	if(UM >= baseupperRange){
    		int mb = 1024*1024;
			Runtime runtime = Runtime.getRuntime();
			double uppererRange = ((runtime.totalMemory() / mb));
			axis.setRange(0.0, uppererRange); 
    	}
    	
     	UM = 0;
    	FM = 0;
    	}catch(Exception e){}
    }
     
}
