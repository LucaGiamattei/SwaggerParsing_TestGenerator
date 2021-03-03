package analyzer;

import java.util.ArrayList;

import parser.TestFrameGenerator;

public class TestFrameAnalyzer {
	
	public TestFrameAnalyzer() {
		super();		
	}
	
	public void countResponseCodes(TestFrameGenerator tfg, ArrayList<Integer> failedIndexes) {
		
		System.out.println("\n# Failures: " + failedIndexes.size());
		
		ArrayList<Integer> codes = new ArrayList<Integer>();
		ArrayList<Integer> count = new ArrayList<Integer>();
		ArrayList<Long> respTimes = new ArrayList<Long>();
		
		for(int i = 0; i < failedIndexes.size(); i++) {
			int code = tfg.testFrames.get(failedIndexes.get(i)).getResponseCode();
			long respTime = tfg.testFrames.get(failedIndexes.get(i)).getResponseTime();
			respTimes.add(respTime);
			
			if(codes.contains(code)) {
				int index = codes.indexOf(code);
				count.set(index,count.get(index)+1);
			}else {
				codes.add(code);
				count.add(1);
			}
		}
		System.out.println("\n----- Code ----- # -----");
		
		for(int i = 0; i < codes.size(); i++) {
			if(codes.get(i) < 100) {
				System.out.print("-       " + codes.get(i)+ " ");
			}else {
				System.out.print("-      " + codes.get(i));
			}
			
			if(count.get(i)<100) {
				System.out.print("       "+ count.get(i)+ "\n");
			}else {
				System.out.print("      "+ count.get(i)+ "\n");
			}
		}
		
		long avg = 0;
		long max = 0;
		long min = respTimes.get(0);
		
		for(int i = 0; i < respTimes.size(); i++) {
			avg +=  respTimes.get(i);
			if(respTimes.get(i) > max)
				max = respTimes.get(i);
			if(respTimes.get(i) < min && tfg.testFrames.get(failedIndexes.get(i)).getResponseCode() != 0)
				min = respTimes.get(i);
		}
		avg = avg/respTimes.size();
		
		System.out.println("\n----- Response times");
		System.out.println("- max        "+ max);
		System.out.println("- min        "+ min);
		System.out.println("- avg        "+ avg);
		
	}
	

}
