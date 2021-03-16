package prioritizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import analyzer.TestFrameAnalyzer;
import parser.TestFrameGenerator;

public class TestFramePrioritizer {
	
	private HashMap<String, Float> methodWeights;
	private HashMap<String, Float> severityMethodModificators;
	
	private ArrayList<Float> fieldWeights;
	private ArrayList<Float> severityFieldModificators;


	
	
	public TestFramePrioritizer() {
		super();
		methodWeights = new  HashMap<String, Float>();
		severityMethodModificators = new  HashMap<String, Float>();
		
		fieldWeights = new  ArrayList<Float>();
		severityFieldModificators = new  ArrayList<Float>();
		
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			fieldWeights.add((float)0);
		}
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			severityFieldModificators.add((float)0);
		}
		
	}
	
	
	
	public void calculateWeights(TestFrameAnalyzer tfa) {
		
		for(Map.Entry<String, ArrayList<Integer>>  method : tfa.methodCount.entrySet()) {
			float weight = 0;
			float modificator = 0;
			
			if(method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX) != 0)
				modificator = (float)method.getValue().get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX)/method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX);
			
			severityMethodModificators.put(method.getKey(),modificator);
			
			if(method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX) != 0)
				weight = (float)method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX)/method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX)*modificator;
			
			methodWeights.put(method.getKey(),weight);
		}
		
		
		
		for(int i = 0;i< TestFrameAnalyzer.FIELDS_NUM; i++) {
			float modificator = 0;
			
			if(tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX) != 0) {
				modificator = (float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX)/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX);
				severityFieldModificators.set(i,modificator);
			}
			
			if(tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX) != 0) {
				fieldWeights.set(i,(float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX)/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX)*modificator);
			}
		}
		
		System.out.println();
		
		for(Map.Entry<String, ArrayList<Integer>>  method : tfa.methodCount.entrySet()) {
			System.out.println("[INFO]	"+method.getKey() +" (total,failure): ("+ method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX)+","+method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX)+")");
			System.out.println("[INFO]	Severity modificator: " + severityMethodModificators.get(method.getKey())+"\n");
		}

		for(int i = 0;i< TestFrameAnalyzer.FIELDS_NUM; i++) {
			System.out.println("[INFO]	"+TestFrameAnalyzer.names.get(i) + " (total,failure): ("+tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX)+","+tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX)+")");
			System.out.println("[INFO]	Severity modificator: " + severityFieldModificators.get(i)+"\n");
		}
		
		
		
	}
	
	public void getWeightsFromFile(TestFrameGenerator tfg, String fileToRead) {
		
		try {
			File myObj = new File(fileToRead);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if(!data.contains("**")) {
					//NON COMPLETA
				}	
			}
			myReader.close();
		} catch (Exception e) {
			System.out.println("[ERROR] Json retrieval: File read failed.");
		}
	}
	
	
	
	public void prioritizeTest(TestFrameGenerator tfg) {
		
	}
	
	
	
	
	public void printWeights() {
		System.out.println("Weights: \n");
		for(Map.Entry<String, Float>  method : methodWeights.entrySet()) {
			System.out.println("	"+ method.getKey() +": "+ method.getValue());			
		}
		
		for(int i = 0;i< TestFrameAnalyzer.FIELDS_NUM; i++) {
			System.out.println("	"+TestFrameAnalyzer.names.get(i) +" : "+ fieldWeights.get(i));
		}
	}
}
