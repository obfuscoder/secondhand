package com.payoff.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.secondhandcommon.items.reader.CsvFinder;

public class PayOffFile {

	String path = "C:\\flohmarkt\\Abrechnung\\";
	String completeBillPath = "C:\\flohmarkt\\KomplettAbrechnung\\";
	String completionpath = "C:\\flohmarkt\\completion\\";
	String soldpath = "C:\\flohmarkt\\Sold\\";
	String completesoldpath = "C:\\flohmarkt\\CompleteSold\\";
	String soldfile = "sold.csv";
	String completesoldfile = "completesold.csv";
	CsvFinder finder;

	boolean completePayoff = false;
	
	float entryfee = (float) 2.50;

	Map<String, List<Object[]>> customerList = new HashMap<String, List<Object[]>>();

	public PayOffFile(boolean completePayoff) {
		
		this.completePayoff = completePayoff;
		if( completePayoff )
		{
			path = completeBillPath;
			soldpath = completesoldpath;
			soldfile = completesoldfile;
		}
		
		finder = new CsvFinder();
		
		try {
			createFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createFile(List<String> soldList) throws IOException,
			DocumentException {
		
		(new File(path)).mkdirs();


		Document document = new Document();// (new Rectangle(340, 842));
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(path + "total_payoff.pdf"));
		document.open();

		document.add(new Phrase(new Chunk("Abrechnung Flohmarkt ", FontFactory
				.getFont(FontFactory.HELVETICA_BOLD, 24))));


		List<Object> data = new ArrayList<Object>();
		float kitasum = (float) 0.0;
		float prise = 0;
		float totalprise = (float) 0.0;
		float totalentryfee = 0;

		// SubDirs
		File file = new File(completionpath);
		String[] names = file.list();

		for (String name : names) {
			if (new File(completionpath + name).isDirectory()) {
				customerList.put(name, new ArrayList<Object[]>());
			}
		}

		for (String itemNr : soldList) {
			Object[] row = finder.getItemForNr(itemNr);

			String artNr = ((String) row[0]);
			String customernr = artNr.substring(2, 5);
			List<Object[]> customerDataList;

			customerDataList = customerList.get(customernr);

			customerDataList.add(row);
			customerList.put(customernr, customerDataList);
			data.add(row);

			prise = Float.parseFloat(((String) row[4]).replace(",", "."));
			totalprise = totalprise * 100 + prise * 100;
			totalprise /= 100;

		}
		totalentryfee = getCustomerCount() * entryfee;
		kitasum = (float) (totalprise * 0.2);

		float totalsum = totalentryfee * 100 + kitasum * 100;
		totalsum /= 100;

		float totalkitasum = totalsum / 2;

		document.add(createItemTable(data, String.format("%.2f", totalprise),
				String.format("%.2f", kitasum),
				String.format("%.2f", totalentryfee),
				String.format("%.2f", totalsum),
				String.format("%.2f", totalkitasum), getCustomerCount()));

		document.close();
	}

	public List<String> getSoldData() throws IOException {
		List<String> soldList = new ArrayList<>();
		LineNumberReader lr;
		String line;

		if( new File(soldpath + soldfile).exists())
		{
			lr = new LineNumberReader(new FileReader(soldpath + soldfile));

			while ((line = lr.readLine()) != null) {
				soldList.add(line);
			}

			lr.close();

			Collections.sort(soldList);
		}

		return soldList;
	}

	public int getCustomerCount() {
		String num = "0";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(completionpath
					+ "customercount.ct"));

			num = br.readLine();
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Integer.parseInt(num);

	}

	public static PdfPTable createItemTable(List<Object> data, String sum,
			String kitasum, String totalentryfee, String totalsum,
			String totalkitasum, int customerCount) {

		PdfPTable table = new PdfPTable(5);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		PdfPCell cell;

		cell = new PdfPCell(new Phrase(new Chunk("Nr", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk("ArtNr", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk("Kategorie",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk("Gr.", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk("Preis", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		for (int i = 0; i < data.size(); i++) {
			Object[] items = (Object[]) data.get(i);
			table.addCell(Integer.toString(i + 1));
			cell = new PdfPCell(new Phrase((String) items[0]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[1]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[3]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[4]));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			// new Row
			cell = new PdfPCell(new Phrase((String) items[2]));
			cell.setColspan(5);
			table.addCell(cell);
		}

		cell = new PdfPCell(new Phrase(
				"------------------------------------------------------------"));
		cell.setColspan(5);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(new Chunk("Summe verk. Artikel",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(2);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(sum + " EURO",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(3);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(new Chunk("Erlös Kita (20%)",
				FontFactory.getFont(FontFactory.HELVETICA, 12))));
		cell.setColspan(2);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(kitasum + " EURO",
				FontFactory.getFont(FontFactory.HELVETICA, 12))));
		cell.setColspan(3);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(new Chunk("Teilnahmegebühr "
				+ customerCount + " Teiln.", FontFactory.getFont(
				FontFactory.HELVETICA, 12))));
		cell.setColspan(2);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(totalentryfee + " EURO",
				FontFactory.getFont(FontFactory.HELVETICA, 12))));
		cell.setColspan(3);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(new Chunk("Gewinn insgesamt",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(2);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(totalsum + " EURO",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(3);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(new Chunk("Gewinn je Kita",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(2);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(totalkitasum + " EURO",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(3);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		return table;
	}

	public void createFiles() throws IOException, DocumentException {
		List<String> soldList = getSoldData();
		Collections.sort(soldList);
		this.createFile(soldList);

		Map customerMap = finder.getAllCustomer();
		
		
		Iterator it = customerList.entrySet().iterator();
		while (it.hasNext()) {
			
			Map.Entry customer = (Map.Entry) it.next();

			Object[] customerInfo = (Object[]) customerMap.get(Integer.parseInt(customer.getKey().toString()));
					
			PayOffCustomer payOffCustomer = new PayOffCustomer(customer
					.getKey().toString(), (List<Object[]>) customer.getValue(),
					entryfee, completePayoff, customerInfo);
			payOffCustomer.createFile();
		}
	}

	public static void main(String[] args) throws IOException,
			DocumentException {
		PayOffFile payoff = new PayOffFile(false);

		// payoff.createFiles();

	}
}
