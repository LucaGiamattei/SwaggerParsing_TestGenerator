package analyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import parser.TestFrameGenerator;

public class TestFrameAnalyzer {
	public static final int TOTAL_INDEX = 0;
	public static final int FAILURE_INDEX = 1;
	public static final int SEVERE_FAILURE_INDEX = 2;
	public static final int FIELDS_NUM = 16;
	
	public static final ArrayList<String> names = new ArrayList<String>(Arrays.asList("payload","date","dateTime","int32","int64","float","double","empty","range","symbol","greater","lower","bTrue","bFalse","sRange","lang"));
	public static final int PAYLOAD_IDX = 0;
	public static final int DATE_IDX= 1;
	public static final int DATETIME_IDX= 2;
	public static final int INT32_IDX= 3;
	public static final int INT64_IDX= 4;
	public static final int FLOAT_IDX= 5;
	public static final int DOUBLE_IDX= 6;
	public static final int EMPTY_IDX= 7;
	public static final int RANGE_IDX= 8;
	public static final int SYMBOL_IDX= 9;
	public static final int GREATER_IDX= 10;
	public static final int LOWER_IDX= 11;
	public static final int BTRUE_IDX= 12;
	public static final int BFALSE_IDX= 13;
	public static final int SRANGE_IDX= 14;
	public static final int LANG_IDX= 15;
	
	public HashMap<String, ArrayList<Integer>> methodCount; //metodo, <#totale,#fallimenti>
	public ArrayList<ArrayList<Integer>> hasFieldCount;
	

	
	public ArrayList<Integer> failedIndexes;
	public ArrayList<Integer> successIndexes;
	
	public ArrayList<Integer> codes;
	public ArrayList<Integer> codeCount;
	
	public ArrayList<Integer> severity; 
	public ArrayList<Integer> countSeverity; 
	
	public ArrayList<Long> respTimes;
	public long min;
	public long max;
	public long avg;
	public int maxIndex;
	public int minIndex;
	
	
	public TestFrameAnalyzer() {
		super();
		
		methodCount = new HashMap<String, ArrayList<Integer>>();
		
		hasFieldCount = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<FIELDS_NUM;i++) {
			hasFieldCount.add(new ArrayList<Integer>(Arrays.asList(0,0,0)));
		}
		
		codes = new ArrayList<Integer>();
		severity = new ArrayList<Integer>();
		countSeverity = new ArrayList<Integer>();
		codeCount = new ArrayList<Integer>();
		failedIndexes = new ArrayList<Integer>();
		successIndexes = new ArrayList<Integer>();
		respTimes = new ArrayList<Long>();
		minIndex = -1;
		maxIndex = -1;
		avg = 0;
		max = 0;
		min = 0;
		
	}
	
	public void analyzeTest(TestFrameGenerator tfg, ArrayList<Integer> tfs) {		
		avg = 0;
		max = 0;
		min = tfg.testFrames.get(tfs.get(0)).getResponseTime();
		
		//SCANNING
		//ciclo su tutti i testframe "scelti" nel dataset completo di tfg
		for(int i = 0; i < tfs.size(); i++) {
			
			String method = tfg.testFrames.get(tfs.get(i)).getReqType();
			int code = tfg.testFrames.get(tfs.get(i)).getResponseCode();
			
			ArrayList<Boolean> hasFieldB = new ArrayList<Boolean>();
			for(int x=0;x<FIELDS_NUM;x++) {
				hasFieldB.add(false);
			}
	    	//boolean hasDate= false,hasDateTime= false,hasInt32= false,hasInt64= false,hasFloat= false,hasDouble= false,hasEmpty= false, hasRange= false, hasSymbol= false, hasGreater= false, hasLower= false, hasBTrue= false, hasBFalse= false, hasSRange= false, hasLang = false;

			
			//METODI
			if(!methodCount.containsKey(method)) {
				ArrayList<Integer> counts = new ArrayList<Integer>();
				counts.add(1);	//TOTAL_INDEX 
				counts.add(0);	//FAILURE_INDEX
				counts.add(0);	//SEVERE_FAILURE_INDEX
				methodCount.put(method,counts);
			}else {
				ArrayList<Integer> counts = new ArrayList<Integer>(methodCount.get(method));
				counts.set(TOTAL_INDEX,counts.get(TOTAL_INDEX)+1);
				methodCount.put(method, counts);
			}
			
			
			if(tfg.testFrames.get(tfs.get(i)).getPayload() != null) {
				 hasFieldB.set(PAYLOAD_IDX,true);
			}
			//ciclo su ogni ic per sapere i fields presenti
			for(int j = 0; j<tfg.testFrames.get(tfs.get(i)).ic.size(); j++) {
				String typeTemp = tfg.testFrames.get(tfs.get(i)).ic.get(j).type;
	    		  
				if(typeTemp.equals("date")) hasFieldB.set(DATE_IDX,true);
				else if(typeTemp.equals("date-time")) hasFieldB.set(DATETIME_IDX,true);
				else if(typeTemp.equals("int32")) hasFieldB.set(INT32_IDX,true);
				else if(typeTemp.equals("int64")) hasFieldB.set(INT64_IDX,true);
				else if(typeTemp.equals("float")) hasFieldB.set(FLOAT_IDX,true);
				else if(typeTemp.equals("double")) hasFieldB.set(DOUBLE_IDX,true);
				else if(typeTemp.equals("empty")) hasFieldB.set(EMPTY_IDX,true);
				else if(typeTemp.equals("range")) hasFieldB.set(RANGE_IDX,true);
				else if(typeTemp.equals("symbol")) hasFieldB.set(SYMBOL_IDX,true);
				else if(typeTemp.equals("greater")) hasFieldB.set(GREATER_IDX,true);
				else if(typeTemp.equals("lower")) hasFieldB.set(LOWER_IDX,true);
				else if(typeTemp.equals("b_true")) hasFieldB.set(BTRUE_IDX,true);
				else if(typeTemp.equals("b_false")) hasFieldB.set(BFALSE_IDX,true);
				else if(typeTemp.equals("s_range")) hasFieldB.set(SRANGE_IDX,true);
				else if(typeTemp.equals("lang")) hasFieldB.set(LANG_IDX,true);
			}
			
			
			
			//CASO FALLIMENTO
			if(tfg.testFrames.get(tfs.get(i)).getState().equals("failed")) {
				
				int severityT = tfg.testFrames.get(tfs.get(i)).getFailureSeverity();	//la severity ha senso solo in caso di fallimento
				
				//CODICI FALLIMENTI
				if(codes.contains(code)) {
					int index = codes.indexOf(code);
					codeCount.set(index,codeCount.get(index)+1);
				}else {
					codes.add(code);
					codeCount.add(1);
				}
				
				//SEVERITY
				if(severity.contains(severityT)) {
					int index = severity.indexOf(severityT);
					countSeverity.set(index,countSeverity.get(index)+1);
				}else {
					severity.add(severityT);
					countSeverity.add(1);
				}
				
				//aggiorno count metodo fallito
				if(tfg.testFrames.get(tfs.get(i)).getResponseCode() != -1 && tfg.testFrames.get(tfs.get(i)).getResponseCode() != -2) {
					ArrayList<Integer> counts = new ArrayList<Integer>(methodCount.get(method));
					counts.set(FAILURE_INDEX,counts.get(FAILURE_INDEX)+1);
					if(tfg.testFrames.get(tfs.get(i)).getFailureSeverity() == 2) {
						counts.set(SEVERE_FAILURE_INDEX,counts.get(SEVERE_FAILURE_INDEX)+1);
					}
					methodCount.put(method, counts);
				}
				
				for(int j=0; j<FIELDS_NUM; j++) {
					if(hasFieldB.get(j)) {
						hasFieldCount.get(j).set(FAILURE_INDEX,hasFieldCount.get(j).get(FAILURE_INDEX)+1);
						if(tfg.testFrames.get(tfs.get(i)).getFailureSeverity() == 2) {
							hasFieldCount.get(j).set(SEVERE_FAILURE_INDEX,hasFieldCount.get(j).get(SEVERE_FAILURE_INDEX)+1);
						}
					}
				}
				
				failedIndexes.add(tfs.get(i));
				
			}else {				
				//CASO SUCCESSO
				successIndexes.add(tfs.get(i));
			}
			
			for(int j=0; j<FIELDS_NUM; j++) {
				if(hasFieldB.get(j)) 
					hasFieldCount.get(j).set(TOTAL_INDEX,hasFieldCount.get(j).get(TOTAL_INDEX)+1);
			}
			

			
			//TEMPI DI RISPOSTA
			respTimes.add(tfg.testFrames.get(tfs.get(i)).getResponseTime());
			
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
		
	}
	
	public void printTest(TestFrameGenerator tfg, ArrayList<Integer> indexes) {
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
	
	public void printMinMax(TestFrameGenerator tfg) {
		if(maxIndex != -1 && minIndex != -1) {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes.add(maxIndex);
			indexes.add(minIndex);
			
			for(int i = 0; i<2; i++) {
				System.out.println("\n----------------------------------------\nurl: "+tfg.testFrames.get(indexes.get(i)).getUrl());
				System.out.println("Method: "+tfg.testFrames.get(indexes.get(i)).getReqType());
				if(tfg.testFrames.get(indexes.get(i)).getSelPayload() != "null")
					System.out.println("payload: "+ tfg.testFrames.get(indexes.get(i)).getSelPayload());
				System.out.print("Expected Responses: ");
				tfg.testFrames.get(indexes.get(i)).printExpectedResponses();
				System.out.println("\nReturned Response: "+ tfg.testFrames.get(indexes.get(i)).getResponseCode());
				System.out.println("Response Time: "+ tfg.testFrames.get(indexes.get(i)).getResponseTime()+" ms");
			}
		} else {
			System.out.println("[ERROR] Failed to print min max..");
		}
	}
	
	public void printStatistics(TestFrameGenerator tfg, ArrayList<Integer> tfs) {
		System.out.println("\n# Test: " + tfs.size());
		System.out.println("# Failures: " + failedIndexes.size());
				
		//CODICI E count
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
			
			if(codeCount.get(i)<100) {
				System.out.print("       "+ codeCount.get(i)+ "\n");
			}else {
				System.out.print("      "+ codeCount.get(i)+ "\n");
			}
		}
		
		if(triggerOss)
			System.out.println("\n*code -1 = SocketTimeoutException \n*code -2 = HTTP method not yet implemented");
		
		//METODI E count
				System.out.println("\n----- Method ----- # -----");
				for(Map.Entry<String, ArrayList<Integer>>  method : methodCount.entrySet()) {
					
					//voglio stampare solo i metodi che sono falliti almeno una volta
					if(method.getValue().get(FAILURE_INDEX) > 0) {
						
						System.out.print("-      " + method.getKey());
						
						if(method.getKey().length() < 3) {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("         "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("       "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}else if(method.getKey().length() == 3) {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("        "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("       "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}else if(method.getKey().length() == 4) {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("       "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("      "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}else if(method.getKey().length() == 5) {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("      "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("     "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}else if(method.getKey().length() == 6) {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("     "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("    "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}else {
							if(method.getValue().get(FAILURE_INDEX)<100) {
								System.out.print("    "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}else {
								System.out.print("   "+ method.getValue().get(FAILURE_INDEX)+ "\n");
							}
						}
					}
				}
		//METODI E count
		System.out.println("\n\n----- Severity ----- # -----");
		for(int i = 0; i < severity.size(); i++) {

			System.out.print("-        " + severity.get(i));
					
			if(countSeverity.get(i)<100) {
				System.out.print("           "+ countSeverity.get(i)+ "\n");
			}else { 
				System.out.print("          "+ countSeverity.get(i)+ "\n");
			}
		}		
		
		System.out.println("\n----- Response times (success and failures)");
		System.out.println("- max        "+ max+ " ms");
		System.out.println("- min        "+ min+ " ms");
		System.out.println("- avg        "+ avg+ " ms");
		//System.out.println("\n # test with 0 reponse time: "+ zeroRespIndexes.size());
	}
	
	//state-code-method-ic-hasPayload-hasStr-hasInt-hasNum-hasBool
	public void printTestToFile(TestFrameGenerator tfg, ArrayList<Integer> tfs) {
		
		try {
		      FileWriter myWriter = new FileWriter("results.txt"); //FileWriter("results.txt",true) aggiunge il testo a quello esistente.
		      BufferedWriter out = new BufferedWriter(myWriter);
		      
		      out.write("state	code	severity	method	ic	hasPayload	hasDate	hasDateTime	hasInt32	hasInt64	hasFloat	hasDouble	hasEmpty	hasRange	hasSymbol	hasGreater	hasLower	hasBTrue	hasBFalse	hasSRange	hasLang");
		      out.newLine();
		      
		      for(int i = 0; i < tfs.size(); i++) {
		    	  boolean hasDate= false,hasDateTime= false,hasInt32= false,hasInt64= false,hasFloat= false,hasDouble= false,hasEmpty= false, hasRange= false, hasSymbol= false, hasGreater= false, hasLower= false, hasBTrue= false, hasBFalse= false, hasSRange= false, hasLang = false;
		    	  out.write(tfg.testFrames.get(tfs.get(i)).getState() + "	"
		    			  + tfg.testFrames.get(tfs.get(i)).getResponseCode() + "	" 
		    			  + tfg.testFrames.get(tfs.get(i)).getFailureSeverity() + "	" 
		    			  + tfg.testFrames.get(tfs.get(i)).getReqType()  + "	" 
		    			 // + tfg.testFrames.get(tfs.get(i)).getResponseTime() + "	" 
		    			  + tfg.testFrames.get(tfs.get(i)).ic.size() + "	"
		    			  );
		    	  if(tfg.testFrames.get(tfs.get(i)).getPayload() != null) {
		    		  out.write("y	");
		    	  }

		    	  
		    	  for(int j = 0; j<tfg.testFrames.get(tfs.get(i)).ic.size(); j++) {
		    		  String typeTemp = tfg.testFrames.get(tfs.get(i)).ic.get(j).type;
		    		  
		    		  	if(typeTemp.equals("date")) hasDate = true;
		    		  	else if(typeTemp.equals("date-time")) hasDateTime = true;
		    		  	else if(typeTemp.equals("int32")) hasInt32 = true;
		    		  	else if(typeTemp.equals("int64")) hasInt64 = true;
		    		  	else if(typeTemp.equals("float")) hasFloat = true;
		    		  	else if(typeTemp.equals("double")) hasDouble = true;
		    		  	else if(typeTemp.equals("empty")) hasEmpty = true;
		    		  	else if(typeTemp.equals("range")) hasRange = true;
		    		  	else if(typeTemp.equals("symbol")) hasSymbol = true;
		    		  	else if(typeTemp.equals("greater")) hasGreater = true;
		    		  	else if(typeTemp.equals("lower")) hasLower = true;
		    		  	else if(typeTemp.equals("b_true")) hasBTrue = true;
		    		  	else if(typeTemp.equals("b_false")) hasBFalse = true;
		    		  	else if(typeTemp.equals("s_range")) hasSRange = true;
		    		  	else if(typeTemp.equals("lang")) hasLang = true;
		    	  }
		    	  
		    	  if(hasDate) {out.write("y	");}else {out.write("n	");}
		    	  if(hasDateTime) {out.write("y	");}else {out.write("n	");}
		    	  if(hasInt32) {out.write("y	");}else {out.write("n	");}
		    	  if(hasInt64) {out.write("y	");}else {out.write("n	");}
		    	  if(hasFloat) {out.write("y	");}else {out.write("n	");}
		    	  if(hasDouble) {out.write("y	");}else {out.write("n	");}
		    	  if(hasEmpty) {out.write("y	");}else {out.write("n	");}
		    	  if(hasRange) {out.write("y	");}else {out.write("n	");}
		    	  if(hasSymbol) {out.write("y	");}else {out.write("n	");}
		    	  if(hasGreater) {out.write("y	");}else {out.write("n	");}
		    	  if(hasLower) {out.write("y	");}else {out.write("n	");}
		    	  if(hasBTrue) {out.write("y	");}else {out.write("n	");}
		    	  if(hasBFalse) {out.write("y	");}else {out.write("n	");}
		    	  if(hasSRange) {out.write("y	");}else {out.write("n	");}
		    	  if(hasLang) {out.write("y	");}else {out.write("n	");}
		    	  
		    	  out.newLine();
		      }
		      
		      out.close();
		      myWriter.close();
	      } catch (Exception e) {
	        System.out.println("[ERROR] File writing error..");
	        
	      }
		
		
	}
	

}
