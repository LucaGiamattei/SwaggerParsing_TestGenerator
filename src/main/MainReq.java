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


	//tfg.JsonURIParser(path+"ts-admin-basic-info-service.json");
//tfg.JsonURIParser(path+"ts-admin-order-service.json");
	/*tfg.JsonURIParser(path+"ts-admin-route-service.json");
tfg.JsonURIParser(path+"ts-admin-travel-service.json");
tfg.JsonURIParser(path+"ts-admin-user-service.json");
tfg.JsonURIParser(path+"ts-assurance-service.json");
tfg.JsonURIParser(path+"ts-basic-service.json");
tfg.JsonURIParser(path+"ts-cancel-service.json");
tfg.JsonURIParser(path+"ts-config-service.json");
tfg.JsonURIParser(path+"ts-consign-price-service.json");
tfg.JsonURIParser(path+"ts-consign-service.json");
tfg.JsonURIParser(path+"ts-contacts-service.json");
tfg.JsonURIParser(path+"ts-execute-service.json");
tfg.JsonURIParser(path+"ts-food-map-service.json");
tfg.JsonURIParser(path+"ts-food-service.json");
tfg.JsonURIParser(path+"ts-inside-payment-service.json");
tfg.JsonURIParser(path+"ts-notification-service.json");
tfg.JsonURIParser(path+"ts-order-other-service.json");
tfg.JsonURIParser(path+"ts-order-service.json");
tfg.JsonURIParser(path+"ts-payment-service.json");
tfg.JsonURIParser(path+"ts-preserve-other-service.json");
tfg.JsonURIParser(path+"ts-preserve-service.json");
tfg.JsonURIParser(path+"ts-price-service.json");
tfg.JsonURIParser(path+"ts-rebook-service.json");
tfg.JsonURIParser(path+"ts-route-plan-service.json");
tfg.JsonURIParser(path+"ts-route-service.json");
tfg.JsonURIParser(path+"ts-seat-service.json");
tfg.JsonURIParser(path+"ts-security-service.json");
tfg.JsonURIParser(path+"ts-station-service.json");
tfg.JsonURIParser(path+"ts-ticketinfo-service.json");
tfg.JsonURIParser(path+"ts-train-service.json");
tfg.JsonURIParser(path+"ts-travel-plan-service.json");
tfg.JsonURIParser(path+"ts-travel-service.json");
tfg.JsonURIParser(path+"ts-travel2-service.json");

*/


public class MainReq {

	private static Scanner scan;

	public static void main(String[] args) {
		scan = new Scanner(System.in);
		String path = "/";
		String token = "";
		ArrayList<String> jsonFiles = new ArrayList<String>();

		try {
			String debugMode = "Default\n";
			
			if(args.length >= 1) {
			String fileToRead = args[0];
			if(args.length == 2) {
				debugMode = args[1];
			}
			
			System.out.println("\n\n***************** Login");
			int Selection = 0;
			while(Selection == 0) {
				System.out.println("Select:\n1) Login Admin\n2) Login User\n3) Don't log");
				Selection = scan.nextInt();
				if((Selection != 1)&&(Selection != 2)&&(Selection != 3)) {Selection = 0;}
			}
			
			if(Selection != 3) {
				if(Selection == 1) {
					token = login(1);
				}else {
					token = login(0);
				}
				
				if(token != null) {
					System.out.println("\nLogin done, token acquired..\n");
					//System.out.println("TOKEN: "+token+"\n");
				}else {
					System.out.println("\nLogin failed..\n");
				}
			}
			
			readJsons(fileToRead, jsonFiles);
			
			TestFrameGenerator tfg;
			if (debugMode.equals("debug") || debugMode.equals("DEBUG") ) {
				tfg = new TestFrameGenerator(true);
			} else {
				tfg = new TestFrameGenerator();
			}
			
			for(int i = 0; i<jsonFiles.size(); i++) {
				System.out.println("***************** Parsing files ");
				tfg.JsonURIParser(path+jsonFiles.get(i));
			}
			System.out.println("***************** Parsing Done");
			
			Selection = 0;
			while(Selection==0){
				System.out.println("\n*****************____Main____******************");
				System.out.println("Options:\n1) Start requesting \n2) Print test generated (not recommended for big datasets, "+tfg.testFrames.size()+" tests to print)\n3) Exit");

				Selection = scan.nextInt();
				if((Selection != 1)&&(Selection != 2)&&(Selection != 3)){Selection = 0;}
				
				if(Selection == 1) {
					int N_tf = 0;
					
					while (N_tf == 0 || N_tf > tfg.testFrames.size()) {
						System.out.println("\nInsert the number of test to execute (max: "+ tfg.testFrames.size() +")");
						N_tf = scan.nextInt();
					}
					
					
					System.out.println("\nSelecting "+ N_tf +" random test...");
					
					ArrayList<Integer> tfs = new ArrayList<Integer>();
					Random random = new Random();
					
					for(int i = 0; i < N_tf; i++) {
						int rNum = random.nextInt(tfg.testFrames.size()-1);
						while (tfs.contains(rNum)) {
							rNum = random.nextInt(tfg.testFrames.size()-1);
						}
						tfs.add(rNum);
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
							System.out.println("\nSelect:\n 1) Back to main options \n 2) Print failed test ("+failedIndexes.size()+" test)\n 3) Analyze failures");
							Selection = scan.nextInt();
							if((Selection != 1)&&(Selection != 2)&&(Selection != 3)){Selection = 0;}

							if(Selection == 2) {
								for(int i = 0; i<failedIndexes.size(); i++) {
									System.out.println("\n----------------------------------------\nurl: "+tfg.testFrames.get(failedIndexes.get(i)).getUrl());
									if(tfg.testFrames.get(failedIndexes.get(i)).getSelPayload() != "null")
										System.out.println("payload: "+ tfg.testFrames.get(failedIndexes.get(i)).getSelPayload());
									System.out.print("Expected Responses: ");
									tfg.testFrames.get(failedIndexes.get(i)).printExpectedResponses();
									System.out.println("\nReturned Response: "+ tfg.testFrames.get(failedIndexes.get(i)).getResponseCode());
									System.out.println("Response Time: "+ tfg.testFrames.get(failedIndexes.get(i)).getResponseTime()+" ms");
								}
								break;
							}else if(Selection == 1) {
								break;
							}else if(Selection == 3) {
								System.out.println("\n***************** Failures Analysis");
								
								TestFrameAnalyzer tfa = new TestFrameAnalyzer();
								
								tfa.countResponseCodes(tfg, failedIndexes);
								
								Selection = 0;
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
				System.out.println("Input parameters: \n - Json file name \n - <debug> (optional)");
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
	    	  System.out.println("[ERRORE] Non è possibile leggere il file indicato.");
	      }
	}
	
	private static String login(int admin_mode) {
		String url = "http://localhost:12340/api/v1/users/login";
		int time=5000;
		
		
		HttpURLConnection con = null;
		StringBuffer response = null;

		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();	
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setConnectTimeout(time);
			con.setReadTimeout((time));
			
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			if(admin_mode == 1) {
				wr.write("{\"username\": \"admin\", \"password\": \"222222\"}");
			}else {
				wr.write("{\"username\": \"fdse_microservice\", \"password\": \"111111\"}");
			}
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
	        System.out.println("\n[ERRORE] Problema in connessione");
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
	
}