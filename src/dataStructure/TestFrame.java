package dataStructure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.RandomStringUtils;


public class TestFrame{
	
	private static final boolean DEBUG_MODE = false;
	
	//per il calcolo della distanza
	private String service;

	private String name;
	private String tfID;
	//private ArrayList<TestCase> testCases;
	private double failureProb;
	private double occurrenceProb;

	//inputClass
	public ArrayList<InputClass> ic = new ArrayList<InputClass>();
	//Risposte attese
	public ArrayList<Integer> expectedResponses = new ArrayList<Integer>();

	
	//per le richieste di tipo POST
	private String payload;
	
	//per semplificare le richieste
	private String reqType;
	private String url;
	private String selPayload;
	private int responseCode;
	private StringBuffer response;
	

	private int time=1000;

	private String finalToken;

	public TestFrame() {
		super();
		//testCases = new ArrayList<TestCase>();
		finalToken="";
	}

	public TestFrame(String _name, String _tfID, String _reqType, String _payload) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload= _payload;
		finalToken="";
	}

	public TestFrame(String _name, String _tfID, double _failureProb, double _occurrenceProb) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.failureProb = _failureProb;
		this.occurrenceProb = _occurrenceProb;
		finalToken="";
	}


/*	public TestFrame(String _name, String _tfID, ArrayList<TestCase> _testCases, double _failureProb, double _occurrenceProb) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		//this.testCases = _testCases;
		this.failureProb = _failureProb;
		this.occurrenceProb = _occurrenceProb;
		finalToken="";
	}*/

	public TestFrame(String _service, String _name, String _tfID, String _reqType, double _failureProb, double _occurrenceProb, String _payload) {
		super();
		this.service = _service;
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.failureProb = _failureProb;
		this.occurrenceProb = _occurrenceProb;
		this.payload = _payload;
		finalToken="";
	}

	// HTTP GET request
	private void sendGet() throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("[DEBUG] GET");

		try {
			con.setRequestMethod("GET");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			if(DEBUG_MODE) 
			System.out.println("[DEBUG] GET response: "+ responseCode);

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
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}

	// HTTP POST request
	private void sendPost() throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		if(DEBUG_MODE) 
		System.out.println("[DEBUG] POST");
		
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
			if(DEBUG_MODE) 
			System.out.println("[DEBUG] POST response: "+ responseCode);
			
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
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		if(DEBUG_MODE) 
		System.out.println("[DEBUG] PUT");
		
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
			if(DEBUG_MODE) 
			System.out.println("[DEBUG] PUT response: "+ responseCode);
			
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
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}


	/*public void addTestCase(String name, String tcID){
		TestCase tc = new TestCase(name, tcID);
		testCases.add(tc);
	}*/

	/****************Experimental Version*********************/	
	//	private static Scanner scan = new Scanner(System.in);

	public double expExtractAndExecuteTestCase(){
		int failedTC = 0;

		for(int i=0; i<30; i++){
			if(extractAndExecuteTestCase())
				failedTC++;
		}

		System.out.println("There are "+failedTC+" on 30 Test Cases executed");

		//		System.out.println("Press ENTER to continue");
		//		scan.nextLine();

		return failedTC/(double)30;
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

//		System.out.println(this.tfID+") "+this.reqType+" "+this.url);
		//		System.out.println("Payload: "+this.selPayload);

		try {

			if(this.reqType.equals("GET")||this.reqType.equals("get")){
				this.sendGet();
			} else if(this.reqType.equals("POST")||this.reqType.equals("post")){
				this.sendPost();
			} else{
				this.sendPut();
			}

		} catch(java.net.SocketTimeoutException e){
//			Toolkit.getDefaultToolkit().beep();

			//			System.out.println("Press ENTER to continue");
			//			scan.nextLine();
			if(DEBUG_MODE) 
			System.out.println("[DEBUG] Risposta (exc): "+ responseCode);

			return false;
			//return true;
		} catch (Exception e) {
			if(responseCode == 0){
				//				Toolkit.getDefaultToolkit().beep();
				//
				//				System.out.println("[0] Press ENTER to continue");
				//				scan.nextLine();
				if(DEBUG_MODE) 
				System.out.println("[DEBUG] Risposta (0): "+ responseCode);
				System.out.println(e);
				return false;
			} else if(responseCode >= 500){
				//				/**********************/
				//				try {
				//					Thread.sleep(1000);
				//				} catch (InterruptedException e1) {
				//					// TODO Auto-generated catch block
				//					e1.printStackTrace();
				//				}
				//				/***********************/
				if(DEBUG_MODE) 
				System.out.println("[DEBUG] Risposta (>500): "+ responseCode);
				return true;
			} else{
				return false;
			}
		}

		if(responseCode >= 500){
			/**********************/
			//			try {
			//				Thread.sleep(1000);
			//			} catch (InterruptedException e1) {
			//				// TODO Auto-generated catch block
			//				e1.printStackTrace();
			//			}
			/***********************/
			return true;
		} else{
			return false;
		}

	}



	//	Metodo di prova usato in simulazione
	//	private boolean extractAndExecuteTestCaseDummy(){
	//		double rand = Math.random();
	//		if (rand <= this.failureProb && this.failureProb!=0){
	//			return true;
	//		} else{
	//			return false;
	//		}
	//	}

	public boolean extractAndExecuteTestCaseExperimenationSimulation(){
		if (this.failureProb > 0.5){
			return true;
		} else{
			return false;
		}
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
			case "range":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
			break;

			case "lower":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min)-500, Integer.parseInt(this.ic.get(i).min)-1));
			break;

			case "greater":  sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min)+1, Integer.parseInt(this.ic.get(i).min)+500));
			break;

			case "different":  sel = randomNotInt();
			break;

			case "symbol":  sel = this.ic.get(i).min;
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

			case "value": sel = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			break;

			case "lang": 
				String[] languages = {"ita", "eng", "fra"}; 
				sel = languages[ThreadLocalRandom.current().nextInt(0, languages.length-1)];
				break; 

			default: sel = "error";
			break;
			}

			if(url.contains("{"+this.ic.get(i).name+"}")){
				url = url.replace("{"+this.ic.get(i).name+"}", sel);
			} else if(selPayload.contains("{"+this.ic.get(i).name+"}")){
				selPayload = selPayload.replace("{"+this.ic.get(i).name+"}", sel);
			}

		}
		if(DEBUG_MODE) {
			System.out.println("[DEBUG] url: "+ url);
			System.out.println("[DEBUG] payload: "+ selPayload);
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

}
