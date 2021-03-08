/*
tf == testframes (un testframe identifica una chiamata con determinate classi di input per ogni input)
si differenziano dai testcase perchè questi ultimi sono i test veri e propri che vanno a porre dei valori per quelle classi di input
esempio: input(user, psw) [testframe: input(stringa alfanumerica < 5char, stringa alfanumerica < 10char)] [testcase: input(luca, luca123)]

*/
package parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import dataOperation.TestFrameDistance;
import dataStructure.InputClass;
import dataStructure.TestFrame;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.*;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.refs.RefFormat;


public class TestFrameGenerator {
	
	private static boolean DEBUG_MODE = false;
	private static boolean QUICK_MODE = false;
	private static boolean SMART_MODE = false;
	
	private static final int N_IC_INTEGER = 7;
	private static final int N_IC_STRING = 3;
	private static final int N_IC_LANGUAGE = 3;
	private static final int N_IC_SYMBOL = 3;
	private static final int N_IC_BOOLEAN = 2;
	private static final int N_IC_DEFAULT = 1;
	private static final int N_IC_QUICK = 2;
	/*
	private static final int PATH_MODE = 0;
	private static final int QUERY_MODE = 1;
	private static final int BODY_MODE = 2;
	*/
	public ArrayList<TestFrame> testFrames;
	private int count;
	private int countTfFile;
	private String host;
	public double[][] matrix;
	
	public TestFrameGenerator() throws IOException {
		super();		
		testFrames = new ArrayList<TestFrame>();
		count = 0;
		countTfFile = 0;
		host = "";
	}
	
	public TestFrameGenerator(boolean debug) throws IOException {
		super();		
		testFrames = new ArrayList<TestFrame>();
		count = 0;
		countTfFile = 0;
		DEBUG_MODE = true;
		host = "";
	}

	
	
/****************************************************** FUNZIONE PARSING ************************************************************/

	public void JsonURIParser(String path) throws IOException {
		
		count = 0;
		
		Swagger swagger = new SwaggerParser().read(path);
		int offset = testFrames.size();
		 
		host = swagger.getHost();
		
		if(!host.contains(":")) {
			System.out.println("[WARNING] Host found: \"" + host+"\", Host written: \"localhost/"+host+"\"");
			host = "localhost/"+host;
		}
		
		if(DEBUG_MODE) {
		System.out.println("[DEBUG] Description: " + swagger.getInfo().getDescription());
		System.out.println("[DEBUG] Host: " + host);
		}
		
		
		if(!DEBUG_MODE) {
			countTfFile = 0;
			System.out.print("Test generated:              "); 
		}
		
		//ciclo sulle risorse
		for(Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
			
			if(DEBUG_MODE) {
			System.out.println("\n[DEBUG] --------------------------------------------------------");
			System.out.println("[DEBUG] Path: " + entry.getKey());
			}
			
			String uri =entry.getKey();
	    	 
			//ciclo sui metodi
			for(Map.Entry<HttpMethod, Operation> op : entry.getValue().getOperationMap().entrySet()) {
				
				if(DEBUG_MODE) {
				System.out.println("\n[DEBUG] Method: " + op.getKey());
	    		System.out.println("[DEBUG] Parameters #"+op.getValue().getParameters().size()+" : ");
				}
				
	    		String method = op.getKey().toString();

	    		
	    		
	    		//PARAMETRI
	    		if(op.getValue().getParameters().size() != 0) {
	    			ArrayList<String> types = new ArrayList<String>();
	    			ArrayList<String> names = new ArrayList<String>();
	    			ArrayList<String> modes = new ArrayList<String>();
	    			ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
	    			
		    		//ciclo sui parametri
		    		for(Parameter p : op.getValue().getParameters()) {
		    			 
		    			String paramType = p.getClass().getSimpleName();
		    			 
		    			if(p instanceof BodyParameter) {
		    				if(DEBUG_MODE) 
		    				System.out.println("[DEBUG] 	"+p.getName() + " : " + paramType);
		    				
		    				boolean isRef = parseBody(swagger, (BodyParameter)p, types, names, modes);
		    				//caso in cui non c'� un riferimento ma devo fare parse dello schema
		    				if(!isRef) {
		    					if(DEBUG_MODE) 
		    					System.out.println("[DEBUG] 	No reference found, doing schema parsing..");
		    					
		    					names.add(p.getName());
		    					ModelImpl modelImpl = (ModelImpl) ((BodyParameter) p).getSchema();
		    					//System.out.println(modelImpl.getAdditionalProperties().getType());
		    					types.add(modelImpl.getAdditionalProperties().getType());
		    					modes.add("BodyParameter");
		    				}
			             } else  if(p instanceof QueryParameter) {
			            	 if(DEBUG_MODE) 
			            	 System.out.println("[DEBUG] 	"+p.getName() + ": " + paramType+" Tipo:"+((AbstractSerializableParameter<QueryParameter>) p).getType());
			            	 
			            	 types.add(((AbstractSerializableParameter<QueryParameter>) p).getType());
			            	 names.add(p.getName());
			            	 modes.add("QueryParameter");
			             } else {
			            	 if(DEBUG_MODE) 
			            	 System.out.println("[DEBUG] 	"+p.getName() + ": " + paramType+" Tipo:"+((AbstractSerializableParameter<PathParameter>) p).getType());
			            	 
			            	 types.add(((AbstractSerializableParameter<PathParameter>) p).getType());
			            	 names.add(p.getName());
			            	 modes.add("PathParameter");
			             }
		    		}
		    		
		    		
		    		//RISPOSTE
		    		ArrayList<Integer> responses = new ArrayList<Integer>();
		    		if(op.getValue().getResponses().size() != 0) {
		    			
		    			if(DEBUG_MODE) 
			    		System.out.println("[DEBUG] Responses #"+op.getValue().getResponses().size()+" : ");
		    			
		    			for(Map.Entry<String,Response> resp : op.getValue().getResponses().entrySet()) {
		    				if(DEBUG_MODE) 
			            	 System.out.println("[DEBUG] 	"+resp.getKey()+": "+resp.getValue().getDescription());
			            	 responses.add(Integer.parseInt(resp.getKey()));
		    			}
		    		}
		    		
		    		addTestFrames(types,names, modes, responses, icTemp,uri,method,offset);
		    		
	    		} else {
	    			//caso 0 parametri
		    		testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
		    		
		    		if(op.getValue().getResponses().size() != 0) {
		    		
		    		if(DEBUG_MODE) 
			    	System.out.println("[DEBUG] Responses #"+op.getValue().getResponses().size()+" : ");
		    			
		    		for(Map.Entry<String,Response> resp : op.getValue().getResponses().entrySet()) {
		    			if(DEBUG_MODE) 
			            	System.out.println("[DEBUG] 	"+resp.getKey()+": "+resp.getValue().getDescription());
		    			testFrames.get(offset+count).expectedResponses.add(Integer.parseInt(resp.getKey()));
		    			}
		    		count++;
		    		countTfFile ++;
		    		
					if(!DEBUG_MODE) {
						printCount();
					}
		    		}
	    		}


	    	 }	 
	     }
	     //fine
		
		//stampaTestFrames();
	    //probabilityDefinition();
	}
	
	
	private void addTestFrames(ArrayList<String> types, ArrayList<String> names,ArrayList<String> modes, ArrayList<Integer> responses, ArrayList<InputClass> ic, String uri, String method, int  offset){
				
			ArrayList<InputClass> icIteration = new ArrayList<InputClass>();
			int n = 0;
			
			if(types.size() > 0) {
				n = inputClassEnum(names.get(0),types.get(0), icIteration);
				
				types.remove(0);
				names.remove(0);
			}
			
			if(icIteration.size() > 0) {
				//non sono ad una foglia
				if(types.size() > 0) {
					for (int i = 0; i < n; i++) {
						ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
						ArrayList<String> typesTemp = new ArrayList<String>();
						ArrayList<String> namesTemp = new ArrayList<String>();
						
						for(int j=0; j<ic.size();j++) {
							icTemp.add(ic.get(j));
						}
						icTemp.add(icIteration.get(i));
						
						for(int j=0; j<types.size();j++) {
							typesTemp.add(types.get(j));
							namesTemp.add(names.get(j));
						}
				    			
						//Chiamata ricorsiva
						addTestFrames(typesTemp,namesTemp,modes, responses, icTemp,uri,method,offset);
					}
				} else {
					//CREO TF

					//devo definire gli n nodi foglia
					for (int i = 0; i < n; i++) {
						ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
						for(int j=0; j<ic.size();j++) {
							icTemp.add(ic.get(j));
						}
						icTemp.add(icIteration.get(i));
						
						//System.out.println("Creo TF con IC size:" + icTemp.size());
						
						boolean pathP = false;
						boolean queryP = false;
						boolean bodyP = false;
						String qry = new String();
						String payload = new String();
						for(int j=0; j<icTemp.size();j++) {
														
							if(modes.get(j) == "PathParameter" && !pathP) {
								pathP=true;
							} else if(modes.get(j) == "QueryParameter") {
								if(!queryP) {
									qry = "?"+icTemp.get(j).name+"={"+icTemp.get(j).name+"}";
								}else {
									qry += "&"+icTemp.get(j).name+"={"+icTemp.get(j).name+"}";
								}
								queryP=true;
							} else if(modes.get(j) == "BodyParameter") {
								if(!bodyP) {
									payload = "{\""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";
								}else {
									payload += ", \""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";
								}
								bodyP=true;
							}
						}

						if(pathP && !queryP && !bodyP) {
							testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
						}else if(!pathP && queryP && !bodyP) {
							testFrames.add(new TestFrame("http://"+host+uri+qry, String.valueOf(offset+count), method, "null", DEBUG_MODE));
						}else if((!pathP && !queryP && bodyP) || (pathP && !queryP && bodyP)) {
							payload += "}";
							testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
							testFrames.get(offset+count).setPayload(payload);
						}else {
							System.out.println("Error");
						}
						
						//aggiungo IC
						for(int j=0; j<icTemp.size();j++) {
							//System.out.println("	Aggiungo a TF#"+count+" IC#"+j+" Name: " + icTemp.get(j).name);
							testFrames.get(offset+count).ic.add( icTemp.get(j));
						}
						//Aggiungo expected resp
						testFrames.get(offset+count).expectedResponses = responses;
						
						count++;
						countTfFile ++;
					}
					
					if(!DEBUG_MODE) {
						printCount();
					}
				}
			}
	}

	
	
	
	private int inputClassEnum(String name, String type, ArrayList<InputClass> ic){
		switch (type) {
		
			//ATT: ci sono alcuni casi non considerati (es. number, boolean)
			case "number":
				ic.add(new InputClass(name, "empty", null, null));
				ic.add(new InputClass(name, "range", "1", "100"));	
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "symbol", "null", null));
					ic.add(new InputClass(name, "greater", "2147483647", null));
					ic.add(new InputClass(name, "lower", "-2147483648", null));
					ic.add(new InputClass(name, "range", "-100", "-1"));
					ic.add(new InputClass(name, "symbol", "0", null));
					return N_IC_INTEGER;
				}
				return N_IC_QUICK;
				
			case "boolean":
				ic.add(new InputClass(name, "b_true", null, null));
				ic.add(new InputClass(name, "b_false", null, null));
				return N_IC_BOOLEAN;
				
			case "integer":
				ic.add(new InputClass(name, "empty", null, null));
				ic.add(new InputClass(name, "range", "1", "100"));	
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "symbol", "null", null));
					ic.add(new InputClass(name, "greater", "2147483647", null));
					ic.add(new InputClass(name, "lower", "-2147483648", null));
					ic.add(new InputClass(name, "range", "-100", "-1"));
					ic.add(new InputClass(name, "symbol", "0", null));
					return N_IC_INTEGER;
				}
				return N_IC_QUICK;
				
			case "string": 
				ic.add(new InputClass(name, "empty", null, null));
				ic.add(new InputClass(name, "s_range", "1", "100"));
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "symbol", "null", null));
					return N_IC_STRING;
				}
				return N_IC_QUICK;
				
			case "language":
				ic.add(new InputClass(name, "empty", null, null));
				ic.add(new InputClass(name, "lang", null, null));
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "s_range", "1", "100"));
					return N_IC_LANGUAGE;
				}
				return N_IC_QUICK;
				
			case "s_symbol":
				ic.add(new InputClass(name, "empty", null, null));
				//ic.add(new InputClass("("+name+")", "symbol", j.get("minimum").getAsString(), null));
				ic.add(new InputClass(name, "s_range", "1", "100"));
				return N_IC_SYMBOL-1;			
			default: 
				return N_IC_DEFAULT;
		}
	}
	
	
	public void stampaTestFrames() {
		
		System.out.println();
		System.out.println("\n***********************************************************");
		System.out.println("********************** "+testFrames.size()+" Test Frame **********************\n");
		
		for(int i=0; i<testFrames.size(); i++) {
			System.out.printf("- "+testFrames.get(i).getName()+" | Method:"+ testFrames.get(i).getReqType()+" | #Ic: "+testFrames.get(i).ic.size()+" | Body: "+testFrames.get(i).getPayload()+" | Expected responses: ");
			testFrames.get(i).printExpectedResponses();
			System.out.println();
		}
		
	}

	 
	private static boolean parseBody(Swagger swagger, BodyParameter p, ArrayList<String> types, ArrayList<String> names, ArrayList<String> modes) {
		if(DEBUG_MODE) {
		System.out.println("[DEBUG] BODY: ");
		System.out.println("[DEBUG] 	"+p.getName());
		}
		try {
		RefProperty rp = new RefProperty(p.getSchema().getReference());
		parseReference(swagger, rp, types, names, modes);
		}catch(Exception e) {
			return false;
		}
		return true;
	}

	private static void parseReference(Swagger swagger, RefProperty rp, ArrayList<String> types, ArrayList<String> names, ArrayList<String> modes) {
	    	
		if(rp.getRefFormat().equals(RefFormat.INTERNAL) && swagger.getDefinitions().containsKey(rp.getSimpleRef())) {
			Model m = swagger.getDefinitions().get(rp.getSimpleRef());
	            
			/*
			if(m instanceof ArrayModel) {
				ArrayModel arrayModel = (ArrayModel)m;
				System.out.println(rp.getSimpleRef() + "[]");
		                
				if(arrayModel.getItems() instanceof RefProperty) {
					RefProperty arrayModelRefProp = (RefProperty)arrayModel.getItems();
					parseReference(swagger, arrayModelRefProp, types, names);
				}
			}*/

			if(m.getProperties() != null) {
				for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
					if(DEBUG_MODE) 
					System.out.println("[DEBUG]   " + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
					types.add(propertyEntry.getValue().getType());
					names.add(propertyEntry.getKey());
					modes.add("BodyParameter");
				}
			}
		}
	}	
	    
	    /*
	    private static void printResponses(Swagger swagger, Map<String, Response> responseMap) {
	        System.out.println("Responses:");
	        for(Map.Entry<String, Response> response : responseMap.entrySet()) {
	            System.out.println(response.getKey() + ": " + response.getValue().getDescription());

	            if(response.getValue().getSchema() instanceof RefProperty) {
	                RefProperty rp = (RefProperty)response.getValue().getSchema();
	                printReference(swagger, rp);
	            }

	            if(response.getValue().getSchema() instanceof ArrayProperty) {
	                ArrayProperty ap = (ArrayProperty)response.getValue().getSchema();
	                if(ap.getItems() instanceof RefProperty) {
	                    RefProperty rp = (RefProperty)ap.getItems();
	                    System.out.println(rp.getSimpleRef() + "[]");
	                    printReference(swagger, rp);
	                }
	            }
	        }
	    }*/
	
	
	public void setQuickMode(boolean quickMode){
		QUICK_MODE = quickMode;
	}
	
	public void setSmartMode(boolean smartMode){
		QUICK_MODE = smartMode;
	}
	
	
	
	
/****************************************************** FUNZIONI AUS ************************************************************
*/
	
// Popolazione delle probabilità di fallimento con valori di default e generazione della matrice dei pesi.
//	da spostare nella classe TestFrame
	public void probabilityDefinition(){
		double dim = testFrames.size();
		
		for(int i=0; i < dim; i++){
			testFrames.get(i).setOccurrenceProb(1/dim);
			testFrames.get(i).setFailureProb(0.5);
		}
		
		matrix = new double[testFrames.size()][testFrames.size()];
		TestFrameDistance tfd = new TestFrameDistance();
		
		for(int i=0; i<testFrames.size(); i++){
			for(int j=0; j<testFrames.size(); j++){
				if(i!=j){
					matrix[i][j] = tfd.getTestDistance(testFrames.get(i), testFrames.get(j), 10);
				} else {
					matrix[i][j] = 0;
				}
//				System.out.print(matrix[i][j]+" ");
			}
//			System.out.println();
		}
		
	}
	
//	aggiornamento delle probabilità e della matrice dei pesi
	public void probabilitiesUpdate(double[] op, double[] fp){
		double dim = testFrames.size();
		
		for(int i=0; i < dim; i++){
			testFrames.get(i).setOccurrenceProb(op[i]);
			testFrames.get(i).setFailureProb(fp[i]);
		}
		
		matrix = new double[testFrames.size()][testFrames.size()];
		TestFrameDistance tfd = new TestFrameDistance();
		
		for(int i=0; i<testFrames.size(); i++){
			for(int j=0; j<testFrames.size(); j++){
				if(i!=j){
					matrix[i][j] = tfd.getTestDistance(testFrames.get(i), testFrames.get(j), 10);
				} else {
					matrix[i][j] = 0;
				}
//				System.out.print(matrix[i][j]+" ");
			}
//			System.out.println();
		}
	}

// aggiornamento della matrice dei pesi
	public void matrixUpdate(){
		matrix = new double[testFrames.size()][testFrames.size()];
		TestFrameDistance tfd = new TestFrameDistance();
		
		for(int i=0; i<testFrames.size(); i++){
			for(int j=0; j<testFrames.size(); j++){
				if(i!=j){
					matrix[i][j] = tfd.getTestDistance(testFrames.get(i), testFrames.get(j), 10);
				} else {
					matrix[i][j] = 0;
				}
//				System.out.print(matrix[i][j]+" ");
			}
//			System.out.println();
		}
	}
	
	public void printCount(){
		if(countTfFile-1 <10) {
			System.out.print("\b"+countTfFile);
		}else if(countTfFile-1 <100) {
			System.out.print("\b\b"+countTfFile);
		}else if(countTfFile-1 <1000) {
			System.out.print("\b\b\b"+countTfFile);
		}else if(countTfFile-1 <10000) {
			System.out.print("\b\b\b\b"+countTfFile);
		}else if(countTfFile-1 <100000) {
			System.out.print("\b\b\b\b\b"+countTfFile);
		}else if(countTfFile-1 <1000000) {
			System.out.print("\b\b\b\b\b\b"+countTfFile);
		}else if(countTfFile-1 <10000000) {
			System.out.print("\b\b\b\b\b\b\b"+countTfFile);
		}else if(countTfFile-1 <100000000) {
			System.out.print("\b\b\b\b\b\b\b\b"+countTfFile);
		}else if(countTfFile-1 <1000000000) {
			System.out.print("\b\b\b\b\b\b\b\b\b"+countTfFile);
		}
	}
	
}
