package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//import dataOperation.OperationalProfileAlterator;
import parser.TestFrameGenerator;
import prioritizer.PrioritySorter;
import prioritizer.TestFramePrioritizer;
import analyzer.TestFrameAnalyzer;
import dataStructure.TestFrame;
//import selector.AWSSelector;
//import selector.AWSSelectorWR;

import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;


/* Configuration Parameters
 * 1) file jsons (filename.txt)
 * 2) quickmode (quickmode, normalmode)
 * 3) loginmode (log, nolog)
 * 4) username (optional)
 * 5) password (optional)
 * 6) loginUrl (optional)
 * */

public class MainReq {

	private static Scanner scan;
	private static String debugMode = "";
	private static String loginMode = "";
	private static String testMode = "";
	private static String jsonsFile = "";
	private static String username = "";
	private static String password = "";
	private static String token = "";
	private static String loginUrl = "";
	private static String priorityMode = "";
	private static String path = "/";
	private static String weightsFile = "weights.txt";
	private static int dynamicRequestBuffer = 50;
	private static ArrayList<String> jsonFiles = new ArrayList<String>();
	private static ArrayList<String> configParameters = new ArrayList<String>();
	
	
	

	public static void main(String[] args) {
		scan = new Scanner(System.in);
		
		AnsiConsole.systemInstall();
		//String path = "/";
		int Selection = 0;
		
		try {
			
			if(args.length >= 1) {
				String configFile = args[0];
				if(args.length == 2) {
					debugMode = args[1];
				}else {
					debugMode = "nodebug";
				}
				
				boolean configDone  = readConfig(configFile);
				if(configDone) {
					
					System.out.println("\n\n***************** CONFIGURATION");
					System.out.println("Json File: " + jsonsFile);
					System.out.println("Test mode: " + testMode);
					System.out.println("Priority mode: " + priorityMode);
					System.out.println("Debug mode: " + debugMode);
					System.out.println("Login mode: " + loginMode);
					if(loginMode.equals("log")) {
						System.out.println("	Username: " + username);
						System.out.println("	Password: " + password);
						System.out.println("	loginUrl: " + loginUrl);
					}
					
					//Login Setting
					if(loginMode.equals("log")) {
						token = login();
						if(token != null) {
							System.out.println("\nLogin done, token acquired..");
							//System.out.println("TOKEN: "+token+"\n");
						}else {
							System.out.println("\nLogin failed..");
						}
					}else if(loginMode.equals("token")) {
						if(token != null) {
							System.out.println("\nToken correctly acquired from config file..");
							//System.out.println("TOKEN: "+token+"\n");
						}else {
							System.out.println("\nFailed to get token from config file..");
						}
					}
					
					//json files reading
					readJsons(jsonsFile, jsonFiles);
					
					//debug/nodebug tfg creating
					TestFrameGenerator tfg;
					if (debugMode.equals("debug") || debugMode.equals("DEBUG") ) {
						tfg = new TestFrameGenerator(true);
					} else {
						tfg = new TestFrameGenerator();
					}
					
					//quickmode Setting
					if(testMode.equals("quickmode")) {
						tfg.setQuickMode(true);
					}else if(testMode.equals("pairwisemode")) {
						tfg.setPairWiseMode(true);
					}else if(testMode.equals("validmode")) {
						tfg.setValidMode(true);
					}else if(testMode.equals("validmodeP")) {
						tfg.setValidModeP(true);
					}else if(testMode.equals("invalidmode")) {
						tfg.setInvalidModeP(true);
					}else if(testMode.equals("pairwisenvmode")) {
						tfg.setPairWiseNVMode(true);
					}else if(testMode.equals("validinvalidmode")) {
						tfg.setValidInvalidMode(true);
					}else if(testMode.equals("quickboundedmode")) {
						tfg.setQuickBoundedModeMode(true);
					}
					
					System.out.println("\n***************** Parsing files ");
					for(int i = 0; i<jsonFiles.size(); i++) {
						tfg.JsonURIParser(path+jsonFiles.get(i),i+1);
						System.out.println("\n");
					}
					System.out.println("***************** Parsing Done");
					
					
					
					
					
					Selection = 0;
					while(Selection==0){
						System.out.println("\n*****************__MAIN OPTIONS");
						System.out.println("Options:\n1) Start requesting (random) \n2) Start requesting (dynamic priority) \n3) Start requesting (file priority) \n4) Print generated test (not recommended for big datasets, "+tfg.testFrames.size()+" test to print)\n5) Exit");
						
						boolean openSecondLevelOptions = false;
						ArrayList<Integer> tfs = new ArrayList<Integer>();
						
						Selection = scan.nextInt();
						if(Selection < 1 || Selection > 5){Selection = 0;}
						
						else if(Selection == 1) {
							
							//-------------------------------------------------------------------- MAIN SEL 1 --------------------------------------------------------------------
							int nTf = 0;
							
							while (nTf == 0 || nTf > tfg.testFrames.size()) {
								System.out.println("\nInsert the number of test to execute (max: "+ tfg.testFrames.size() +")");
								nTf = scan.nextInt();
							}
							
							//SELEZIONE TEST							
							Random random = new Random();
							
							if(nTf ==  tfg.testFrames.size()) {
								System.out.println("\nAll dataset selected..");
								for(int i = 0; i < tfg.testFrames.size(); i++) {
									tfs.add(i);
								}
							}else {
								System.out.println("\nSelecting "+ nTf +" random test..");

								for(int i = 0; i < nTf; i++) {
									int rNum = random.nextInt(tfg.testFrames.size()-1);
									while (tfs.contains(rNum)) {
										rNum = random.nextInt(tfg.testFrames.size()-1);
									}
									tfs.add(rNum);
								}
							}
							long startReq = System.currentTimeMillis();
							
							startRequesting(tfg,tfs,nTf);
							
							long reqTime = System.currentTimeMillis() - startReq ;
							
							System.out.println("\nTotal time: "+ new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
							
							openSecondLevelOptions = true;
							
						} else if(Selection == 2) {
							
							
							//-------------------------------------------------------------------- MAIN SEL 2 --------------------------------------------------------------------	
							//seleziono i primi N con maggiore priorità
							int nTf = 0;
							
							while (nTf == 0 || nTf > tfg.testFrames.size()) {
								System.out.println("\nInsert the number of test to execute (max: "+ tfg.testFrames.size() +")");
								nTf = scan.nextInt();
							}
							
							long startReq = System.currentTimeMillis();
							
							startDynamicRequesting(tfg,tfs,nTf);
							
							long reqTime = System.currentTimeMillis() - startReq ;
							
							System.out.println("\nTotal time: "+ new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
							
							openSecondLevelOptions = true;
							
						} else if(Selection == 3) {
							
							//-------------------------------------------------------------------- MAIN SEL 3 --------------------------------------------------------------------
							//leggo da file i pesi e prioritizzo
							TestFramePrioritizer tfp;
							int nTf = 0;
							
							if (priorityMode.equals("onlysevere")) {
								tfp = new TestFramePrioritizer(true);
							}else {
								tfp = new TestFramePrioritizer(false);
							}
							
							tfp.getWeightsFromFile(weightsFile);
							tfp.printWeights();
							tfp.prioritizeTest(tfg.testFrames);
							
							tfg.testFrames.sort(new PrioritySorter());
							Collections.reverse(tfg.testFrames);
							
							//seleziono i primi N con maggiore priorità
							while (nTf == 0 || nTf > tfg.testFrames.size()) {
								System.out.println("\nInsert the number of test to execute (max: "+ tfg.testFrames.size() +")");
								nTf = scan.nextInt();
							}
							
							for (int i = 0; i< nTf; i++) {tfs.add(i);}
							
							long startReq = System.currentTimeMillis();
							
							startRequesting(tfg,tfs,nTf);
							
							long reqTime = System.currentTimeMillis() - startReq ;
							
							System.out.println("\nTotal time: "+ new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
							
							openSecondLevelOptions = true;
							
							
						} else if(Selection == 4) {
							
							//-------------------------------------------------------------------- MAIN SEL 4 --------------------------------------------------------------------
							
							tfg.stampaTestFrames();
							Selection = 0;
							
						}
						
						//-------------------------------------------------------------------- AFTER REQUEST OPTIONS --------------------------------------------------------------------
						if(openSecondLevelOptions) {
							Selection = 0;
							while(Selection == 0) {
								System.out.println("\nSelect:\n 1) Analyzer \n 2) Prioritizer \n 3) Back to main options");
								Selection = scan.nextInt();
								if(Selection < 1 || Selection > 3){Selection = 0;}
			
								if(Selection == 3) {
									break;
										
								}else if(Selection == 1) {
									
									//ANALISI TEST EFFETTUATI
									System.out.println("\n***************** Analysis");
										
									TestFrameAnalyzer tfa = new TestFrameAnalyzer();
										
									tfa.analyzeTest(tfg.testFrames, tfs);
									
									Selection = 0;
									while(Selection == 0) {
										System.out.println("\nSelect:\n1) Calculate Statistics\n"
												+ "2) Print failed test ("+tfa.failedIndexes.size()+")\n"
												+ "3) Print success test ("+tfa.successIndexes.size()+")\n"
												+ "4) Print max/min response time test\n"
												+ "5) Print data to file\n"
												+ "6) Calculate Statistics by Json\n"
												+ "7) Exit from analyzer");
										Selection = scan.nextInt();
										if(Selection < 1 || Selection > 7) {Selection = 0;}
										
										else if (Selection == 1) {
											//statistics
											tfa.printStatistics(tfg, tfs);
											Selection = 0;
											
										}else if (Selection == 2) {
											tfa.printTest(tfg.testFrames, tfa.failedIndexes);
											Selection = 0;
											
										}else if (Selection == 3) {
											tfa.printTest(tfg.testFrames, tfa.successIndexes);
											Selection = 0;
											
										}else if(Selection == 4) {
											//stampa min max	
											tfa.printMinMax(tfg);
											Selection = 0;
											
										}else if(Selection == 5) {
											tfa.printTestToFile(tfg, tfs);
											Selection = 0;
											
										}else if(Selection == 6) {
											tfa.printStatisticsByJson(tfg.testFrames, tfs);
											Selection = 0;
											
										}else if(Selection == 7) {
											break;
										}
									}
									Selection = 0;
								
								}else if(Selection == 2) {
									//PRIORITIZZAZIONE TEST EFFETTUATI
									System.out.println("\n***************** Prioritization");
									TestFramePrioritizer tfp;
									
									if (priorityMode.equals("onlysevere")) {
										tfp = new TestFramePrioritizer(true);
									}else {
										tfp = new TestFramePrioritizer(false);
									}
									
									Selection = 0;
									while(Selection == 0) {
										System.out.println("\nSelect:\n1) Calculate Weights\n"
												+ "2) Print Weights to Console \n"
												+ "3) Prioritize Test \n"
												+ "4) Print Weights to File \n"
												+ "5) Exit from Prioritizer");
										Selection = scan.nextInt();
										if(Selection < 1 || Selection > 5) {Selection = 0;}
										
										else if (Selection == 1) {
											TestFrameAnalyzer tfa = new TestFrameAnalyzer();
											tfa.analyzeTest(tfg.testFrames, tfs);
											tfp.calculateWeights(tfa);			
											Selection = 0;
											
										}else if (Selection == 2) {
											tfp.printWeights();
											Selection = 0;
											
										}else if (Selection == 3) {
											tfp.prioritizeTest(tfg.testFrames);
											Selection = 0;

										}else if(Selection == 4) {
											tfp.printWeightsToFile(weightsFile);
											Selection = 0;
											
										}else if(Selection == 5) {
											break;
										}
									}
									
									Selection = 0;
								}
							}
							openSecondLevelOptions = false;
							Selection = 0;
						}
						
					}
					System.out.println("***************** Exit");
				}else {
					System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Configuration Failed");
				}
			}else {
				System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Requested input parameters: \n - Configuration file name \n - <debug> (optional)");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		AnsiConsole.systemUninstall();
	}
	
	
	
	
	
	// FUNZIONI AUSILIARIE *****************************************************************************************************************************
	
	private static void startDynamicRequesting(TestFrameGenerator tfg, ArrayList<Integer> tfs, int nTf) {
		System.out.println();
		System.out.println(ansi().fgBright(MAGENTA).a("Requesting..").reset());
		System.out.println();
		
		boolean e = false;
		int internalErrorCount = 0;
		TestFrameAnalyzer tfa = new TestFrameAnalyzer();
		TestFramePrioritizer tfp;
		
		if (priorityMode.equals("onlysevere")) {
			tfp = new TestFramePrioritizer(true);
		}else {
			tfp = new TestFramePrioritizer(false);
		}
		
		ArrayList<TestFrame> testNotDone = new ArrayList<TestFrame>(tfg.testFrames);
		ArrayList<Integer> indexToAnalyze = new ArrayList<Integer>();
		
		int failCount = 0;
		int countReset = 1;
		int changeLoading = 0;
		Random random = new Random();
		int bufferIndex =0;
		
		while(tfs.size() < dynamicRequestBuffer) {
			bufferIndex = random.nextInt(testNotDone.size()-1);
			tfs.add(bufferIndex);
			
			//first random request
			tfg.testFrames.get(bufferIndex).setFinalToken(token);
			e=tfg.testFrames.get(bufferIndex).extractAndExecuteTestCase();
			if(!e) {
				if(tfg.testFrames.get(tfs.get(bufferIndex)).getResponseCode() >= 0) {
					failCount++;
				}else {
					internalErrorCount++;
				}
				e = false;
			}
			testNotDone.remove(bufferIndex);
			indexToAnalyze.add(bufferIndex);
		}
		
		while(tfs.size() < nTf) {			
			tfa.analyzeTest(tfg.testFrames, indexToAnalyze);
			tfp.calculateWeights(tfa);
			tfp.prioritizeTest(testNotDone);
			
			testNotDone.sort(new PrioritySorter());
			Collections.reverse(testNotDone);

			//prendo indice
			int tfgIndex = 0;
			for(int i=0;i<tfg.testFrames.size();i++) {
				if(testNotDone.get(0).getTfID() == tfg.testFrames.get(i).getTfID()) {
					tfs.add(i);
					tfgIndex=i;
				}
			}
			
			tfg.testFrames.get(tfgIndex).setFinalToken(token);
			e=tfg.testFrames.get(tfgIndex).extractAndExecuteTestCase();
			if(!e) {
				if(tfg.testFrames.get(tfs.get(tfgIndex)).getResponseCode() >= 0) {
					failCount++;
				}else {
					internalErrorCount++;
				}
				e = false;
			}
			
			testNotDone.remove(0);
			indexToAnalyze.add(tfgIndex);
			
			changeLoading = loadingPrint(countReset++,changeLoading);
			
		}
		
		/*	1)effettuo prima richiesta random
		 * 	2)calcolo pesi con il risultato
		 * 	3)ordino sulla priorità
		 * 	4)eseguo prima istruzione che trovo che non ho già eseguito
		 * */
		
		int execTest = nTf - internalErrorCount;
		int successCount = execTest-failCount;
		
		System.out.println();
		System.out.println("***************** Requests End");
		
		System.out.println();
		System.out.println("Results:");
		System.out.println("Test executed: " + execTest);
		System.out.println("Success: " + successCount);
		System.out.println("Failures: " + failCount);
		tfp.printWeights();
	}
	
	
	private static void startRequesting(TestFrameGenerator tfg, ArrayList<Integer> tfs, int nTf) {
		System.out.println();
		System.out.println(ansi().fgBright(MAGENTA).a("Requesting..").reset());
		System.out.println();
		boolean e = false;
		
		int countReset = 0;
		int failCount =0;
		int changeLoading = 0;
		int internalErrorCount = 0;
		
		//INVIO RICHIESTE
		for(int i=0; i<nTf ;i++){
			//System.out.println();
			tfg.testFrames.get(tfs.get(i)).setFinalToken(token);
			e=tfg.testFrames.get(tfs.get(i)).extractAndExecuteTestCase();
			
			changeLoading = loadingPrint(countReset++,changeLoading);
			
			if(!e){
				if(tfg.testFrames.get(tfs.get(i)).getResponseCode() >= 0) {
					failCount++;
				}else {
					internalErrorCount++;
				}
				e = false;
			}
		}
		System.out.println("\n***************** Requests End");
		
		int execTest = nTf-internalErrorCount;
		int successCount = execTest - failCount;
		
		System.out.println("\nResults:");
		System.out.println("Test executed: " + execTest);
		System.out.println("Success: " + successCount);
		System.out.println("Failures: " + failCount);
	}
	
	
	private static int loadingPrint(int countReset,int changeLoading) {
		
		if(countReset % 25 == 0) {
			if(changeLoading > 8) {
				changeLoading = 1;
			}else {
				changeLoading++;
			}
			
			if(changeLoading == 1) {
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("||||||").reset());
			}else if(changeLoading == 2){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("//////").reset());
			}else if(changeLoading == 3){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("------").reset());
			}else if(changeLoading == 4){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("\\\\\\\\\\\\").reset());
			}else if(changeLoading == 5){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("||||||").reset());
			}else if(changeLoading == 6){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("//////").reset());
			}else if(changeLoading == 7){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("------").reset());
			}else if(changeLoading == 8){
				System.out.print("\b\b\b\b\b\b"+ansi().fgBright(MAGENTA).a("\\\\\\\\\\\\").reset());
			}
					
		}
		return changeLoading;
	}
	
	
	private static void readJsons(String fileToRead, ArrayList<String> jsonFiles) {
		try {
	        File myObj = new File(fileToRead);
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	          String data = myReader.nextLine();
	          if(!data.contains("**"))
	        	  jsonFiles.add(data);
	        }
	        myReader.close();
	      } catch (Exception e) {
	    	  System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Json retrieval: File read failed.");
	      }
	}
	
	private static String login() {
		int time=5000;
		
		
		HttpURLConnection con = null;
		StringBuffer response = null;

		try {
			URL obj = new URL(loginUrl);
			con = (HttpURLConnection) obj.openConnection();	
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setConnectTimeout(time);
			con.setReadTimeout((time));
			
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			
			wr.write("{\"username\": \""+username+"\", \"password\": \""+password+"\"}");
				
			wr.flush();

			int responseCode = con.getResponseCode();
			
			//System.out.println("[INFO] Response code: "+ responseCode);
			
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
					
				}
				//System.out.println("\n[INFO] Login Response: " + response);
				in.close();
			}
			
		} catch (IOException e) {
	        System.out.println("\n"+"["+ansi().fgBright(RED).a("ERROR").reset()+"] Login: Connection problem.");
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		
		if(response != null)
			return getToken(response.toString());
		else 
			return null;
	}
	
	private static String getToken(String json) {		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);
		JsonObject obj = element.getAsJsonObject();
		JsonObject data = obj.getAsJsonObject("data");
		
		return data.get("token").toString().replace("\"","");
	}
	
	
	private static boolean readConfig(String configFile) {
		try {
	        File myObj = new File(configFile);
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	          String data = myReader.nextLine();
	          if(!data.contains("**"))
	        	  configParameters.add(data);
	        }
	        myReader.close();
	      } catch (Exception e) {
	    	  System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Configuration: File read failed.");
	      }
		return parseConfigParameters();
	}
	

	private static boolean parseConfigParameters() {
		
		boolean returnVal = false;
		
		//controllo che il numero di parametri sia corretto
		if(configParameters.size() >= 4 && configParameters.size() <= 7) {
			//jsonsFile parsing
			jsonsFile = configParameters.get(0).split("=")[1];
			testMode = configParameters.get(1).split("=")[1];
			priorityMode = configParameters.get(2).split("=")[1];
			if(testMode.equals("quickmode") 
					|| testMode.equals("normalmode") 
					|| testMode.equals("pairwisemode") 
					|| testMode.equals("validmode") 
					|| testMode.equals("validmodeP") 
					|| testMode.equals("invalidmode")
					|| testMode.equals("pairwisenvmode")
					|| testMode.equals("validinvalidmode")
					|| testMode.equals("quickboundedmode")) {
				
				loginMode = configParameters.get(3).split("=")[1];
				if(loginMode.equals("log") || loginMode.equals("nolog")|| loginMode.equals("token")) {
					if(loginMode.equals("log")) {
						username = configParameters.get(4).split("=")[1];
						password = configParameters.get(5).split("=")[1];
						loginUrl = configParameters.get(6).split("=")[1];
					}else if(loginMode.equals("token")) {
						token = configParameters.get(4).split("=")[1];
					}
					returnVal = true;
				}else {System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Configuration parsing: Wrong logMode");}
			}else{System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Configuration parsing: Wrong testMode");}
		}
		
		return returnVal;
	}
	
}