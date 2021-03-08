package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

//import dataOperation.OperationalProfileAlterator;
import parser.TestFrameGenerator;
import analyzer.TestFrameAnalyzer;
//import selector.AWSSelector;
//import selector.AWSSelectorWR;


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
	private static String path = "/";
	private static ArrayList<String> jsonFiles = new ArrayList<String>();
	private static ArrayList<String> configParameters = new ArrayList<String>();

	public static void main(String[] args) {
		scan = new Scanner(System.in);
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
					}
					
					System.out.println("\n***************** Parsing files ");
					for(int i = 0; i<jsonFiles.size(); i++) {
						tfg.JsonURIParser(path+jsonFiles.get(i));
						System.out.println("\n");
					}
					System.out.println("***************** Parsing Done");
					
					Selection = 0;
					while(Selection==0){
						System.out.println("\n*****************__MAIN OPTIONS");
						System.out.println("Options:\n1) Start requesting \n2) Print generated test (not recommended for big datasets, "+tfg.testFrames.size()+" test to print)\n3) Exit");
		
						Selection = scan.nextInt();
						if(Selection < 1 || Selection > 3){Selection = 0;}
						
						if(Selection == 1) {
							int N_tf = 0;
							
							while (N_tf == 0 || N_tf > tfg.testFrames.size()) {
								System.out.println("\nInsert the number of test to execute (max: "+ tfg.testFrames.size() +")");
								N_tf = scan.nextInt();
							}
														
							ArrayList<Integer> tfs = new ArrayList<Integer>();
							Random random = new Random();
							
							if(N_tf ==  tfg.testFrames.size()) {
								System.out.println("\nAll dataset selected..");
								for(int i = 0; i < tfg.testFrames.size(); i++) {
									tfs.add(i);
								}
							}else {
								System.out.println("\nSelecting "+ N_tf +" random test..");

								for(int i = 0; i < N_tf; i++) {
									int rNum = random.nextInt(tfg.testFrames.size()-1);
									while (tfs.contains(rNum)) {
										rNum = random.nextInt(tfg.testFrames.size()-1);
									}
									tfs.add(rNum);
								}
							}
							ArrayList<Integer> failedIndexes = new ArrayList<Integer>();
							
							System.out.println("\nRequesting...\n");
							boolean e = false;
							
							int countReset = 0;
							boolean changeLoading = true;
							
							for(int i=0; i<N_tf ;i++){
								//System.out.println();
								tfg.testFrames.get(tfs.get(i)).setFinalToken(token);
								e=tfg.testFrames.get(tfs.get(i)).extractAndExecuteTestCase();
								
								countReset++;
								if(countReset == 25) {
									if(changeLoading) {
										System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");countReset = 0;
										changeLoading = false;
									}else{System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b////////////////////");countReset = 0;
									changeLoading = true;}
											
								}
								if(!e){
									failedIndexes.add(tfs.get(i));
									e = false;
								}
							}
							System.out.println("\n***************** Requests End");
							
							int success = N_tf-failedIndexes.size();
							System.out.println("\nResults:");
							System.out.println("Test executed: " + N_tf);
							System.out.println("Success: " + success);
							System.out.println("Failures: " + failedIndexes.size());
							
							if(failedIndexes.size() > 0) {
								Selection = 0;
								while(Selection == 0) {
									System.out.println("\nSelect:\n 1) Analyze \n 2) Back to main options");
									Selection = scan.nextInt();
									if(Selection < 1 || Selection > 2){Selection = 0;}
			
									if(Selection == 2) {
										break;
										
									}else if(Selection == 1) {
										System.out.println("\n***************** Analysis");
										
										TestFrameAnalyzer tfa = new TestFrameAnalyzer();
										
										tfa.analyzeTest(tfg, tfs, failedIndexes);
										//Selection = 0;
									}
								}
								Selection = 0;
							}
							Selection = 0;
						}
						
						if(Selection == 2) {
							tfg.stampaTestFrames();
							Selection = 0;
						}
					}
					System.out.println("***************** Exit");
				}else {
					System.out.println("[ERROR] Configuration Failed");
				}
			}else {
				System.out.println("[ERROR] Requested input parameters: \n - Configuration file name \n - <debug> (optional)");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	
	
	//FUNZIONI AUSILIARIE
	
	private static void readJsons(String fileToRead, ArrayList<String> jsonFiles) {
		try {
	        File myObj = new File(fileToRead);
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	          String data = myReader.nextLine();
	          jsonFiles.add(data);
	        }
	        myReader.close();
	      } catch (Exception e) {
	    	  System.out.println("[ERROR] Json retrieval: File read failed.");
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
				in.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
	        System.out.println("\n[ERROR] Login: Connection problem.");
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
	          configParameters.add(data);
	        }
	        myReader.close();
	      } catch (Exception e) {
	    	  System.out.println("[ERROR] Configuration: File read failed.");
	      }
		return parseConfigParameters();
	}
	

	private static boolean parseConfigParameters() {
		
		boolean returnVal = false;
		
		if(configParameters.size() >= 3 && configParameters.size() <= 6) {
			//jsonsFile parsing
			jsonsFile = configParameters.get(0).split("=")[1];
			testMode = configParameters.get(1).split("=")[1];
			if(testMode.equals("quickmode") || testMode.equals("normalmode")) {
				loginMode = configParameters.get(2).split("=")[1];
				if(loginMode.equals("log") || loginMode.equals("nolog")) {
					if(loginMode.equals("log")) {
						username = configParameters.get(3).split("=")[1];
						password = configParameters.get(4).split("=")[1];
						loginUrl = configParameters.get(5).split("=")[1];
					}
					returnVal = true;
				}
			}
		}
		
		return returnVal;
	}
	
}