package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class TotalPayOff extends BasePayOff {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    public File createTotalPayoffFile(Path basePath, Event event) throws DocumentException, IOException {
        Files.createDirectories(basePath);
        Path fullPath = Paths.get(basePath.toString(), "payoff.pdf");
        Document document = new Document(PageSize.A4, 80, 50, 50, 30);
        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));
        document.open();
        addHeader(document, event);
        document.add(createTotalTable(event));
        if (event.donationOfUnsoldItemsEnabled) {
            document.newPage();
            document.add(createDonatorsTable(event));
        }
        document.close();
        return fullPath.toFile();
    }

    private PdfPTable createDonatorsTable(Event event) {
        List<Donator> donators = new ArrayList<>();
        List<Returner> returns = new ArrayList<>();
        Iterable<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();
        for (Reservation reservation : reservations) {
            List<Item> returnedItems = itemRepository.findByReservationAndSoldNullAndDonationFalseOrderByNumberAsc(reservation);
            List<Item> donatedItems = itemRepository.findByReservationAndSoldNullAndDonationTrueOrderByNumberAsc(reservation);
            if (returnedItems.isEmpty()) {
                donators.add(new Donator(reservation, donatedItems.size()));
            } else {
                returns.add(new Returner(reservation, returnedItems.size()));
            }
        }

        PdfPTable table = new PdfPTable(6);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTotalLine(table, "Komplettspender", Integer.toString(donators.size()), true, 14);
        for (Donator donator : donators) {
            addTotalLine(table, donator.toString(), Integer.toString(donator.count), false, 12);
        }

        addTotalLine(table, "Verkäufer mit zurückzugebenden Artikeln", Integer.toString(returns.size()), true, 14);
        for (Returner returner : returns) {
            addTotalLine(table, returner.toString(), Integer.toString(returner.count), false, 12);
        }

        return table;
    }

    private PdfPTable createTotalTable(Event event) {
        boolean considerSellerFee = JOptionPane.showConfirmDialog(
                null, "Soll vom Auszahlbetrag die Reservierungsgebühr abgezogen werden?", "Reservierungsgebühr",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;

        long reservationCount = reservationRepository.count();
        Iterable<Reservation> reservations = reservationRepository.findAll();

        PdfPTable table = new PdfPTable(6);
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
            double commissionCutSum = sum * (1 - reservation.commissionRate.doubleValue());

            commissionCutSum = Math.floor(commissionCutSum / pricePrecision) * pricePrecision;
            commissionSum += sum - commissionCutSum;
            feeSum += sellerFee;
            if (considerSellerFee) {
                commissionCutSum -= sellerFee;
            }
            payouts.add(new Payout(reservation, commissionCutSum));
        }

        int soldItemCount = itemRepository.findBySoldNotNull().size();
        addTotalLine(table, "Anzahl verkaufter Artikel", Integer.toString(soldItemCount), true, 12);
        addTotalLine(table, "Summe verkaufter Artikel", currency.format(soldSum), true, 12);
        addTotalLine(table, "Kommissionsanteil", currency.format(commissionSum), false, 12);
        addTotalLine(table, "Reservierungsgebühren für " + reservationCount + " Reservierungen", currency.format(feeSum), false, 12);
        addTotalLine(table, "Gewinn insgesamt", currency.format(commissionSum + feeSum), true, 14);

        addTotalLine(table, "Auszahlbeträge", "", true, 14);
        Map<Integer, Integer> totalCoins = new HashMap<>();
        for(Payout payout : payouts) {
            addTotalLine(table, payout.toString(), currency.format(payout.value), false, 12);
            addPayoutCoinsToTotal(totalCoins, payout);
        }
        Payout totalPayout = new Payout();
        totalPayout.coins = totalCoins;
        addTotalLine(table, totalPayout.coinString(), "", true, 14);
        return table;
    }

    private void addPayoutCoinsToTotal(Map<Integer, Integer> totalCoins, Payout payout) {
        for (Map.Entry<Integer, Integer> entry : payout.coins.entrySet()) {
            Integer count = totalCoins.get(entry.getKey());
            if (count == null) count = 0;
            count += entry.getValue();
            totalCoins.put(entry.getKey(), count);
        }
    }

    private class ReservationReference {
        public Reservation reservation;

        public ReservationReference(Reservation reservation) {
            this.reservation = reservation;
        }

        private ReservationReference() {
        }

        public String toString() {
            return String.format("%d - %s", reservation.number, reservation.seller.getName());
        }
    }

    private class ReservationReferenceWithCount extends ReservationReference {
        int count;

        public ReservationReferenceWithCount(Reservation reservation, int count) {
            super(reservation);
            this.count = count;
        }
    }

    private class Payout extends ReservationReference {
        public double value;
        public Map<Integer, Integer> coins;
        public Payout(Reservation reservation, double value) {
            super(reservation);
            this.value = value;
            this.coins = calculateCoins();
        }

        public Payout() {
        }

        private Map<Integer, Integer> calculateCoins() {
            int cents = (int) (value * 100);
            Map<Integer, Integer> coinCounts = new HashMap<>();
            int[] coins = { 50000, 20000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1 };
            for(int coin : coins) {
                if (coin > cents) continue;
                int coinCount = cents / coin;
                coinCounts.put(coin, coinCount);
                cents -= coin * coinCount;
            }
            return coinCounts;
        }

        public String toString() {
            return String.format("%s (%s)", super.toString(), coinString());
        }

        public String coinString() {
            Integer[] keys = coins.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer key : keys) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(String.format("%dx%s", coins.get(key), currency.format((double) key / 100)));
            }
            return sb.toString();
        }
    }

    private class Returner extends ReservationReferenceWithCount {
        public Returner(Reservation reservation, int itemCount) {
            super(reservation, itemCount);
        }
    }

    private class Donator extends ReservationReferenceWithCount {
        public Donator(Reservation reservation, int count) {
            super(reservation, count);
        }
    }
}
