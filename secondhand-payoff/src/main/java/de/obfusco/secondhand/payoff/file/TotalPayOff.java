package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.payoff.Rounder;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.StockItem;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import de.obfusco.secondhand.storage.repository.StockItemRepository;
import de.obfusco.secondhand.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class TotalPayOff extends BasePayOff {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    StockItemRepository stockItemRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    public File createTotalPayoffFile(Path basePath, Event event) throws DocumentException, IOException {
        Files.createDirectories(basePath);
        Path fullPath = Paths.get(basePath.toString(), "payoff.pdf");
        Document document = new Document(PageSize.A4, 50, 50, 50, 30);
        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));
        document.open();
        addHeader(document, event);
        document.add(createTotalTable(event));
        document.newPage();
        document.add(createSoldStockItemsTable());
        if (event.donationOfUnsoldItemsEnabled) {
            document.newPage();
            document.add(createDonatorsTable());
        }
        if (event.gates) {
            document.newPage();
            document.add(createGatesTable());
            document.newPage();
            document.add(createMissingItemsTable());
        }
        document.close();
        return fullPath.toFile();
    }

    private PdfPTable createSoldStockItemsTable() {
        Stream<StockItem> stream = StreamSupport.stream(stockItemRepository.findAll().spliterator(), false);
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        Long soldStockItems = stockItemRepository.countOfSoldItems();
        if (soldStockItems == null) soldStockItems = 0L;
        addTotalLine(table, "verkaufte Stammartikel", true, 14, null, String.valueOf(soldStockItems));
        stream.filter(it -> it.getSold() > 0).forEach(
                it -> addTotalLine(table, it.description, false, 12, null, String.valueOf(it.getSold()))
        );
        return table;
    }

    private PdfPTable createDonatorsTable() {
        Event event = eventRepository.find();
        List<Donator> donators = new ArrayList<>();
        List<Returner> returns = new ArrayList<>();
        Iterable<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();
        for (Reservation reservation : reservations) {
            List<Item> returnedItems = event.gates ?
                    itemRepository.findByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseOrderByNumberAsc(reservation) :
                    itemRepository.findByReservationAndSoldNullAndDonationFalseOrderByNumberAsc(reservation);
            List<Item> donatedItems = event.gates ?
                    itemRepository.findByReservationAndCheckedInNotNullAndSoldNullAndDonationTrueOrderByNumberAsc(reservation) :
                    itemRepository.findByReservationAndSoldNullAndDonationTrueOrderByNumberAsc(reservation);
            if (returnedItems.isEmpty()) {
                donators.add(new Donator(reservation, donatedItems.size()));
            } else {
                returns.add(new Returner(reservation, returnedItems.size()));
            }
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTotalLine(table, "Komplettspender", true, 14, null, Integer.toString(donators.size()));
        for (Donator donator : donators) {
            addTotalLine(table, donator.toString(), false, 12, null, Integer.toString(donator.count));
        }

        addTotalLine(table, "Verkäufer mit zurückzugebenden Artikeln", true, 14, null, Integer.toString(returns.size()));
        for (Returner returner : returns) {
            addTotalLine(table, returner.toString(), false, 12, null, Integer.toString(returner.count));
        }

        return table;
    }

    private PdfPTable createTotalTable(Event event) {
        long reservationCount = reservationRepository.count();
        Iterable<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        double pricePrecision = event.pricePrecision.doubleValue();

        double commissionSum = 0.0;
        double feeSum = 0.0;
        double soldSum = 0.0;
        List<Payout> payouts = new ArrayList<>();
        for (Reservation reservation : reservations) {
            double sellerFee = reservation.fee.doubleValue();
            double sum = 0;
            for (Item item : itemRepository.findByReservationAndSoldNotNullOrderByNumberAsc(reservation)) {
                sum += item.price.doubleValue();
            }
            soldSum += sum;
            double commissionCut = Rounder.round(sum * reservation.commissionRate.doubleValue(), event.preciseBillAmounts, pricePrecision);
            double commissionCutSum = sum - commissionCut;
            commissionSum += commissionCut;
            feeSum += sellerFee;
            if (event.incorporateReservationFee()) {
                commissionCutSum -= sellerFee;
            }
            payouts.add(new Payout(reservation, commissionCutSum));
        }

        long soldItemCount = itemRepository.countBySoldNotNull();
        Long soldStockItemCount = stockItemRepository.countOfSoldItems();
        if (soldStockItemCount == null) soldStockItemCount = 0L;
        double soldStockItemSum = storageService.sumOfSoldStockItems();
        addTotalLine(table, "Anzahl verkaufter Stammartikel", true, 12, null, Long.toString(soldStockItemCount));
        addTotalLine(table, "Summe verkaufter Stammartikel", true, 12, null, currency.format(soldStockItemSum));
        addTotalLine(table, "Anzahl verkaufter Artikel", true, 12, null, Long.toString(soldItemCount));
        addTotalLine(table, "Summe verkaufter Artikel", true, 12, null, currency.format(soldSum));
        addTotalLine(table, "Kommissionsanteil", false, 12, null, currency.format(commissionSum));
        addTotalLine(table, "Reservierungsgebühren für " + reservationCount + " Reservierungen", false, 12, null, currency.format(feeSum));
        addTotalLine(table, "Gewinn insgesamt", true, 14, null, currency.format(commissionSum + feeSum + soldStockItemSum));

        addTotalLine(table, "Auszahlbeträge", true, 14, null, "");
        Map<Integer, Long> totalCoins = new HashMap<>();
        for (Payout payout : payouts) {
            addTotalLine(table, payout.toString(), false, 12, null, currency.format(payout.value));
            addPayoutCoinsToTotal(totalCoins, payout);
        }
        Payout totalPayout = new Payout();
        totalPayout.coins = totalCoins;
        addTotalLine(table, totalPayout.coinString(), true, 14, null, "");
        return table;
    }

    private void addPayoutCoinsToTotal(Map<Integer, Long> totalCoins, Payout payout) {
        for (Map.Entry<Integer, Long> entry : payout.coins.entrySet()) {
            Long count = totalCoins.get(entry.getKey());
            if (count == null) count = 0L;
            count += entry.getValue();
            totalCoins.put(entry.getKey(), count);
        }
    }

    private static class ReservationReference {
        Reservation reservation;

        ReservationReference(Reservation reservation) {
            this.reservation = reservation;
        }

        private ReservationReference() {
        }

        public String toString() {
            return reservation.toString();
        }
    }

    private static class ReservationWithCount extends ReservationReference {
        int count;

        ReservationWithCount(Reservation reservation, int count) {
            super(reservation);
            this.count = count;
        }
    }

    private static class Payout extends ReservationReference {
        double value;
        Map<Integer, Long> coins;

        Payout(Reservation reservation, double value) {
            super(reservation);
            this.value = value;
            this.coins = calculateCoins();
        }

        Payout() {
        }

        private Map<Integer, Long> calculateCoins() {
            long cents = Math.round(value * 100.0);
            Map<Integer, Long> coinCounts = new HashMap<>();
            int[] coins = {50000, 20000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1};
            for (int coin : coins) {
                if (coin > cents) continue;
                long coinCount = cents / coin;
                coinCounts.put(coin, coinCount);
                cents -= coin * coinCount;
            }
            return coinCounts;
        }

        public String toString() {
            return String.format("%s (%s)", super.toString(), coinString());
        }

        String coinString() {
            Integer[] keys = coins.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer key : keys) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(String.format("%dx%s", coins.get(key), coinValueString(key)));
            }
            return sb.toString();
        }

        String coinValueString(long value) {
            if (value < 100) {
                return String.format("%d¢", value);
            } else {
                return String.format("%d€", value/100);
            }
        }
    }

    private static class Returner extends ReservationWithCount {
        Returner(Reservation reservation, int itemCount) {
            super(reservation, itemCount);
        }
    }

    private static class Donator extends ReservationWithCount {
        Donator(Reservation reservation, int count) {
            super(reservation, count);
        }
    }

    private PdfPTable createGatesTable() {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setHeaderRows(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        List<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();
        addTotalLine(table, "Check In/Out", true, 14, null);
        addTotalLine(table, "Reservierung", true, 10, null, "Gesamt", "Checkin", "Verkauf", "Spende", "Checkout", "fehlen");
        long totalItemCount = 0; 
        long totalCheckinCount = 0; 
        long totalSoldCount = 0;
        long totalDonationCount = 0; 
        long totalCheckoutCount = 0;
        long totalMissingCount = 0;
        for (Reservation reservation : reservations) {
            long itemCount = itemRepository.countByReservation(reservation);
            long checkinCount = itemRepository.countByReservationAndCheckedInNotNull(reservation);
            long soldCount = itemRepository.countByReservationAndSoldNotNull(reservation);
            long donationCount = itemRepository.countByReservationAndCheckedInNotNullAndSoldNullAndDonationTrue(reservation);
            long checkoutCount = itemRepository.countByReservationAndCheckedOutNotNull(reservation);
            long missingCount = itemRepository.countByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseAndCheckedOutNull(reservation);
            totalItemCount += itemCount;
            totalCheckinCount += checkinCount;
            totalCheckoutCount += checkoutCount;
            totalSoldCount += soldCount;
            totalDonationCount += donationCount;
            totalMissingCount += missingCount;
            addTotalLine(table, reservation.toString(), missingCount > 0, 12,
                    null, Long.toString(itemCount), Long.toString(checkinCount), Long.toString(soldCount),
                    Long.toString(donationCount), Long.toString(checkoutCount), Long.toString(missingCount));
        }
        addTotalLine(table, "Summe", true, 14,
                null, Long.toString(totalItemCount), Long.toString(totalCheckinCount), Long.toString(totalSoldCount),
                Long.toString(totalDonationCount), Long.toString(totalCheckoutCount), Long.toString(totalMissingCount));

        return table;
    }

    private PdfPTable createMissingItemsTable() throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        int[] alignments = { Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_RIGHT };
        table.setWidthPercentage(100f);
        table.setHeaderRows(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidths(new int[]{2, 3, 5, 2, 1});
        addTotalLine(table, "Vermisste Artikel", true, 14, null);
        addTotalLine(table, "Nummer", true, 10, alignments, "Kategorie", "Beschreibung", "Größe", "Preis");
        List<Item> items = itemRepository.findByCheckedInNotNullAndSoldNullAndDonationFalseAndCheckedOutNullOrderByNumberAsc();
        for (Item item: items) {
            addTotalLine(table, String.format("%d - %d", item.reservation.number, item.number), false, 10,
                    alignments,
                    item.category.name,
                    item.description,
                    item.size == null ? "" : item.size,
                    currency.format(item.price));
        }
        return table;
    }
}
