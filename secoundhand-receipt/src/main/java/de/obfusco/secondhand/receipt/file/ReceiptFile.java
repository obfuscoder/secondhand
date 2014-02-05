package de.obfusco.secondhand.receipt.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;

@Component
public class ReceiptFile {

	public static final int EVENT_ID = 1;

	@Autowired
	ReservationRepository reservationRepository;

	@Autowired
	EventRepository eventRepository;

	private String path = "C:\\flohmarkt\\Annahme\\";

	public File createFile() throws FileNotFoundException, DocumentException {
		String fullPath = path + "receipt.pdf";

		File directory = new File(path);

		if (!directory.exists()) {
			directory.mkdir();
		}

		Document document = new Document(PageSize.A4,80, 50, 50, 30);

		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(fullPath));

		document.open();
		addHeader(document);
		document.add(new Phrase("\n\n"));
		document.add(new Phrase(
				"Mit meiner Unterschrift bestaetige ich die Teilnahmebedingungen."));

		List<Reservation> reservations = reservationRepository
				.findByEvent(eventRepository.findOne(EVENT_ID));

		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(100f);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		String[] columnNames = { "ResNr", "Name", "Unterschrift" };
		float[] widths = new float[] { 12f, 40f, 40f };

		table.setWidths(widths);

		for (String columnName : columnNames) {
			table.addCell(new PdfPCell(new Phrase(new Chunk(columnName,
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)))));
		}

		for (final Reservation reservation : reservations) {
			PdfPCell cell = new PdfPCell(new Phrase(new Chunk(reservation.getNumber()
					.toString(),
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14))));
			table.addCell(cell);
			cell = new PdfPCell(new Phrase(
					reservation.getSeller().getLastName()
					+ ", "
					+ reservation.getSeller().getFirstName()));
			table.addCell(cell);
			cell = new PdfPCell();
			table.addCell(cell);
		}

		document.add(table);
		document.close();
		return new File(fullPath);

	}

	protected void addHeader(Document document) throws DocumentException {
		document.add(new Phrase(new Chunk("Annahme Flohmarkt", FontFactory
				.getFont(FontFactory.HELVETICA_BOLD, 24))));
	}
}
