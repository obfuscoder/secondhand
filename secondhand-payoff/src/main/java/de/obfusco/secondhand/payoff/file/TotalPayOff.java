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
        document.close();
        return fullPath.toFile();
    }

    private PdfPTable createTotalTable(Event event) {
        long reservationCount = reservationRepository.count();
        Iterable<Reservation> reservations = reservationRepository.findAll();

        PdfPTable table = new PdfPTable(6);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        double pricePrecision = event.pricePrecision.doubleValue();
        double sellerFee = event.sellerFee.doubleValue();

        double commissionSum = 0.0;
        double feeSum = 0.0;
        double soldSum = 0.0;
        List<Payout> payouts = new ArrayList<>();
        for (Reservation reservation : reservations) {
            double sum = 0;
            for (Item item : itemRepository.findByReservationAndSoldNotNullOrderByNumberAsc(reservation)) {
                sum += item.price.doubleValue();
            }
            soldSum += sum;
            double commissionCutSum = sum * (1 - event.commissionRate.doubleValue());

            commissionCutSum = Math.floor(commissionCutSum / pricePrecision) * pricePrecision;
            commissionSum += sum - commissionCutSum;
            feeSum += sellerFee;
            payouts.add(new Payout(reservation, commissionCutSum - sellerFee));
        }

        int soldItemCount = itemRepository.findBySoldNotNull().size();
        addTotalLine(table, "Anzahl verkaufter Artikel", Integer.toString(soldItemCount), true, 12);
        addTotalLine(table, "Summe verkaufter Artikel", currency.format(soldSum), true, 12);
        addTotalLine(table, "Kommissionsanteil (" + percent.format(event.commissionRate) + ")", currency.format(commissionSum), false, 12);
        addTotalLine(table, "Teilnahmegebühren für " + reservationCount + " Teilnehmer", currency.format(feeSum), false, 12);
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

    private class Payout {
        public Reservation reservation;
        public double value;
        public Map<Integer, Integer> coins;
        public Payout() {}
        public Payout(Reservation reservation, double value) {
            this.reservation = reservation;
            this.value = value;
            this.coins = calculateCoins();
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
            return String.format("%d - %s (%s)", reservation.number, reservation.seller.getName(), coinString());
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
}
