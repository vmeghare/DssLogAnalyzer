package org.capgemini.am.LogAnalyzer;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

import org.jfree.chart.ChartPanel;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class AnalyseData extends ApplicationFrame{

        final static Logger ADLog = Logger.getLogger(AnalyseData.class);
        
	Map<String, Map<String, Long[]>> thirdPartyResponse = new LinkedHashMap<String, Map<String, Long[]>>();
	Map<String, Long> thirdPartyAverageResponse = new LinkedHashMap<String, Long>();
	ChartPanel panels[];
	
	private int currentChartNo = 0;
	private JButton previousButton = new JButton("<");
	private JButton nextButton = new JButton(">");
	private JPanel currentChartPanel = new JPanel();
	
	public AnalyseData() {
		super("EIF");
	}
	
	public int analyseData(String fileName){
		String sCurrentLine;
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			File error = new File("Error.log");
			File Data = new File("Data.csv");
			File finalReport = new File("FinalReport.csv");
		
			error.delete();
			FileWriter ErrorLog = new FileWriter(error);
			
			Data.delete();
			FileWriter DataLog = new FileWriter(Data);
			
			finalReport.delete();
			FileWriter FinalLog = new FileWriter(finalReport);

			
		
			while ((sCurrentLine = br.readLine()) != null) {				
				
				if(sCurrentLine.contains("Requesting")) {
					
					sCurrentLine = sCurrentLine.replaceFirst(",", ":");
					
					
					Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.ENGLISH).parse(sCurrentLine.substring(0,sCurrentLine.indexOf(",")));
					String thirdParty = sCurrentLine.substring(sCurrentLine.indexOf("INFO")+5,sCurrentLine.indexOf("-",sCurrentLine.indexOf("INFO")+5)-1);
					String CorrelationID = sCurrentLine.substring(sCurrentLine.indexOf("CorrelationID:")+14);
					if(thirdPartyResponse.containsKey(thirdParty)) {
						Map<String, Long[]> correlationIDs = thirdPartyResponse.get(thirdParty);
						
						if(correlationIDs.containsKey(CorrelationID) == false){
							Long[] times = new Long[2];
							times[0] = date.getTime();
							correlationIDs.put(CorrelationID, times);
						}else {
							ErrorLog.append("Duplicate Request : thirdParty - "+thirdParty+" / CorrelationID - "+CorrelationID+"\n");					
						}
						
					}else {
						Map<String, Long[]> correlationIDs = new LinkedHashMap<String, Long[]>();
						Long[] times = new Long[2];
						times[0] = date.getTime();
						correlationIDs.put(CorrelationID, times);
						thirdPartyResponse.put(thirdParty, correlationIDs);
						thirdPartyAverageResponse.put(thirdParty, 0L);
					}
				}
				else if(sCurrentLine.contains("Response received")) {
					
					sCurrentLine = sCurrentLine.replaceFirst(",", ":");
					Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.ENGLISH).parse(sCurrentLine.substring(0,sCurrentLine.indexOf(",")));
					String thirdParty = sCurrentLine.substring(sCurrentLine.indexOf("INFO")+5,sCurrentLine.indexOf("-",sCurrentLine.indexOf("INFO")+5)-1);
					String CorrelationID = sCurrentLine.substring(sCurrentLine.indexOf("CorrelationID:")+14);
					if(thirdPartyResponse.containsKey(thirdParty)){
						Map<String, Long[]> correlationIDs = thirdPartyResponse.get(thirdParty);
						if(correlationIDs.containsKey(CorrelationID)) {
							Long[] times = correlationIDs.get(CorrelationID);							
							Long requestedTime = times[0];
							Long responseTime = date.getTime();
							Long timeTaken = responseTime - requestedTime;
							times[1] = timeTaken;
						}else {
							ErrorLog.append("Unable to Calculate Time Taken : thirdParty - "+thirdParty+" / CorrelationID - "+CorrelationID+"\n");						
						}
					}else{
							ErrorLog.append("Unable to Calculate Time Taken : thirdParty - "+thirdParty+" / CorrelationID - "+CorrelationID+"\n");
					}
				}
			}
			
			br.close();
			//FinalLog.append("FINAL REPORT : \n ");
						
			Set<Entry<String, Map<String, Long[]>>> thirdParties = thirdPartyResponse.entrySet();
			Iterator<Entry<String, Map<String, Long[]>>> iterator = thirdParties.iterator();
			panels = new ChartPanel[thirdParties.size()]; 
			DataLog.append("RequestType,Time of Request,Correlation ID,Time taken to Serve(ms)\n");
			FinalLog.append("Third Party,Successful Requests,Total response time(ms),Average Response Time(ms),Max Response Time(ms),Min Response Time(ms),Total Failed Requests\n");
			int Count = 0;
			while(iterator.hasNext()){				
				int NoOfSuccessCalls = 0;
				int NoOfFailedCalls = 0;
				long MaxTime = 0;
				long MinTime = Long.MAX_VALUE;
				
				Entry<String, Map<String, Long[]>> thirdParty = iterator.next();
				//DataLog.append("--------------"+thirdParty.getKey()+"-------------- \n");
				//FinalLog.append("--------------"+thirdParty.getKey()+"-------------- \n");
				
				Map<String, Long[]> corrlationIDS = thirdParty.getValue();
				
				Set<Entry<String, Long[]>> corrlationIDs = corrlationIDS.entrySet();
				Iterator<Entry<String, Long[]>> iteratorCorrelation = corrlationIDs.iterator();
				
				while(iteratorCorrelation.hasNext()){				
					Entry<String, Long[]> corrlationID = iteratorCorrelation.next();
					if(corrlationID.getValue()[1] != null){
						NoOfSuccessCalls++;
						Long TotalTime = thirdPartyAverageResponse.get(thirdParty.getKey());
						TotalTime = TotalTime + corrlationID.getValue()[1];
						thirdPartyAverageResponse.put(thirdParty.getKey(), TotalTime);
						SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS aa", Locale.ENGLISH);
						String requestedDate = date.format(new Date(corrlationID.getValue()[0]));
						DataLog.append(thirdParty.getKey()+","+requestedDate+","+corrlationID.getKey()+","+ corrlationID.getValue()[1]+"\n");
						if(corrlationID.getValue()[1] > MaxTime){
							MaxTime = corrlationID.getValue()[1];
						} 
						if(corrlationID.getValue()[1] < MinTime){
							MinTime = corrlationID.getValue()[1];
						}
					}else {
						NoOfFailedCalls++;
						ErrorLog.append("No response/Error response : thirdParty - "+thirdParty.getKey()+" / CorrelationID - "+corrlationID.getKey()+"\n");
					}					
				}
				
				
				
				FinalLog.append(thirdParty.getKey()+","+NoOfSuccessCalls+","+thirdPartyAverageResponse.get(thirdParty.getKey())+",");
				//FinalLog.append("Total Time Taken to Serve Successful Requests(ms) : "+thirdPartyAverageResponse.get(thirdParty.getKey())+"\n");
				if(NoOfSuccessCalls != 0){
					panels[Count] = Charts.createChart(thirdParty.getKey(), corrlationIDS);
					FinalLog.append(""+thirdPartyAverageResponse.get(thirdParty.getKey())/NoOfSuccessCalls+","+MaxTime+","+MinTime+",");
					//FinalLog.append("Average time Taken(ms) : "+(thirdPartyAverageResponse.get(thirdParty.getKey())/NoOfSuccessCalls)+"\n");
					//FinalLog.append("Max(ms) : "+MaxTimeRequestedAt+" : "+MaxCorelationID+" : "+MaxTime+"\n");
				}else{
					FinalLog.append("0,0,0,");
				}
				FinalLog.append(NoOfFailedCalls+"\n");
				Count++;
			}
			ErrorLog.flush();
			ErrorLog.close();
			DataLog.flush();
			DataLog.close();
			FinalLog.flush();
			FinalLog.close();		
			thirdPartyAverageResponse = null;
			thirdPartyResponse = null;
                        
                        ADLog.info("INFO test logs");
			return 0;
		} catch (Exception e) {	
			e.printStackTrace();
                        
                        ADLog.info("INFO logs");
			return 1;
		}
	}
	public static void main(String args[]){		
		if(args.length == 0){
			System.out.println("No file name mentioned for processing . Using default : dss.log");
			args = new String[]{"dss.log"};
		}
		AnalyseData objAnalyseData = new AnalyseData();
		int result = objAnalyseData.analyseData(args[0]);
		if(result == 0){
			System.out.println("Log file processed successfully.");
			System.out.println("Report files created.");
			objAnalyseData.addPanels();
		}else {
			System.out.println("Error while processing logs.");
		}
                
                ADLog.info("INFO logs");
				
	}
	public void addPanels(){
		currentChartNo = 0;
		currentChartPanel = panels[currentChartNo];
		add(currentChartPanel , BorderLayout.CENTER);
		add(previousButton, BorderLayout.WEST);
		add(nextButton, BorderLayout.EAST);
		previousButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        if (currentChartNo > 0) {
		            getContentPane().remove(currentChartPanel);
		            do{
		            --currentChartNo;
		            }while(panels[currentChartNo] == null);
		            currentChartPanel = panels[currentChartNo];
		            currentChartPanel.setVisible(true);
		            
		            getContentPane().add(currentChartPanel, BorderLayout.CENTER);
		            pack();
		            repaint(100L);
		        }
		    }
		});
		nextButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        if (currentChartNo < panels.length - 1) {
		            getContentPane().remove(currentChartPanel);
		            do{
			            ++currentChartNo;
			        }while(panels[currentChartNo] == null);
		            currentChartPanel = panels[currentChartNo];
		            currentChartPanel.setVisible(true);
		            getContentPane().add(currentChartPanel, BorderLayout.CENTER);
		            pack();
		            repaint(100L);
		        }
		    }
		});
		//setContentPane(currentChartPanel);
		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		ADLog.info("INFO logs");
	}
}
