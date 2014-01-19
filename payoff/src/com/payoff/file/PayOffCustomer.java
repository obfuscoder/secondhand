package com.payoff.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

public class PayOffCustomer {
	
	String path =  "C:\\flohmarkt\\Abrechnung\\";
	String completeBillpath =  "C:\\flohmarkt\\KomplettAbrechnung\\";
	String filename;
	List<Object[]> itemdata;
	float entryfee;
	String customer;
	Object[] customerInfo;
	public PayOffCustomer( String customer, List<Object[]> data, float entryfee, boolean completePayoff, Object[] customerInfo )
	{
		
		if(completePayoff)
		{
			path = completeBillpath;
		}
		this.customerInfo = customerInfo;
		path = path+customer+"\\";
		filename = "total_payoff.pdf";
		itemdata = data;
		this.customer = customer;
		this.entryfee = entryfee;
	}

	
	public void createFile()  throws IOException, DocumentException 
	{
		boolean success = (new File(path)).mkdirs();
		 if (!success) {
			 System.out.println("create Dir failed");
		 }
		 
		 Document document = new Document();// (new Rectangle(340, 842));
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(path + "total_payoff.pdf"));
			document.open();
			
			document.add(new Phrase(new Chunk("Abrechnung Flohmarkt", FontFactory
					.getFont(FontFactory.HELVETICA_BOLD, 24))));
			
			document.add(new Phrase("\n\n"));
			
			if( customerInfo == null )
			{
				document.add(new Phrase("Keine Kundeninformation!"));
			}
			else
			{
				document.add(new Phrase((customerInfo[1]).toString() + "\n"));
				document.add(new Phrase((customerInfo[2]).toString() + "\n"));
				document.add(new Phrase((customerInfo[3]).toString() + "\n\n"));
				document.add(new Phrase("Tel: " + (customerInfo[4]).toString()+ "\n"));
				document.add(new Phrase("Email: " + (customerInfo[5]).toString()));
				
			}
			
			
			document.add(new Phrase("\n\n"));
			
			document.add(new Phrase("Kundennr: " + customer + "\n\n"));
			
			document.add(new Phrase(new Chunk(itemdata.size() + " Artikel wurde(n) verkauft", FontFactory
					.getFont(FontFactory.HELVETICA_BOLD, 18))));
			
			CsvFinder finder = new CsvFinder();
			
			Map<String, Object[]> customerItems = finder.getAllCustomerItems(customer);
			
			float kitasum = (float) 0.0;
			float prise = 0;
			float totalprise = (float) 0.0;
			for( Object[] items : itemdata )
			{
				customerItems.remove(items[0]);
				prise = Float.parseFloat(((String)items[4]).replace(",","."));
				totalprise = totalprise*100 + prise*100;
				totalprise /=100;
			}
			
			kitasum = (float) (totalprise * 0.2);
			
			float totalsum = totalprise*100 - (kitasum*100 + entryfee*100);
			totalsum /=100;
			
			document.add(createItemTable(itemdata, String.format("%.2f", totalprise), String.format("%.2f", kitasum),String.format("%.2f", entryfee),String.format("%.2f", totalsum)));
			
			
			document.add(new Phrase("\n"));
			document.add(new Phrase(new Chunk(customerItems.size() + " Artikel wurde(n) nicht verkauft", FontFactory
					.getFont(FontFactory.HELVETICA_BOLD, 18))));
			
			
			List<Object[]> customerItemList=new ArrayList<>();
			totalprise = (float) 0.0;
			prise = 0;
			for(Object[] item : customerItems.values())
			{
				customerItemList.add(item);
				prise = Float.parseFloat(((String)item[4]).replace(",","."));
				totalprise = totalprise*100 + prise*100;
				totalprise /=100;
			}
			document.add(createItemTable((List<Object[]>) customerItemList, String.format("%.2f", totalprise)));
			
			document.close();
	}
	
	public static PdfPTable createItemTable(List<Object[]> data, String sum) {
		return createItemTable(data, sum, "", "", "");
	}
	public static PdfPTable createItemTable(List<Object[]> data, String sum, String kitasum, String entryfeeString, String totalsumString) {

		PdfPTable table = new PdfPTable(5);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		PdfPCell cell;

		cell = new PdfPCell(new Phrase(new Chunk("Pos", FontFactory.getFont(
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
			cell = new PdfPCell(new Phrase(((String) items[0]).substring(5, 7)));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[0]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[1]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[3]));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase((String) items[4]));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			//new Row
			cell = new PdfPCell(new Phrase((String) items[2]));
			cell.setColspan(5);
			table.addCell(cell);
		}
		

		cell = new PdfPCell(new Phrase("------------------------------------------------------------"));
		cell.setColspan(5);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		cell = new PdfPCell(new Phrase(new Chunk("Summe", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(new Chunk(sum+ " EURO", FontFactory.getFont(
				FontFactory.HELVETICA_BOLD, 12))));
		cell.setColspan(4);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		
		if(kitasum != "")
		{
			cell = new PdfPCell(new Phrase(new Chunk("Teilnahmegebühr", FontFactory.getFont(
					FontFactory.HELVETICA, 12))));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase(new Chunk("- " + entryfeeString + " EURO", FontFactory.getFont(
					FontFactory.HELVETICA, 12))));
			cell.setColspan(4);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase(new Chunk("Erlös Kita (20%)", FontFactory.getFont(
					FontFactory.HELVETICA, 12))));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase(new Chunk("- " + kitasum+ " EURO", FontFactory.getFont(
					FontFactory.HELVETICA, 12))));
			cell.setColspan(4);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase(new Chunk("Gewinn", FontFactory.getFont(
					FontFactory.HELVETICA_BOLD, 12))));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase(new Chunk(totalsumString+ " EURO", FontFactory.getFont(
					FontFactory.HELVETICA_BOLD, 12))));
			cell.setColspan(4);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}
		
		return table;
	}
}
