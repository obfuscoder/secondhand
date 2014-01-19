package com.secondhandcommon.sold.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SoldFileWriter {
	String _path = "C:\\flohmarkt\\Sold\\";
	String _postCodePath = "C:\\flohmarkt\\PostCodes\\";

	public SoldFileWriter() {

	}

	public SoldFileWriter(String path) {
		_path = path;
	}

	public void writeSoldItemsToFile(List<Object> itemNums, int postCode) {
		boolean success;

		File f = new File(_path);
		if (!f.exists()) {
			success = f.mkdirs();
			if (!success) {
				System.out.println("create Sold Dir failed");
			}
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileOutputStream(new File(_path
					+ "sold.csv"), true /* append */));
			for (Object nr : itemNums) {
				writer.println((String) nr);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (postCode == 0) {
			return;
		}

		f = new File(_postCodePath);
		if (!f.exists()) {
			success = f.mkdirs();
			if (!success) {
				System.out.println("create PostCode Dir failed");
			}
		}

		f = new File(_postCodePath + "postCodes.csv");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.out.println("create PostCode File failed");
			}
		}

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(_postCodePath
					+ "postCodes.csv"));

			// einlesen
			ArrayList<String> content = new ArrayList<String>();
			String str = "";
			while ((str = in.readLine()) != null)
				content.add(str);

			// ver�ndern und schreiben
			BufferedWriter out = new BufferedWriter(new FileWriter(
					_postCodePath + "postCodes.csv"));
			boolean found = false;
			for (String line : content) {
				if (line.substring(0, line.indexOf(";")).equals(String.valueOf(postCode))) {
					found = true;
					System.out.println("Counter: " + line.substring(
							line.indexOf(";") +1, line.length()));
					int counter = Integer.parseInt((String) (line.substring(
							line.indexOf(";") +1, line.length())));

					line = line.replace(postCode + ";" + counter,
							postCode + ";" + ++counter);
				}
				out.write(line + "\n");
			}
			if (!found) {
				String line = postCode + ";" + "1" + "\n";
				out.write(line);
			}

			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException ex) {

		}
	}

}
