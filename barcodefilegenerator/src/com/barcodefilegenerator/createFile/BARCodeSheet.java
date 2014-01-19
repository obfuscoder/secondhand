package com.barcodefilegenerator.createFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.secondhandcommon.constants.Constants;
import com.secondhandcommon.items.reader.CsvFinder;

public class BARCodeSheet {

	/** The resulting PDF. */
	private static final String RESULT = "C:\\flohmarkt\\result_o_b.pdf";
	private static final String resultpath = "C:\\flohmarkt\\completion\\";


	@SuppressWarnings("unchecked")
	public BARCodeSheet() {
		
		
	}

	public String createPdf(String path, int customer) throws IOException,
			DocumentException {

		
		Document document = new Document(PageSize.A4, 0, 0, 0, 0);
		String filename = path + "result_o_b.pdf";
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(filename));
		document.open();

		int size = items.size();
		int modItems = size % 4;
		int tablelinecounter;
		tablelinecounter = size/4 ;
		if (modItems > 0) {
			tablelinecounter++;
		}
		for (int j = 1; j <= tablelinecounter; j++) {
			int tablecounter = 4;
			if (j == tablelinecounter && modItems > 0) {
				tablecounter = modItems;
			}

			createTableLine(document, writer, tablecounter, customer);
			
//			if(j%14 == 0)
//				document.newPage();
		}

		document.close();
		
		return filename;

	}


	private void createTableLine(Document document, PdfWriter writer, int count, int customer)
			throws DocumentException {
		
		PdfPTable table = new PdfPTable(4); // 3 columns.
		table.setSpacingBefore(0);
		table.setSpacingAfter(0);
		table.setWidthPercentage(100);

		PdfPCell cell1 = new PdfPCell(new Paragraph(""));
		PdfPCell cell2 = new PdfPCell(new Paragraph(""));
		PdfPCell cell3 = new PdfPCell(new Paragraph(""));
		PdfPCell cell4 = new PdfPCell(new Paragraph(""));

		cell1.setBorderColor(BaseColor.WHITE);
		cell2.setBorderColor(BaseColor.WHITE);
		cell3.setBorderColor(BaseColor.WHITE);
		cell4.setBorderColor(BaseColor.WHITE);
		
		cell1.addElement(createTable(writer, document, customer));
		if (count > 1)
			cell2.addElement(createTable(writer, document, customer));
		if (count > 2)
			cell3.addElement(createTable(writer, document, customer));
		if (count > 3)
			cell4.addElement(createTable(writer, document, customer));

		table.addCell(cell1);
		table.addCell(cell2);
		table.addCell(cell3);
		table.addCell(cell4);

		document.add(table);

	}

	private Element createTable(PdfWriter writer, Document document, int customer)
			throws DocumentException {

		PdfContentByte cb = writer.getDirectContent();

		Object[] itemdata = (Object[]) items.get(itemkeys.get(itemCounter));
		
		// EAN 8
		Barcode128 codeEAN = new Barcode128();
		// document.add(new Paragraph("Barcode EAN.UCC-8"));
		codeEAN.setCodeType(Barcode.CODE128);
//		codeEAN.setBarHeight(codeEAN.getSize() * 3f);
		codeEAN.setCode((String) itemdata[0]);

		PdfPTable table = new PdfPTable(6);

//		table.setSpacingBefore(10f);

		table.setTotalWidth(100);
		table.setLockedWidth(true);

		PdfPCell cell;
		String nr = (String) itemdata[0];
		cell = new PdfPCell(new Phrase((new Chunk(nr.substring(5, 7), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)))));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBorderColor(BaseColor.WHITE);
		table.addCell(cell);
		Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
		barcode.scalePercent(120f);
		cell = new PdfPCell(barcode);
		cell.setColspan(5);
		cell.setBorderColor(BaseColor.WHITE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(6f);
//		cell.setPaddingLeft(50);
		table.addCell(cell);
		
		table.setSpacingAfter(4f);

		itemCounter++;

		return table;

		// add barcode to document
		// document.add(codeEAN.createImageWithBarcode(cb, null, null));
	}

	int itemCounter = 0;
	Map items = new HashMap<>();
	List itemkeys;
	public void createPDFFiles() throws IOException, DocumentException
	{
		
		for( int customercount = 1; customercount < Constants.CUSTOMER_COUNT; customercount++)
		{
			CsvFinder finder;
			
			String customer = new DecimalFormat( "000" ).format(customercount);
			String path = resultpath + customer + "\\";
			finder = new CsvFinder( path + customer + "_userItems.csv" );
			if(!items.isEmpty()) items.clear();
			items = finder.getAllItems();
			itemkeys = new ArrayList<Object[]>(items.keySet());
			itemCounter = 0;
			Collections.sort(itemkeys);
		
			createPdf(path, customercount);
		}
	}
	
	public String createPDFFile( int customernr) throws IOException, DocumentException
	{
			CsvFinder finder;
			
			String customer = new DecimalFormat( "000" ).format(customernr);
			String path = resultpath + customer + "\\";
			finder = new CsvFinder( path + "items.csv" );
			if(!items.isEmpty()) items.clear();
			items = finder.getAllCustomerItems(customer);
			itemkeys = new ArrayList<Object[]>(items.keySet());
			itemCounter = 0;
			Collections.sort(itemkeys);
		
			String filename = createPdf(path, customernr);
			
			return filename;
	}
}

