package analyzer;

import java.util.ArrayList;
import java.util.Scanner;

import parser.TestFrameGenerator;

public class TestFrameAnalyzer {
	private Scanner scan;
	private ArrayList<Integer> codes;
	private ArrayList<Integer> count;
	private ArrayList<Long> respTimes;
	private ArrayList<Integer> zeroRespIndexes;
	private ArrayList<Integer> successIndexes;
	private int maxIndex;
	private int minIndex;
	
	
	public TestFrameAnalyzer() {
		super();		
		codes = new ArrayList<Integer>();
		count = new ArrayList<Integer>();
		respTimes = new ArrayList<Long>();
		zeroRespIndexes = new ArrayList<Integer>();
		successIndexes = new ArrayList<Integer>();
		minIndex = -1;
		maxIndex = -1;
	}
	
	public void analyzeTest(TestFrameGenerator tfg, ArrayList<Integer> tfs, ArrayList<Integer> failedIndexes) {
		scan = new Scanner(System.in);
		
		//SCANNING FAILURES
		for(int i = 0; i < tfs.size(); i++) {
			
			if(failedIndexes.contains(tfs.get(i))) {
				//E' fallimento
				int code = tfg.testFrames.get(tfs.get(i)).getResponseCode();
				
				if(codes.contains(code)) {
					int index = codes.indexOf(code);
					count.set(index,count.get(index)+1);
				}else {
					codes.add(code);
					count.add(1);
				}
			}else {
				successIndexes.add(tfs.get(i));
			}
			
			long respTime = tfg.testFrames.get(tfs.get(i)).getResponseTime();
			if(respTime == 0 && tfg.testFrames.get(tfs.get(i)).getResponseCode() > 0)
				zeroRespIndexes.add(tfs.get(i));
			
			respTimes.add(respTime);
		}
		
		int Selection = 0;
		while(Selection == 0) {
			System.out.println("\nSelect:\n1) Calculate Statistics\n"
					+ "2) Print failed test ("+failedIndexes.size()+")\n"
					+ "3) Print success test ("+successIndexes.size()+")\n"
					+ "4) Print max/min response time test\n"
					+ "5) Exit from analyzer");
			Selection = scan.nextInt();
			if(Selection < 1 || Selection > 5) {Selection = 0;}
			
			else if (Selection == 1) {
				//statistics
				printStatistics(tfg, tfs, failedIndexes);
				Selection = 0;
				
			}else if (Selection == 2) {
				printTest(tfg, failedIndexes);
				Selection = 0;
				
			}else if (Selection == 3) {
				printTest(tfg, successIndexes);
				Selection = 0;
				
			}else if(Selection == 4) {
				//stampa min max
				if(maxIndex != -1 && minIndex != -1) {
					ArrayList<Integer> indexes = new ArrayList<Integer>();
					indexes.add(maxIndex);
					indexes.add(minIndex);
					
					printTest(tfg, indexes);
				}else {
					System.out.println("Nothing found.. (try after calculating statistics)");
				}
				Selection = 0;
				
			}
			
			if(Selection == 5) {
				break;
			}
		}
	}
	
	private void printTest(TestFrameGenerator tfg, ArrayList<Integer> indexes) {
		for(int i = 0; i<indexes.size(); i++) {
			System.out.println("\n----------------------------------------\nurl: "+tfg.testFrames.get(indexes.get(i)).getUrl());
			System.out.println("Method: "+tfg.testFrames.get(indexes.get(i)).getReqType());
			if(tfg.testFrames.get(indexes.get(i)).getSelPayload() != "null")
				System.out.println("payload: "+ tfg.testFrames.get(indexes.get(i)).getSelPayload());
			System.out.print("Expected Responses: ");
			tfg.testFrames.get(indexes.get(i)).printExpectedResponses();
			System.out.println("\nReturned Response: "+ tfg.testFrames.get(indexes.get(i)).getResponseCode());
			System.out.println("Response Time: "+ tfg.testFrames.get(indexes.get(i)).getResponseTime()+" ms");
		}
	}
	
	
	private void printStatistics(TestFrameGenerator tfg, ArrayList<Integer> tfs, ArrayList<Integer> failedIndexes) {
		System.out.println("\n# Test: " + tfs.size());
		System.out.println("# Failures: " + failedIndexes.size());
				
		//CODICI E COUNT
		System.out.println("\n----- Code ----- # -----");
		boolean triggerOss = false;
		for(int i = 0; i < codes.size(); i++) {
			if(codes.get(i) < 100) {
				if(codes.get(i) == -1 || codes.get(i) == -2) {
					System.out.print("-      " + codes.get(i)+ "*");
					triggerOss = true;
				} else {
					System.out.print("-       " + codes.get(i)+ " ");
				}
			}else {
				System.out.print("-      " + codes.get(i));
			}
			
			if(count.get(i)<100) {
				System.out.print("       "+ count.get(i)+ "\n");
			}else {
				System.out.print("      "+ count.get(i)+ "\n");
			}
		}
		
		if(triggerOss)
			System.out.println("\n*code -1 = SocketTimeoutException \n*code -2 = HTTP method not yet implemented");
		
		//TEMPI DI RISPOSTA
		long avg = 0;
		long max = 0;
		long min = respTimes.get(0);
		
		for(int i = 0; i < respTimes.size(); i++) {
			avg +=  respTimes.get(i);
			if(respTimes.get(i) > max) {
				max = respTimes.get(i);
				maxIndex = tfs.get(i);
			}
			
			if(respTimes.get(i) < min && tfg.testFrames.get(tfs.get(i)).getResponseCode() > 0) {
				min = respTimes.get(i);
				minIndex = tfs.get(i);
			}
		}
		avg = avg/respTimes.size();
		
		System.out.println("\n----- Response times (success and failures)");
		System.out.println("- max        "+ max+ " ms");
		System.out.println("- min        "+ min+ " ms");
		System.out.println("- avg        "+ avg+ " ms");
		//System.out.println("\n # test with 0 reponse time: "+ zeroRespIndexes.size());
	}
	
	

}
