package com.totalcalculate.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;

import com.secondhandcommon.constants.Constants;

public class CsvCompletion {

	String path = "C:\\Users\\Anne\\flohmarkt\\completion\\";
	String customerpath = "C:\\Users\\Anne\\flohmarkt\\customer\\";

	public void writeAllDataToOneCsvFile() throws IOException {
		
		boolean success = (new File(path)).mkdirs();
		 if (!success) {
			 System.out.println("create Dir failed");
		 }
		
		File file = new File(path + "completeItemFile.csv");
		 
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileWriter filewriter = new FileWriter(file.getAbsoluteFile(),
				true);
		LineNumberReader lr;
		DecimalFormat df3 = new DecimalFormat("000");
		String line;
		
		filewriter.write(Constants.ITEM_TITLE);
		
		for (int customercount = 1; customercount <= Constants.CUSTOMER_COUNT; customercount++) {
			lr = new LineNumberReader(new FileReader(customerpath + df3.format(customercount)
					+ "\\" + df3.format(customercount) + "_userItems.csv"));

			//1st Line ist needed!
			lr.readLine();
			
			while ((line = lr.readLine()) != null) {
				filewriter.write( System.getProperty("line.separator") + line);
			}
			lr.close();
		}
		filewriter.close();
	}
	
	

	public static void main(String[] args) throws IOException {
		new CsvCompletion().writeAllDataToOneCsvFile();
	}
}
