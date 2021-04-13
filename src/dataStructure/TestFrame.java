package dataStructure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.*;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomStringUtils;


public class TestFrame{
	
	private static boolean DEBUG_MODE = true;
	
	//per il calcolo della distanza
	private String service;

	private String name;
	private String tfID;
	private int jsonID;
	//private ArrayList<TestCase> testCases;
	private double failureProb;
	private double occurrenceProb;

	//inputClass
	public ArrayList<InputClass> ic = new ArrayList<InputClass>();
	
	public ArrayList<ArrayList<InputClass>> ref = new ArrayList<ArrayList<InputClass>>();
	//Risposte attese
	public ArrayList<Integer> expectedResponses = new ArrayList<Integer>();
	
	//per le richieste di tipo POST
	private String payload;
	
	//per semplificare le richieste
	private String reqType;
	private String url;
	private String selPayload;
	private int responseCode;
	private long responseTime;
	private StringBuffer response;
	

	private int time=1000;

	private String finalToken;
	
	private String state;
	private float priority;
	private int failureSeverity;

	public TestFrame(boolean debug) {
		super();
		//testCases = new ArrayList<TestCase>();
		finalToken="";
		state = "notdefined";
		priority = 0;
	}

	public TestFrame(String _name, String _tfID, String _reqType, String _payload, boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload= _payload;
		finalToken="";
		DEBUG_MODE= debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
	}
	
	public TestFrame(int _jsonID, String _name, String _tfID, String _reqType, String _payload, boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload= _payload;
		finalToken="";
		DEBUG_MODE= debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
		this.jsonID = _jsonID;
	}

	
	// HTTP GET request
	private void sendGet() throws Exception {
		URL obj = new URL(url);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] GET");

		try {
			con.setRequestMethod("GET");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);

			con.setConnectTimeout(time);
			con.setReadTimeout((time));
			 
			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;
			
			//if(DEBUG_MODE) 
			//System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] GET response: "+ responseCode);
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
			
			//print result
			//		System.out.println(response.toString());
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}

	// HTTP POST request
	private void sendPost() throws Exception {

		URL obj = new URL(url);
		long start = System.currentTimeMillis(); 
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] POST");
		
		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			con.setRequestMethod("POST");

			con.setConnectTimeout(time);
			con.setReadTimeout(time);

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(this.selPayload);
			wr.flush();

			
			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;
			
			//if(DEBUG_MODE) 
			//System.out.println("[DEBUG] POST response: "+ responseCode);
			
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(
						new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				
				//print result
				//		System.out.println(response.toString());
			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	// HTTP PUT request
	private void sendPut() throws Exception {
		responseCode = 0;
		URL obj = new URL(url);
		long start = System.currentTimeMillis(); 
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] PUT");
		
		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			con.setRequestMethod("PUT");

			con.setConnectTimeout(time);
			con.setReadTimeout(time);

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(this.selPayload);
			wr.flush();

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;
			//if(DEBUG_MODE) 
			//System.out.println("[DEBUG] PUT response: "+ responseCode);
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(
						new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	// HTTP DELETE request
	private void sendDelete() throws Exception {
		URL obj = new URL(url);
		long start = System.currentTimeMillis(); 
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] DELETE");

		try {
			con.setRequestMethod("DELETE");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;			
			//if(DEBUG_MODE) 
			//System.out.println("[DEBUG] GET response: "+ responseCode);
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	private void sendHead() throws Exception {
		URL obj = new URL(url);
		long start = System.currentTimeMillis(); 
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] HEAD");

		try {
			con.setRequestMethod("HEAD");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;
			
			//if(DEBUG_MODE) 
			//System.out.println("[DEBUG] GET response: "+ responseCode);
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}


	/**************************************/

	//	Metodo per l'esecuzione di un caso di Test estratto dal Test Frame
	public boolean extractAndExecuteTestCase(){
		//		return this.extractAndExecuteTestCaseExperimenationSimulation();
		////		return this.extractAndExecuteTestCaseDummy();
		////		System.out.println("[DEBUG] "+name+" "+this.payload);
		//	}
		//	public boolean extractAndExecuteTestCasebackup(){
		url = this.name;
		selPayload = this.payload;

		if(url.contains("{")||selPayload.contains("{")){
			this.selectTC();
		}
		
		if(DEBUG_MODE) {
			System.out.println("\n----------------------------------------");
			System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] url: "+ url);
			if(selPayload != "null")
			System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] payload: "+ selPayload);
		}

//		System.out.println(this.tfID+") "+this.reqType+" "+this.url);
		//		System.out.println("Payload: "+this.selPayload);

		try {

			if(this.reqType.equals("GET")||this.reqType.equals("get")){
				this.sendGet();
			} else if(this.reqType.equals("POST")||this.reqType.equals("post")){
				this.sendPost();
			} else if(this.reqType.equals("DELETE")||this.reqType.equals("delete")){
				this.sendDelete();
			} else if(this.reqType.equals("PUT")||this.reqType.equals("put")){
				this.sendPut();
			} else if(this.reqType.equals("HEAD")||this.reqType.equals("head")){
				this.sendHead();
			}  else {
				//System.out.println("\n[ERROR] Metodo non implementato: " + this.reqType);
				responseCode = -2;
				state = "failed";
				return false;
			}

		} catch(java.net.SocketTimeoutException e){
			if(DEBUG_MODE) {
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Exception response (timeout): "+ responseCode);
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Response Time: "+ responseTime+" ms");
				System.out.println(e);
			}
			responseCode = -1;
			state = "failed";
			failureSeverity = 0;
			return false;
			
		} catch (Exception e) {
			if(DEBUG_MODE) 
			System.out.println(e);
			
			state = "failed";
			boolean returnValue = false;
			
			for(int i = 0; i < expectedResponses.size(); i++) {
				if(responseCode == expectedResponses.get(i)) {
					returnValue = true;
					state = "success";
				}
			}
			
			if(state.equals("failed") && responseCode>=500) {
				failureSeverity = 2;
			}else if(state.equals("failed")){
				failureSeverity = 1;
			}
			
			if(DEBUG_MODE) {
				if(returnValue){
					System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] ExceptionHandler expected response: "+ responseCode);
					System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Response Time: "+ responseTime+" ms");
	
				} else{
					System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] ExceptionHandler not expected response: "+ responseCode);
					System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Response Time: "+ responseTime+" ms");
				}
			}
			
			return returnValue;
		}
		
		//noException
		boolean returnValue = false;
		state = "failed";
		
		for(int i = 0; i < expectedResponses.size(); i++) {
			if(responseCode == expectedResponses.get(i)) {
				returnValue = true;
				state = "success";
			}
		}
		
		if(state.equals("failed") && responseCode>=500) {
			failureSeverity = 2;
		}else if(state.equals("failed")){
			failureSeverity = 1;
		}
		
		if(DEBUG_MODE) {
			if(returnValue) {
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Expected response received: "+ responseCode);
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Response Time: "+ responseTime+" ms");
			}else {
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Not expected response received: "+ responseCode);
				System.out.println("["+ansi().fgBright(CYAN).a("DEBUG").reset()+"] Response Time: "+ responseTime+" ms");
			}
		}
		
		return returnValue;
	}



	//	Metodo necessario per costruire l'URL da inviare
	private void selectTC(){
		String sel;
		String type;
		
		selPayload = payload;
		
		for(int i=0; i<this.ic.size(); i++){
			type = this.ic.get(i).type;
			
			//System.out.println("[DEBUG] nome parametro: "+ this.ic.get(i).name +" tipo: "+ this.ic.get(i).type);
			
			switch(type){
			case "float":  sel = String.valueOf(ThreadLocalRandom.current().nextFloat());
			break;
			
			case "double":  sel = String.valueOf(ThreadLocalRandom.current().nextDouble());
			break;
			
			case "int32":  sel = String.valueOf(ThreadLocalRandom.current().nextInt());
			break;
			
			case "int64":  
				/*if(this.ic.get(i).defaultValue != null && this.ic.get(i).defaultValue != "") {
					sel = this.ic.get(i).defaultValue;
					break;
				}*/
				sel = String.valueOf(ThreadLocalRandom.current().nextLong());
			break;
			
			case "date": sel = String.valueOf(LocalDate.now());
			break;
			
			case "date-time": 
				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); 
				df.setTimeZone(tz);
				sel = df.format(new Date());
			break;
			
			case "range":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
			break;

			case "lower":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min)-500, Integer.parseInt(this.ic.get(i).min)-1));
			break;

			case "greater":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min)+1, Integer.parseInt(this.ic.get(i).min)+500));
			break;

			case "different":  sel = randomNotInt();
			break;

			case "symbol":  sel = this.ic.get(i).defaultValue;
			break;

			case "empty":	sel = "";
			break;

			case "s_range": sel = RandomStringUtils.randomAlphanumeric(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
			break;

			case "s_greater" :	sel = RandomStringUtils.randomAlphanumeric(Integer.parseInt(this.ic.get(i).min));
			break;

			case "n_range": sel = RandomStringUtils.randomNumeric(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
			break;

			case "n_greater" :	sel = RandomStringUtils.randomNumeric(Integer.parseInt(this.ic.get(i).min));
			break;
			
			case "b_true": sel = "true";
			break;
			
			case "b_false": sel = "false";
			break;

			case "lang": 
				String[] languages = {"ita", "eng", "fra"}; 
				sel = languages[ThreadLocalRandom.current().nextInt(0, languages.length-1)];
				break; 

			default: 
				System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] Type \""+type+"\" of IC not implemented, put \"error\" string in place..");				
				sel = "error";
			break;
			}

			if(url.contains("{"+this.ic.get(i).name+"}")){
				url = url.replace("{"+this.ic.get(i).name+"}", sel);
			} else if(selPayload.contains("{"+this.ic.get(i).name+"}")){
				selPayload = selPayload.replace("{"+this.ic.get(i).name+"}", sel);
			}

		}

	}

	private String randomNotInt(){
		String[] val= {"1.5","true","a","null"};

		return val[ThreadLocalRandom.current().nextInt(0, 4)];
	}


	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTfID() {
		return tfID;
	}
	
	public int getJsonID() {
		return jsonID;
	}

	public void setTfID(String tfID) {
		this.tfID = tfID;
	}

	public double getFailureProb() {
		return failureProb;
	}

	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
	}

	public double getOccurrenceProb() {
		return occurrenceProb;
	}

	public void setOccurrenceProb(double occurrenceProb) {
		this.occurrenceProb = occurrenceProb;
	}

	public String getPayload() {
		return payload;
	}
	
	public String getState() {
		return state;
	}
	public int getFailureSeverity() {
		return failureSeverity;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public void setPriority(float p) {
		this.priority = p;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getUrl(){
		return this.url;
	}
	
	public int getResponseCode(){
		return this.responseCode;
	}
	
	public long getResponseTime(){
		return this.responseTime;
	}


	public String getSelPayload(){
		return this.selPayload;
	}

	public String getFinalToken() {
		return finalToken;
	}

	public void setFinalToken(String finalToken) {
		this.finalToken = finalToken;
	}
	
	public void printExpectedResponses() {
		for(int i=0; i<expectedResponses.size(); i++) {
			System.out.printf(expectedResponses.get(i)+" ");
		}
	}
	
	public void printValidCombination() {
		System.out.printf("Valid/nonValid combination: ");
		for(int i=0; i<ic.size(); i++) {
			if(ic.get(i).valid) { 
				System.out.printf(" V");
			}else {
				System.out.printf(" NV");

			}
		}
	}
	
	public void printTestFrame() {
		System.out.printf("- "+this.getName()+" | Method:"+ this.getReqType()+" | #Ic: "+this.ic.size()+" | Body: "+this.getPayload()+" | Expected responses: ");
		this.printExpectedResponses();
	}

}
