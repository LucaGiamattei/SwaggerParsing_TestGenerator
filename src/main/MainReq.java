package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//import dataOperation.OperationalProfileAlterator;
import parser.TestFrameGenerator;
//import selector.AWSSelector;
//import selector.AWSSelectorWR;

public class MainReq {

	private static Scanner scan;

	public static void main(String[] args) {
		scan = new Scanner(System.in);
		String path = "JSONdoc/";
		String adminT = "";
		String userT = "";

		try {
//			1. ACQUISIZIONE DEI TEST FRAMES
			TestFrameGenerator tfg = new TestFrameGenerator();
			
			//tfg.JsonURIParser(path+"test+.json");
			
			
 			tfg.JsonURIParser(path+"ts-admin-basic-info-service.json");


			//tfg.JsonURIParser(path+"ts-admin-order-service.json");
			tfg.JsonURIParser(path+"ts-admin-route-service.json");
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
			
			
			System.out.println("*****************1. Profile initialization*****************");
			//bw.write("Profile initialization\n");
			
			int[] fp = new int[tfg.testFrames.size()];
			
			for(int i=0; i<fp.length; i++){
				fp[i] = 0;
			}
			
			boolean e = false;
			for(int i=0; i<tfg.testFrames.size() ;i++){
				//System.out.println();
				
				//bw.write(1/(double)tfg.testFrames.size()+"@");
				//inizializzazione delle probabilità di fallimento
				for(int j=0; j<1; j++){
					e=tfg.testFrames.get(i).extractAndExecuteTestCase();
					System.out.print("+");
					if(e){
						fp[i]++;
						e = false;
					}
				}
				
				/*
				if(fp[i] == 0){
					bw.write(0+"\n");
				} else if (fp[i] == 5) {
					bw.write(1+"\n");
				} else {
					bw.write((double)fp[i]/5+"\n");
				}
				tfg.testFrames.get(i).setOccurrenceProb(1/(double)tfg.testFrames.size());
				tfg.testFrames.get(i).setFailureProb((double)fp[i]/5);
				*/
			}
			
			System.out.println("\nProbabilities updated");
			System.out.println("*****************END*****************");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}