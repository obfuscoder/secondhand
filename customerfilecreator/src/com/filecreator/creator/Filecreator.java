package com.filecreator.creator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

public class Filecreator {

	String path = "C:\\Users\\Anne\\flohmarkt\\customer\\";
	private final int customerSize = 40;
	private final int marketCounter = 10;
	private final int allowedItems = 50;
	
	public void createFoldersForCustomer()
	{
		 boolean success = (new File(path)).mkdirs();
		 if (!success) {
			 System.out.println("create Dir failed");
		 }
		 
		 for( int i=1; i<= customerSize; i++ )
		 {
			 String dirName;
			 DecimalFormat df =   new DecimalFormat  ( "000" );
			 dirName = df.format(i);
			 
			 success = (new File(path + dirName)).mkdirs();

			 if (!success) {
				 System.out.println("create SubDir failed");
				 continue;
			 }
			 
			 createCsvFiles(i,df);
		 }
	}
	
	public void createCsvFiles(int customer, DecimalFormat df)
	{
		String itemfile =path + df.format(customer) + "\\"+ df.format(customer) + "_userItems.csv";
		String customerfile =path + df.format(customer) + "\\"+ df.format(customer) + "_customer.csv";
		DecimalFormat df2 = new DecimalFormat  ( "00" );
				
		PrintWriter writer;
		try {
			writer = new PrintWriter(itemfile , "UTF-8");
			writer.println("Artnr;Kategorie;Bezeichnung;Groesse;Preis");
			
			String itemnr;
			Long itemLong;
			int checksum;
			for(int i = 1; i <= allowedItems; i++ )
			{
				checksum = 0;
				
				itemnr = df2.format(marketCounter) + df.format(customer) + df2.format(i);
				itemLong = Long.parseLong(itemnr);
				while( itemLong!=0)
				{
					checksum += itemLong%10;
					itemLong /= 10;
				}
				itemnr += Integer.toString(checksum%10);
				
//				writer.println(itemnr + ";;;;");
				//TEST
				writer.println(itemnr + ";Baby" + ";bla" + itemnr + ";" + i + ";" + String.format("%.2f", ((float)i/2)));
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			writer = new PrintWriter(customerfile , "UTF-8");
			writer.println("Nr;Vorname;Name;Str.;HausNr;PLZ;Ort;Tel;Tel2(Handy);Email;Bemerkung");
			writer.println(df.format(customer) +";;;;;;;;;;");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}
	
	
	public static void main(String[] args) 
	{
		new Filecreator().createFoldersForCustomer();
	}
}
