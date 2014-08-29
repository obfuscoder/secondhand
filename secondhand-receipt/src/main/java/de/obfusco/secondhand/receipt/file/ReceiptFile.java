package de.obfusco.secondhand.receipt.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ReceiptFile {

    public static final int EVENT_ID = 1;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    public File createFile(Path basePath, String title, String introText) throws DocumentException, IOException {
        Files.createDirectories(basePath);
        Path fullPath = Paths.get(basePath.toString(), "receipt.pdf");
        Document document = new Document(PageSize.A4, 80, 50, 50, 30);

        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));

        document.open();
        addHeader(document, title);

        List<Reservation> reservations = reservationRepository.findByEvent(eventRepository.findOne(EVENT_ID));

        String[] columnNames = {"ResNr", "Name", "Unterschrift"};
        float[] widths = new float[]{12f, 40f, 40f};
        PdfPTable table = new PdfPTable(columnNames.length);
        table.setWidths(widths);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell headerCell = new PdfPCell(new Phrase(introText,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        headerCell.setBorder(0);
        headerCell.setPaddingBottom(20f);
        headerCell.setColspan(columnNames.length);
        table.addCell(headerCell);

        for (String columnName : columnNames) {
            table.addCell(new PdfPCell(new Phrase(new Chunk(columnName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)))));
        }

        table.setHeaderRows(2);

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
        return fullPath.toFile();

    }

    protected void addHeader(Document document, String title) throws DocumentException {
        document.add(new Phrase(new Chunk(title, FontFactory
                .getFont(FontFactory.HELVETICA_BOLD, 24))));
    }
}
