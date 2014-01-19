package com.secondhandcommon.csvread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.log.SysoCounter;

public class CsvReader {

	static Map<String, Object[]> data = new HashMap<String, Object[]>();
	static Map<Integer, Object[]> customerdata = new HashMap<Integer, Object[]>();
	
	static List<String[]> postCodeData = new ArrayList<String[]>();
	Map<String, Object[]> user = new HashMap<String, Object[]>();
	String csvItemsFolder = "C:\\flohmarkt\\completion\\";
	String csvItemFile = csvItemsFolder + "completeItemFile.csv";
	String csvPostCodeFile = "C:\\flohmarkt\\PostCodes\\postCodes.csv";
	String csvCustomerItemFile = "items.csv";
	String customerfile = "C:\\flohmarkt\\Customer\\customer.csv";
	

	public CsvReader( boolean read ) {
		
		if(read)
		{
			readItems();
			readPostCodes();
		}
	}

	public CsvReader(String file) {
		csvItemFile = file;
		readItems();
	}

	public void readItems() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(csvItemFile), "UTF-8"));
			try {
				line = br.readLine();
			} catch (NullPointerException e) {
				System.out.println("Read Line failed: " + e);
			}
			while ((line = br.readLine()) != null) {

				// use comma as separator
				Object[] itemInfo = line.split(cvsSplitBy);
				if (itemInfo.length > 1) {
					data.put((String) itemInfo[0], itemInfo);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void readCustomer() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(customerfile), "UTF-8"));
			try {
				line = br.readLine();
			} catch (NullPointerException e) {
				System.out.println("Read Line failed: " + e);
			}
			while ((line = br.readLine()) != null) {

				// use comma as separator
				Object[] customerInfo = line.split(cvsSplitBy);
				if (customerInfo.length > 1) {
					customerdata.put( Integer.parseInt((String)customerInfo[0]), customerInfo);
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("File \"" + customerfile + "\" existiert nicht.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Map<String, Object[]> readcustomerItems( String customer) {

		Map<String, Object[]> customerdata = new HashMap<String, Object[]>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(csvItemsFolder + customer + "\\" + csvCustomerItemFile), "UTF-8"));

			while ((line = br.readLine()) != null) {

				// use comma as separator
				Object[] itemInfo = line.split(cvsSplitBy);
				if (itemInfo.length > 1) {
					customerdata.put((String) itemInfo[0], itemInfo);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return customerdata;
	}
	
	public void saveItemsToCustomerFolder()
	{
		int customercount = 0;
		Iterator it = data.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        //ArtNr
	        String customerNr = ((String) pair.getKey()).substring(2, 5);
	
	        	(new File(csvItemsFolder + customerNr)).mkdirs();
	        	try {
					File file = new File(csvItemsFolder + customerNr + "\\items.csv");
					if (!file.exists()) {
						file.createNewFile();
						customercount++;
					}
					
					FileWriter fw = new FileWriter(file.getAbsoluteFile(), true );
					BufferedWriter bw = new BufferedWriter(fw);
					Object[] item = (Object[]) pair.getValue();
					int j = 0;
					for( int i = 0; i < item.length-1; i++ )
					{
						bw.write(item[i] + ";");
						j=i;
					}
					bw.write(item[++j] + "\r\n");
					bw.close();
					fw.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    File file = new File(csvItemsFolder + "\\customercount.ct");
	    
	    FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(String.valueOf(customercount));
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readPostCodes() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(csvPostCodeFile), "UTF-8"));

			while ((line = br.readLine()) != null) {

				String[] postcode_data = line.split(cvsSplitBy);
				postCodeData.add(postcode_data);
			}

		} catch (FileNotFoundException e) {
			System.out.println("File \"" + csvPostCodeFile + "\" existiert nicht.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void readUserInfo() {

	}

	public static Object[] getItem(String itemnr) {
		return data.get(itemnr);
	}

	public static Map getAllItems() {
		return data;
	}
	
	public Map getAllCustomer() {
		this.readCustomer();
		return customerdata;
	}
	
	public static List<String[]> getAllPostCodes() {
		return postCodeData;
	}
	
	public Map getAllCustomerItems( String customer) {
		return readcustomerItems( customer );
	}
}
