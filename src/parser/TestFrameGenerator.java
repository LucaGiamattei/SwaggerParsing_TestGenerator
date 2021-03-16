/*
tf == testframes (un testframe identifica una chiamata con determinate classi di input per ogni input)
si differenziano dai testcase perchÃ¨ questi ultimi sono i test veri e propri che vanno a porre dei valori per quelle classi di input
esempio: input(user, psw) [testframe: input(stringa alfanumerica < 5char, stringa alfanumerica < 10char)] [testcase: input(luca, luca123)]

*/
package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import dataStructure.InputClass;
import dataStructure.TestFrame;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.*;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.refs.RefFormat;


public class TestFrameGenerator {
	
	private static boolean DEBUG_MODE = false;
	private static boolean QUICK_MODE = false;
	private static boolean PAIRWISE_MODE = false;
	
	private static final int N_IC_INTEGER = 7;
	private static final int N_IC_STRING = 3;
	private static final int N_IC_LANGUAGE = 3;
	private static final int N_IC_SYMBOL = 3;
	private static final int N_IC_BOOLEAN = 2;
	private static final int N_IC_DEFAULT = 0;
	private static final int N_IC_QUICK = 2;
	
	public ArrayList<TestFrame> testFrames;
	private int count;
	private int offset;
	private int countTfFile;
	private String host;
	private static boolean skipMethod = false;
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

	@SuppressWarnings("unchecked")
	public void JsonURIParser(String path) throws IOException {
		
		if(PAIRWISE_MODE)
			QUICK_MODE=true;
		
		Swagger swagger = new SwaggerParser().read(path);
		
		count = 0;
		countTfFile = 0;
		offset = testFrames.size();
		host = swagger.getHost();
		
		if(!host.contains(":")) {
			System.out.println("[WARNING] Host found: \"" + host+"\", Host written: \"localhost/"+host+"\"");
			host = "localhost/"+host;
		}
		
		if(DEBUG_MODE) {
		System.out.println("[DEBUG] Description: " + swagger.getInfo().getDescription());
		System.out.println("[DEBUG] Host: " + host);
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
	    		System.out.println("[DEBUG]	Parameters #"+op.getValue().getParameters().size()+" : ");
				}
				
	    		String method = op.getKey().toString();	    		
	    		
	    		//PARAMETRI
	    		if(op.getValue().getParameters().size() != 0) {
	    			
	    			ArrayList<String> types = new ArrayList<String>();
	    			ArrayList<String> formats = new ArrayList<String>();
	    			ArrayList<String> names = new ArrayList<String>();
	    			ArrayList<String> modes = new ArrayList<String>();
	    			ArrayList<String> referenceNames = new ArrayList<String>();
	    			ArrayList<String> arrayNames = new ArrayList<String>();
	    			ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
	    			
		    		//ciclo sui parametri
		    		for(Parameter p : op.getValue().getParameters()) {
		    			 
		    			String paramType = p.getClass().getSimpleName();
		    			
		    			//BODYPARAMETER
		    			if(p instanceof BodyParameter) {
		    				if(DEBUG_MODE) 
			    				System.out.println("[DEBUG]		"+p.getName() + " : " + paramType);
		    				
		    				if(method.equals("DELETE")||method.equals("delete"))
		    					System.out.println("[WARNING] Found body parameter in DELETE method, will probably fail this test.. ("+uri+")");
		    				
		    				if(method.equals("GET")||method.equals("get")) {
		    					System.out.println("[WARNING] Found body parameter in GET method, not implemented, skip parsing and send without payload.. ("+uri+")");
		    				} else {
		    					
		    					
		    					//System.out.println("[NFO] Array dimensions before body: " + types.size() + " "+ names.size() + " "+ formats.size() + " "+ modes.size());
			    				parseBody(swagger, (BodyParameter)p, types, names, modes, formats);
			    				//System.out.println("[NFO] Array dimensions after body: " + types.size() + " "+ names.size() + " "+ formats.size() + " "+ modes.size());

		    				}
		    				
		    				//QUERYPARAMETER
			             } else  if(p instanceof QueryParameter) {
			            	 if(DEBUG_MODE) 
			            	 System.out.println("[DEBUG]		"+p.getName() + ": " + paramType+" Type:"+((AbstractSerializableParameter<QueryParameter>) p).getType()+" ("+((AbstractSerializableParameter<QueryParameter>) p).getFormat()+")");
			            	 
			            	 if(((AbstractSerializableParameter<QueryParameter>) p).getType().equals("array")) {
			            		 Property items = ((AbstractSerializableParameter<QueryParameter>) p).getItems();
			            		 
			            		 types.add(items.getType());
			            		 formats.add(items.getFormat());
			            		 modes.add("QueryParameter_a");
			            	 }else {
				            	 types.add(((AbstractSerializableParameter<QueryParameter>) p).getType());
				            	 modes.add("QueryParameter");
				            	 formats.add(((AbstractSerializableParameter<QueryParameter>) p).getFormat());
			            	 }
			            	 names.add(p.getName());
			            	 
			             } else  if(p instanceof PathParameter){
			            	//PATHPARAMETER
			            	 if(DEBUG_MODE) 
				            	 System.out.println("[DEBUG]		"+p.getName() + ": " + paramType+" Type:"+((AbstractSerializableParameter<PathParameter>) p).getType()+" ("+((AbstractSerializableParameter<PathParameter>) p).getFormat()+")");
				            	 
				            	 types.add(((AbstractSerializableParameter<PathParameter>) p).getType());
				            	 names.add(p.getName());
				            	 modes.add("PathParameter");
				            	 formats.add(((AbstractSerializableParameter<PathParameter>) p).getFormat());
				            	 
			             }else {
			            	 System.out.println("[ERROR] Found parameter not supported.. (Supported: BodyParameter, QueryParameter, PathParameter)");
			             }
		    		}
		    		
		    		if (!skipMethod) {
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
			    				    		
			    		//variabile utile solo nel pairwise_mode
			    		int notValidCount=2;	
			    		
			    		//Se ho 1 solo parametro body allora voglio considerare solo quello non valido (ovviamente nel caso pairwise_mode)
			    		if(PAIRWISE_MODE) {		    			
			    			int countBodyPar=0;
			    			for(int i = 0; i < modes.size(); i++) 
			    				if(modes.get(i).equals("BodyParameter"))
			    					countBodyPar++;
			    			
				    		if(countBodyPar==1) {
				    			if(DEBUG_MODE) 
					            	 System.out.println("[DEBUG][Pairwise_mode] Found only 1 BodyParameter, setting notValidCount to 1..");
				    			notValidCount=1;
				    		}
			    		}
			    		
			    		//caso in cui ho trovato solo 1 parametro body nella GET
			    		if((method.equals("GET")||method.equals("GET"))&& types.size()==0) {
			    			//uso il caso 0 parametri
			    			add0ParameterTestFrame(uri,method,op.getValue());
			    		}else {
			    		//ESPLORAZIONE ALBERO E GENERAZIONE TF 
			    		exploreInputTree(types, formats, names, modes,referenceNames,arrayNames, responses, icTemp,uri,method,notValidCount,0,true);
			    		}
		    		}	
		    		skipMethod = false;
	    		} else {
	    			//caso 0 parametri
	    			add0ParameterTestFrame(uri,method,op.getValue());
	    		}
	    		
				
	    	 }	 
	     }
	     //fine
		System.out.println("\nTest generated:	"+ countTfFile);
	}
	
	
	
	
	
	
	
	
	//-------------------------------------- FUNZIONI AGGIUNTA TESTFRAMES
	
	
	//funzione ricorsiva esplorazione albero di input
	private void exploreInputTree(ArrayList<String> types,ArrayList<String> formats, ArrayList<String> names,ArrayList<String> modes,ArrayList<String> referenceNames,ArrayList<String> arrayNames, ArrayList<Integer> responses, ArrayList<InputClass> ic, String uri, String method, int notValidCount, int paramIndex,boolean validBranch){
			
			ArrayList<InputClass> icIteration = new ArrayList<InputClass>();
			int n = 0;
			
			if(types.size() > 0) {
				
				/*	Se sono stati trovati parametri ref (riferimenti) questi sono stati normalmente aggiunti ma devono essere saltati nell'esplorazione */
				if(types.get(0).equals("ref")||types.get(0).equals("array")) {
					
					//ATT: devo costruire una copia del vettore così che iterazioni successive ma dello stesso livello eseguano la stessa operazione.
					ArrayList<String> modesTemp = new ArrayList<String>(modes);
					
					if(types.get(0).equals("ref")) {
						//unica operazione realmente utile. il nome del riferimento/array serve per la costruzione della stringa del test
						referenceNames.add(names.get(0));
						modesTemp.remove("BodyParameter_ref");
					}else {
						arrayNames.add(names.get(0));
						modesTemp.remove("BodyParameter_array");
					}
					
					types.remove(0);
					formats.remove(0);
					names.remove(0);
					
					exploreInputTree(types, formats, names, modesTemp,referenceNames,arrayNames, responses, ic, uri, method,notValidCount,paramIndex,validBranch);
					
					return;
				}
				
				n = inputClassEnum(names.get(0),types.get(0),formats.get(0), icIteration);
				
				types.remove(0);
				formats.remove(0);
				names.remove(0);
			}
			paramIndex++;
			
			//a questo punto se icIteration.size() < 0 c'è un problema e non ha senso continuare.. probabile che un type non sia listato nell'enum
			if(icIteration.size() > 0) {
				//non sono ad una foglia se qui types.size() >0
				if(types.size() > 0) {
					
					//nel caso quickmode (e quindi pairwise) n=2 cioè input valido/invalido
					for (int i = 0; i < n; i++) {
						
						boolean Skip = false;	//utile solo nel pairwise_mode
						ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
						ArrayList<String> typesTemp = new ArrayList<String>();
						ArrayList<String> formatsTemp = new ArrayList<String>();
						ArrayList<String> namesTemp = new ArrayList<String>();
						
						for(int j=0; j<ic.size();j++) {
							icTemp.add(ic.get(j));
						}
						icTemp.add(icIteration.get(i));
						
						for(int j=0; j<types.size();j++) {
							typesTemp.add(types.get(j));
							formatsTemp.add(formats.get(j));
							namesTemp.add(names.get(j));
						}
						
						//vado a valutare se skippare o no il branch soltanto se sono in pairwisemode e sto valutando un parametro nel body/query
						if(PAIRWISE_MODE && (modes.get(paramIndex-1).contains("BodyParameter")||modes.get(paramIndex-1).contains("QueryParameter"))) {
							if(icIteration.get(i).valid) {
								if(types.size()<notValidCount && !validBranch) {
									Skip = true;
								}
							}else {
								validBranch = false;
								notValidCount--;
								if(notValidCount<0 || types.size()<notValidCount) {
									Skip = true;
								}
							}
						}
						
						//Chiamata ricorsiva
						if(!Skip)
							exploreInputTree(typesTemp, formatsTemp, namesTemp, modes,referenceNames,arrayNames, responses, icTemp, uri, method,notValidCount,paramIndex,validBranch);
					}
					
					
				} else {
					//CREO TF
					
					//devo definire gli ultimi n nodi foglia
					for (int i = 0; i < n; i++) {
						boolean Skip = false;
						ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
						for(int j=0; j<ic.size();j++) {
							icTemp.add(ic.get(j));
						}
						icTemp.add(icIteration.get(i));
						
						//anche per le foglie devo valutare le condizioni in pairwisemode
						if(PAIRWISE_MODE && (modes.get(paramIndex-1).contains("BodyParameter")||modes.get(paramIndex-1).contains("QueryParameter"))) {
							if(icIteration.get(i).valid && notValidCount>0 && !validBranch) {
								Skip = true;
							}else if ((!icIteration.get(i).valid && --notValidCount<0) || (!icIteration.get(i).valid && validBranch)) {
								Skip = true;
							}
						}
						
						if(!Skip) {
							//creo effettivamente i test
							addTestFrames(icTemp,modes,referenceNames,arrayNames,responses,uri, method);
						}
					}
				}
			}else {
				System.out.println("[ERROR] Error in test exploration, this method should not be considered.. ("+uri+" "+ method+")");
			} 
	}
	
	
	//funzione costruzione test in base agli inputclass calcolati
	private void addTestFrames(ArrayList<InputClass> icTemp,ArrayList<String> modes,ArrayList<String> referenceNames,ArrayList<String> arrayNames, ArrayList<Integer> responses,  String uri, String method){
		//System.out.println("[INFO] Aggiungo");
		
		boolean pathP = false;
		boolean queryP = false;
		boolean bodyP = false;
		boolean refP = false;
		boolean arrayP_lv0 = false;
		boolean arrayP_lv1 = false;
		String qry = new String();
		String payload = new String();
		int countRef = 0;
		int countArray = 0;
		
		for(int j=0; j<icTemp.size();j++) {
										
			if(modes.get(j).equals("PathParameter")) {// && !pathP) {
				//qui come in ogni caso se sono il primo parametro dopo i BodyParameter_1 allora devo chiudere le parentesi della reference.
				if(refP) {
					payload += "}";
					refP=false;
				}
				
				if(arrayP_lv0) {
					payload += "]";
					arrayP_lv0=false;
				}else if(arrayP_lv1) {
					payload += "}]";
					arrayP_lv1=false;
				}
					
				pathP=true;
				
				
			} else if(modes.get(j).equals("QueryParameter")) {
				if(refP) {
					payload += "}";
					refP=false;
				}
				if(arrayP_lv0) {
					payload += "]";
					arrayP_lv0=false;
				}else if(arrayP_lv1) {
					payload += "}]";
					arrayP_lv1=false;
				}
				if(!queryP) {
					qry = "?"+icTemp.get(j).name+"={"+icTemp.get(j).name+"}";
				}else {
					qry += "&"+icTemp.get(j).name+"={"+icTemp.get(j).name+"}";
				}
				queryP=true;
				
				
			} else if(modes.get(j).equals("QueryParameter_a")) {
				if(refP) {
					payload += "}";
					refP=false;
				}
				if(arrayP_lv0) {
					payload += "]";
					arrayP_lv0=false;
				}else if(arrayP_lv1) {
					payload += "}]";
					arrayP_lv1=false;
				}
				if(!queryP) {
					qry = "?"+icTemp.get(j).name+"=[{"+icTemp.get(j).name+"}]";
				}else {
					qry += "&"+icTemp.get(j).name+"=[{"+icTemp.get(j).name+"}]";
				}
				queryP=true;
				
				
			} else if(modes.get(j).equals("BodyParameter")) {
				if(refP) {
					payload += "}";
					refP=false;
				}
				if(arrayP_lv0) {
					payload += "]";
					arrayP_lv0=false;
				}else if(arrayP_lv1) {
					payload += "}]";
					arrayP_lv1=false;
				}
				
				if(!bodyP) {
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload = "{\""+icTemp.get(j).name+"\" : {"+icTemp.get(j).name+"}";
					}else {payload = "{\""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";}
				}else {
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload += ", \""+icTemp.get(j).name+"\" : {"+icTemp.get(j).name+"}";
					}else {payload += ", \""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";}
				}
				bodyP=true;
				
				
			} else if(modes.get(j).equals("BodyParameter_r1")) {
				if(arrayP_lv0) {
					payload += "]";
					arrayP_lv0=false;
				}else if(arrayP_lv1) {
					payload += "}]";
					arrayP_lv1=false;
				}
				if(!refP) {
					
					if(!bodyP) {
						payload = "{\""+referenceNames.get(countRef)+"\" : {";
					}else {
						payload += ", \""+referenceNames.get(countRef)+"\" : {";
					}
					countRef++;
					bodyP=true;
					
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload += "\""+icTemp.get(j).name+"\" : {"+icTemp.get(j).name+"}";
					}else {payload += "\""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";}
					
				}else {
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload += ", \""+icTemp.get(j).name+"\" : {"+icTemp.get(j).name+"}";
					}else {payload += ", \""+icTemp.get(j).name+"\" : \"{"+icTemp.get(j).name+"}\"";}
				}
				refP=true;
				
				
			}else if(modes.get(j).contains("BodyParameter_a")) {
				if(refP) {
					payload += "}";
					refP=false;
				}
				
				if(!arrayP_lv0 && !arrayP_lv1) {
					//a1 è un oggetto e richiede le parentesi graffe interne oltre che le quadre come per semplici stringhe o interi.
					if(!bodyP) {
						if(modes.get(j).equals("BodyParameter_a0")) {
							payload = "{\""+arrayNames.get(countArray)+"\" : [";
							arrayP_lv0 = true;
						}else { //BodyParameter_a1
							payload = "{\""+arrayNames.get(countArray)+"\" : [{";
							arrayP_lv1 = true;
						}
					}else {
						if(modes.get(j).equals("BodyParameter_a0")) {
							payload += ", \""+arrayNames.get(countArray)+"\" : [";
							arrayP_lv0 = true;
						} else { //BodyParameter_a1
							payload += ", \""+arrayNames.get(countArray)+"\" : [{";
							arrayP_lv1 = true;
						}
					}
					countArray++;
					bodyP=true;
					
					//dopo aver aperto l'array aggiungo il primo parametro presente in questa iterazione
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload += "{"+icTemp.get(j).name+"}";
					}else {payload += "\"{"+icTemp.get(j).name+"}\"";}
					
				} else {
					
					if(icTemp.get(j).type.contains("int")||icTemp.get(j).type.equals("float")||icTemp.get(j).type.equals("double")) {
						payload += ", {"+icTemp.get(j).name+"}";
					} else {
						payload += ", \"{"+icTemp.get(j).name+"}\"";
					}
				}
				
				
			} else {
				System.out.println("[ERROR] Found mode not implemented.. ("+ modes.get(j) +")");
			}
		}
		
		if(refP) {
			payload += "}";
			refP=false;
		}
		if(arrayP_lv0) {
			payload += "]";
			arrayP_lv0=false;
		}else if(arrayP_lv1) {
			payload += "}]";
			arrayP_lv1=false;
		}
		

		if(pathP && !queryP && !bodyP) {
			testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
		}else if(queryP && !bodyP) {
			testFrames.add(new TestFrame("http://"+host+uri+qry, String.valueOf(offset+count), method, "null", DEBUG_MODE));
		}else if((!pathP && !queryP && bodyP) || (pathP && !queryP && bodyP)) {
			payload += "}";
			testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
			testFrames.get(offset+count).setPayload(payload);
		}else if(!pathP && queryP && bodyP) {
			payload += "}";
			testFrames.add(new TestFrame("http://"+host+uri+qry, String.valueOf(offset+count), method, "null", DEBUG_MODE));
			testFrames.get(offset+count).setPayload(payload);
		}else {
			System.out.println("[ERROR] Undefined, impossible to add Test.. (uri: " + uri + ")");
			return;
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
	
	private void add0ParameterTestFrame(String uri, String method, Operation op) {
		testFrames.add(new TestFrame("http://"+host+uri, String.valueOf(offset+count), method, "null", DEBUG_MODE));
		
		if(op.getResponses().size() != 0) {
		
		if(DEBUG_MODE) 
		System.out.println("[DEBUG] Responses #"+op.getResponses().size()+" : ");
			
		for(Map.Entry<String,Response> resp : op.getResponses().entrySet()) {
			if(DEBUG_MODE) 
	        	System.out.println("[DEBUG] 	"+resp.getKey()+": "+resp.getValue().getDescription());
			testFrames.get(offset+count).expectedResponses.add(Integer.parseInt(resp.getKey()));
			}
		count++;
		countTfFile ++;
		}
	}
	
	
	
	/************************************************************************************************************************************************************************/
	
	
	//-------------------------------------- FUNZIONI BODY PARSING
	 
	private static void parseBody(Swagger swagger, BodyParameter p, ArrayList<String> types, ArrayList<String> names, ArrayList<String> modes,ArrayList<String> formats) {
		if(DEBUG_MODE) {
		System.out.println("[DEBUG] BODY: ");
		//System.out.println("[DEBUG] 	"+p.getName());
		}
		try {
			String ref = p.getSchema().getReference();
			if(ref == null) {
				//System.out.println("[INFO]		Parameter:"+p.getName()+" ref: " + ref);
				//caso in cui non c'è un riferimento ma devo fare parse dello schema
				parseBodyNoRef(swagger, (BodyParameter)p, types, names, modes, formats);
			}else {
			
				RefProperty rp = new RefProperty(ref);

				boolean isRef = parseReference(swagger, rp, types, names, modes,formats,0,false,false);
				
				if(!isRef) {
					System.out.println("[ERROR]	Error while parsing body reference..");
					return;
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("[ERROR]	Error catched while parsing body.. (Parameter: "+p.getName()+")");
		}
	}
	
	
	//FUNZIONE PARSING DI UNA REF (può essere ricorsiva se trova una ref all'interno di una ref)
	//OSS: il ciclo di ricorsione di ferma solo alla prima iterazione (se ho piu ricorsioni non aggiunge niente ai vettori types,names.. utili per l'esplorazione dell'albero)
	private static boolean parseReference(Swagger swagger, RefProperty rp, ArrayList<String> types, ArrayList<String> names, ArrayList<String> modes,ArrayList<String> formats,int iteration,boolean refRec,boolean arrayRec) {
	    
		//System.out.println("[DEBUG]		Parsing reference: " + rp.getSimpleRef());
		
		if((rp.getRefFormat().equals(RefFormat.INTERNAL) && swagger.getDefinitions().containsKey(rp.getSimpleRef()))&&iteration<2) {
			Model m = swagger.getDefinitions().get(rp.getSimpleRef());   
			
			/*if(m instanceof ArrayModel) {
				ArrayModel arrayModel = (ArrayModel)m;
				System.out.println("[WARNING] Array in Reference not tested! no test insert..");
		                
				if(arrayModel.getItems() instanceof RefProperty) {
					RefProperty arrayModelRefProp = (RefProperty)arrayModel.getItems();
					
					parseReference(swagger, arrayModelRefProp, types, names, modes,formats,iteration++);
				}
			}*/

			if(m.getProperties() != null) {
				for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
					
					if (propertyEntry.getValue() instanceof RefProperty) {
						//System.out.println("[INFO]	Found reference in reference..");
						if(DEBUG_MODE) 
							System.out.println("[DEBUG]		" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
						
						modes.add("BodyParameter_ref");
						names.add(propertyEntry.getKey());
						types.add(propertyEntry.getValue().getType());
						formats.add("");
						
						parseReference(swagger, (RefProperty)propertyEntry.getValue(), types, names, modes,formats,++iteration,true, false);
						iteration--;
						
					}else if (propertyEntry.getValue() instanceof ArrayProperty) {
						
						
						Property items =((ArrayProperty) propertyEntry.getValue()).getItems();
						
						if(items instanceof RefProperty) {
							if(DEBUG_MODE) 
								System.out.println("[DEBUG]		" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
							modes.add("BodyParameter_array");
							names.add(propertyEntry.getKey());
							types.add(propertyEntry.getValue().getType());
							formats.add("");
							parseReference(swagger, (RefProperty)items, types, names, modes,formats,++iteration,false,true);
							iteration--;
						}else {
							modes.add("BodyParameter_array");
							names.add(propertyEntry.getKey());
							types.add(propertyEntry.getValue().getType());
							formats.add("");
							
							types.add(items.getType());
							names.add(items.getType());
							formats.add(items.getFormat());
							modes.add("BodyParameter_a1");
							if(DEBUG_MODE) {
								System.out.println("[DEBUG]		" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
								System.out.println("[DEBUG]			" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()+ " Of " + items.getType()+" ("+items.getFormat()+")");
								}

							//System.out.println("[WARNING] Found items in array reference, not yet implemented.. (Ref: "+ rp.getSimpleRef()+")");
						}
						
						
					} else {
					

						if(propertyEntry.getValue().getType().equals("object")) {
							
							if(DEBUG_MODE) { 
								System.out.println("[DEBUG]		No properties for "+propertyEntry.getKey()+", trying with additionalProp..");
								System.out.println("[DEBUG]		" + propertyEntry.getKey() + " : " + ((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getType()+"("+((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getFormat()+")");
								//System.out.println("[DEBUG]		VALUE:" +  ((MapProperty) propertyEntry.getValue()).getAdditionalProperties());
							}
							
							names.add(propertyEntry.getKey());
							types.add(((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getType());
							formats.add(((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getFormat());
						}else {
							
							if(DEBUG_MODE && iteration == 0) {
								System.out.println("[DEBUG]		" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()+"("+propertyEntry.getValue().getFormat()+")");
							}else if(DEBUG_MODE && iteration == 1){
								System.out.println("[DEBUG]			" + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()+"("+propertyEntry.getValue().getFormat()+")");
							}
							
						
							types.add(propertyEntry.getValue().getType());
							formats.add(propertyEntry.getValue().getFormat());
							//System.out.println("[DEBUG]		Adding:" + propertyEntry.getKey() + " Types SIZE: "+ types.size());
							names.add(propertyEntry.getKey());
						}
						
						if(iteration == 0) {
							modes.add("BodyParameter");
						}else if(iteration == 1) {
							if(refRec) {
								modes.add("BodyParameter_r1");
							}else if(arrayRec) {
								modes.add("BodyParameter_a1");
							}
						}else {
							skipMethod = true;
							types.clear();
							names.clear();
							modes.clear();
							formats.clear();
							System.out.println("[ERROR]	Found more than 2 recursive reference, not implemented, skip method.. (ref: "+rp.getSimpleRef()+")");
							return false;
						}
					
					}
				}
				return true;
			}
			System.out.println("[WARNING]	No properties found");
			return true;
			
		} else if(iteration >=2){
			skipMethod = true;
			types.clear();
			names.clear();
			modes.clear();
			formats.clear();
			System.out.println("[WARNING]	Found more than 2 recursive iteration, skip method.. (ref: "+rp.getSimpleRef()+")");
			return false;
		} else {
			System.out.println("[WARNING]	Maybe reference definition missing.. (ref: "+rp.getSimpleRef()+")");
			return false;
		}
	}
	
	
	
	//Funzione che effettua il parsing di uno schema a partire da un bodyparameter e non un riferimento) 
	//anche questa può essere ricorsiva.
	private static void parseBodyNoRef(Swagger swagger, BodyParameter p, ArrayList<String> types, ArrayList<String> names, ArrayList<String> modes,ArrayList<String> formats) {
		
		if(DEBUG_MODE) 
			System.out.println("[DEBUG]		No reference found, trying schema parsing..");
		
		
		Model model = ((BodyParameter) p).getSchema();
		boolean arrayFound = false;
		
		//caso in cui ho un array
		if(model instanceof ArrayModel) {
			arrayFound = true;
			ArrayModel arrayModel = (ArrayModel)model;
			
			Property items = arrayModel.getItems();
			
			modes.add("BodyParameter_array");
			names.add(p.getName());
			types.add(arrayModel.getType());
			formats.add("");
			
			//caso in cui lo schema è definito non in un riferimento ma gli items si
			if(items instanceof RefProperty) {
				RefProperty arrayModelRefProp = (RefProperty)arrayModel.getItems();
				parseReference(swagger, arrayModelRefProp, types, names, modes,formats,1,false,true);
				//System.out.println("[WARNING] Found reference in array schema, not yet implemented..");
			}else {
				//OSS: array può avere un solo item se direttamente specificato(non tramite ref)
				//a differenza del caso precedente poichè non ho un riferimento ho la possibilità di avere solo tipi semplici.
				if(DEBUG_MODE) 
					System.out.println("[DEBUG]   " + p.getName() + " : "+ arrayModel.getType()+ " Of " + items.getType()+" ("+items.getFormat()+")");
				
				names.add(items.getType());
				types.add(items.getType());
				formats.add(items.getFormat());
				modes.add("BodyParameter_a0");
				//System.out.println("[WARNING] Found array schema, not yet implemented..");
			}
			
			//se non è un array ed ha delle proprietà
		}else if(model.getProperties() != null) {
			for (Map.Entry<String, Property> propertyEntry : model.getProperties().entrySet()) {
				if(DEBUG_MODE) 
					System.out.println("[DEBUG]   " + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()+" ("+propertyEntry.getValue().getFormat()+")");
				types.add(propertyEntry.getValue().getType());
				formats.add(propertyEntry.getValue().getFormat());
				names.add(propertyEntry.getKey());
				modes.add("BodyParameter");
			}
		}else if(!arrayFound){
			//caso in cui ho additionalProperties
				//ATTENZIONE: qui potrebbero esserci altre add prop oppure anche un riferimento!
			ModelImpl modelImpl = (ModelImpl) model;
			if(modelImpl.getAdditionalProperties() != null) {
				
				if(DEBUG_MODE) { 
					System.out.println("[DEBUG]		No properties, trying with additionalProp..");
					System.out.println("[DEBUG]   " + p.getName() + " : " + modelImpl.getAdditionalProperties().getType()+" ("+modelImpl.getAdditionalProperties().getFormat()+")");
				}
				names.add(p.getName());
				types.add(modelImpl.getAdditionalProperties().getType());
				modes.add("BodyParameter");
				formats.add(modelImpl.getAdditionalProperties().getFormat());
			}
		}else {
			System.out.println("[ERROR] Error while parsing body schema..");
		}
	}
	
	
	
	
	
	
	
	
	
	
	/************************************************************************************************************************************************************************/

	//FUNZIONE AGGIUNTA IC IN BASE AL TIPO
	private int inputClassEnum(String name, String type, String format, ArrayList<InputClass> ic){
		switch (type) {
		
			//ATT: ci sono alcuni casi non considerati (es. number, boolean)
			case "number":
				//input valido
				if(format != null) {
					if (format.equals("double")) {
						ic.add(new InputClass(name, "double", null, null));
					}else {
						ic.add(new InputClass(name, "float", null, null));
					}
				}else {
					//float di default
					ic.add(new InputClass(name, "float", null, null));
				}
				
				//input non valido
				ic.add(new InputClass(name, "symbol", "null", null));
				
				if(PAIRWISE_MODE) {
					ic.get(ic.size()-1).valid = false;
					ic.get(ic.size()-2).valid = true;
				}
				
				return N_IC_QUICK; //2
				
				
			case "boolean":
				ic.add(new InputClass(name, "b_true", null, null));

				//input non valido
				ic.add(new InputClass(name, "symbol", "null", null));
				
				if(PAIRWISE_MODE) {
					ic.get(ic.size()-1).valid = false;
					ic.get(ic.size()-2).valid = true;
				}
				
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "b_false", null, null));
					return N_IC_BOOLEAN+1;
				}
				
				return N_IC_BOOLEAN;
				
			case "integer":
				
				//input valido
				if(format != null) {
					if (format.equals("int64")) {
						ic.add(new InputClass(name, "int64", null, null));
					}else {
						ic.add(new InputClass(name, "int32", null, null));
					}
				}else {
					ic.add(new InputClass(name, "range", "1", "10"));
				}
				
				//input non valido
				ic.add(new InputClass(name, "symbol", "null", null));
				
				if(PAIRWISE_MODE) {
					ic.get(ic.size()-1).valid = false;
					ic.get(ic.size()-2).valid = true;
				}
				
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "empty", null, null));
					ic.add(new InputClass(name, "greater", "2147483647", null));
					ic.add(new InputClass(name, "lower", "-2147483648", null));
					ic.add(new InputClass(name, "range", "1", "100"));
					ic.add(new InputClass(name, "range", "-100", "-1"));
					ic.add(new InputClass(name, "symbol", "0", null));
					return N_IC_INTEGER+1;
				}
				return N_IC_QUICK;
				
			case "string":
				if(format != null) {
					if (format.equals("date")) {
						ic.add(new InputClass(name, "date", null, null));
					}else if (format.equals("date-time")){
						ic.add(new InputClass(name, "date-time", null, null));
					}
				}else {
					ic.add(new InputClass(name, "s_range", "1", "20"));
				}

				//input non valido
				ic.add(new InputClass(name, "empty", null, null));
				
				if(PAIRWISE_MODE) {
					ic.get(ic.size()-1).valid = false;
					ic.get(ic.size()-2).valid = true;
				}
				
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "symbol", "null", null));
					return N_IC_STRING;
				}
				return N_IC_QUICK;
				
				
			case "language":
				ic.add(new InputClass(name, "lang", null, null));
				
				//input non valido
				ic.add(new InputClass(name, "empty", null, null));
				
				if(PAIRWISE_MODE) {
					ic.get(ic.size()-1).valid = false;
					ic.get(ic.size()-2).valid = true;
				}
				
				if(!QUICK_MODE) {
					ic.add(new InputClass(name, "s_range", "1", "100"));
					return N_IC_LANGUAGE;
				}
				return N_IC_QUICK;
				
			case "s_symbol":
				System.out.println("[INFO] Type \""+type+"\" found");
				//valid?
				ic.add(new InputClass(name, "empty", null, null));
				ic.add(new InputClass(name, "s_range", "1", "100"));
				
				return N_IC_SYMBOL-1;
				
			/*case "ref":
				//System.out.println("[INFO] Type \""+type+"\" found");
				ic.add(new InputClass(name, "ref", null, null));
				return 1;
				*/
			default:
				System.out.println("[ERROR] Type \""+type+"\" not implemented, returning 0 IC..");
				return N_IC_DEFAULT;
		}
	}
	    
	
	
	
	
	
	
	
	
	
	
	
/****************************************************** FUNZIONI AUSILIARIE ************************************************************
*/
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
	
	public void stampaTestFrames() {
		System.out.println();
		System.out.println("\n***********************************************************");
		System.out.println("********************** "+testFrames.size()+" Test Frame **********************\n");
		
		for(int i=0; i<testFrames.size(); i++) {
			System.out.printf("- "+testFrames.get(i).getName()+" | Method:"+ testFrames.get(i).getReqType()+" | #Ic: "+testFrames.get(i).ic.size()+" | Body: "+testFrames.get(i).getPayload()+" | Expected responses: ");
			testFrames.get(i).printExpectedResponses();
			if(PAIRWISE_MODE) {
				System.out.print(" | ");
				testFrames.get(i).printValidCombination();
			}
			System.out.println();
		}
	}
	
	
	public void setQuickMode(boolean quickMode){
		QUICK_MODE = quickMode;
	}
	
	public void setPairWiseMode(boolean pairWiseMode){
		PAIRWISE_MODE = pairWiseMode;
	}
	
	
	
	
}
