package dataOperation;

import dataStructure.TestFrame;

public class TestFrameDistance {
	
	public TestFrameDistance() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
/*	metodo che calcolca la distanza tra due Test Frame ta e tb:
	in ingresso è richiesto un parametro intero fk, il quale consente di utilizzare una diversa f(k)
	per il calcolo della distanza.
	Nel caso corrente è possibile utilizzare una sola opzione, quindi il parametro non è significativo.
	Il parametro maxdistance definisce la distanza massima che è possibile definire tra due TF.
*/	
	public double getTestDistance(TestFrame ta, TestFrame tb, int maxdistance){
		int k = this.getDistance(ta, tb, maxdistance);
		
// 		System.out.println("[DEBUG] k = "+k);
//		System.out.print(k+" ");
		
		double distance=0;
		
		if(k == 0 || k == -1 || k == -2){
//			System.out.println("[DEBUG] Ramo 1");
			distance = 0;
		} else {
//			System.out.println("[DEBUG] Ramo 2");
			distance = ta.getFailureProb() * (1/(double)(k))*tb.getFailureProb(); 
		}
		
		return distance;
	
	}
	
	public double getPperFTestDistance(TestFrame ta, TestFrame tb, int maxdistance){
		int k = this.getDistance(ta, tb, maxdistance);
		
// 		System.out.println("[DEBUG] k = "+k);
//		System.out.print(k+" ");
		
		double distance=0;
		
		if(k == 0 || k == -1 || k == -2){
//			System.out.println("[DEBUG] Ramo 1");
			distance = 0;
		} else {
//			System.out.println("[DEBUG] Ramo 2");
			distance = ta.getOccurrenceProb()*ta.getFailureProb() * (1/(double)(k))*tb.getOccurrenceProb()*tb.getFailureProb(); 
		}
		
		return distance;
	
	}

//	Calcolo dell'effettiva distanza tra due TF come la differenza delle classi di input.
//	Tale distanza è considerata nulla sia in caso di autoanelli, sia in caso di TF che non hanno legami:
//	- diversa URI;
//	Se hanno la stessa URI, ma payloaad diverso si considera una distanza massima imposta dall'utente.
//	Hp: i test frames relativi alla stessa URI sono della stessa lunghezza (tutte le istanze delle input class hanno la stessa lunghezza)
	private int getDistance(TestFrame a, TestFrame b, int max){
		int k=0;
		
		if(a.getName().equals(b.getName())){
			
			if(a.getPayload().equals(b.getPayload())){
				//stesso test frame
				return 0;
			} else if(a.getPayload().length() == b.getPayload().length()){
				if(a.ic.size() == b.ic.size()){
					for(int i=0; i<a.ic.size(); i++){
						if(!a.ic.get(i).name.equals(b.ic.get(i).name)){
							k++;
						}
					}
					//stesso test frame, ma con ic diverse
					return k;
				} else {
					//stesso nome del metodo, ma diverso numero di input class (caso molto remoto)
					return max;
				}
			} else{
				//stesso nome del metodo ma diverso payload
				return max;
			}
			
		} else if(a.getName().length() == b.getName().length()){
			
			if(a.getPayload().length() == b.getPayload().length()){
				if(a.ic.size() == b.ic.size()){
					for(int i=0; i<a.ic.size(); i++){
						if(!a.ic.get(i).name.equals(b.ic.get(i).name)){
							k++;
						}
					}	
					//stesso test frame, ma input class differenti
					return k;
				} else {
					//piccola probabilità che si tratti dello stesso metodo, ma diverso numero di input class (caso molto remoto)
					return max;
				}
			} else {
				//stesso metodo, ma diverse input class e diverso payload
				return max;
			}
			
		} else {
			//non esiste un link
			return 0;
		}
		
		
		
//		char carattere = a.charAt(0);
//		int i = 0, k = 0;
//		
//		//controllo che il nome del metodo sia lo stesso.
//		while((i < a.length()-1) && (carattere!='(')){
//			
//			if(carattere != b.charAt(i)){
//				return -2;
//			}
//			
//			i++;
//			carattere = a.charAt(i);
//		}
//		
//		while(i < a.length()-1){
//			
//			if(carattere != b.charAt(i)){
//				k++;
//			}
//			
//			i++;
//			carattere = a.charAt(i);
//		}
//		
//		return k;
	}

}
