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
	private int maxIndex;
	private int minIndex;
	
	
	public TestFrameAnalyzer() {
		super();		
		codes = new ArrayList<Integer>();
		count = new ArrayList<Integer>();
		respTimes = new ArrayList<Long>();
		zeroRespIndexes = new ArrayList<Integer>();
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
			}
			
			long respTime = tfg.testFrames.get(tfs.get(i)).getResponseTime();
			if(respTime == 0 && tfg.testFrames.get(tfs.get(i)).getResponseCode() > 0)
				zeroRespIndexes.add(tfs.get(i));
			
			respTimes.add(respTime);
		}
		
		int Selection = 0;
		while(Selection == 0) {
			System.out.println("\nSelect:\n1) Calculate Statistics\n2) Print failed test ("+failedIndexes.size()+")\n3) Print min and max response time test\n4) Exit from analyzer");
			Selection = scan.nextInt();
			if((Selection != 1)&&(Selection != 2)&&(Selection != 3)&&(Selection != 4)) {Selection = 0;}
			
			//stampa 0 resp time
			/*else if(Selection == 4) {
				if(zeroRespIndexes.size() != 0) {
					for(int i = 0; i<zeroRespIndexes.size(); i++) {
						System.out.println("\n----------------------------------------\nurl: "+tfg.testFrames.get(zeroRespIndexes.get(i)).getUrl());
						if(tfg.testFrames.get(zeroRespIndexes.get(i)).getSelPayload() != "null")
							System.out.println("payload: "+ tfg.testFrames.get(zeroRespIndexes.get(i)).getSelPayload());
						System.out.print("Expected Responses: ");
						tfg.testFrames.get(zeroRespIndexes.get(i)).printExpectedResponses();
						System.out.println("\nReturned Response: "+ tfg.testFrames.get(zeroRespIndexes.get(i)).getResponseCode());
						System.out.println("Response Time: "+ tfg.testFrames.get(zeroRespIndexes.get(i)).getResponseTime()+" ms");
					}
				} else {
					System.out.println("No 0 response time test found.. ");
				}
				Selection = 0;
				
			}*/ else if(Selection == 3) {
				//stampa min max
				if(maxIndex != -1 && minIndex != -1) {
					//max
					System.out.println("\n------------------MAX-------------------\nurl: "+tfg.testFrames.get(maxIndex).getUrl());
					if(tfg.testFrames.get(maxIndex).getSelPayload() != "null")
						System.out.println("payload: "+ tfg.testFrames.get(maxIndex).getSelPayload());
					System.out.print("Expected Responses: ");
					tfg.testFrames.get(maxIndex).printExpectedResponses();
					System.out.println("\nReturned Response: "+ tfg.testFrames.get(maxIndex).getResponseCode());
					System.out.println("Response Time: "+ tfg.testFrames.get(maxIndex).getResponseTime()+" ms");
				
					System.out.println("\n------------------MIN-------------------\nurl: "+tfg.testFrames.get(minIndex).getUrl());
					if(tfg.testFrames.get(minIndex).getSelPayload() != "null")
						System.out.println("payload: "+ tfg.testFrames.get(minIndex).getSelPayload());
					System.out.print("Expected Responses: ");
					tfg.testFrames.get(minIndex).printExpectedResponses();
					System.out.println("\nReturned Response: "+ tfg.testFrames.get(minIndex).getResponseCode());
					System.out.println("Response Time: "+ tfg.testFrames.get(minIndex).getResponseTime()+" ms");
				}else {
					System.out.println("Nothing found.. (try after calculating statistics)");
				}
				Selection = 0;
				
			}else if (Selection == 2) {
				for(int i = 0; i<failedIndexes.size(); i++) {
					System.out.println("\n----------------------------------------\nurl: "+tfg.testFrames.get(failedIndexes.get(i)).getUrl());
					if(tfg.testFrames.get(failedIndexes.get(i)).getSelPayload() != "null")
						System.out.println("payload: "+ tfg.testFrames.get(failedIndexes.get(i)).getSelPayload());
					System.out.print("Expected Responses: ");
					tfg.testFrames.get(failedIndexes.get(i)).printExpectedResponses();
					System.out.println("\nReturned Response: "+ tfg.testFrames.get(failedIndexes.get(i)).getResponseCode());
					System.out.println("Response Time: "+ tfg.testFrames.get(failedIndexes.get(i)).getResponseTime()+" ms");
				}
				Selection = 0;
				
			}else if (Selection == 1) {
				
				//statistics
				printStatistics(tfg, tfs, failedIndexes);
				
				Selection = 0;
			}
			
			if(Selection == 4) {
				
				break;
			}
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
