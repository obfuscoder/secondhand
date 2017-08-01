package de.obfusco.secondhand.net;

import de.obfusco.secondhand.gui.MainConfiguration;
import de.obfusco.secondhand.net.dto.*;
import de.obfusco.secondhand.storage.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MainConfiguration.class)
public class EventImporterTest {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    StorageConverter storageConverter;

    @Test
    public void importJson() {
        Event event = parse(getClass().getResourceAsStream("/test.json"));
        storageConverter.storeEvent(event);
        de.obfusco.secondhand.storage.model.Event dataEvent = eventRepository.find();
        assertEquals(4, dataEvent.getId().intValue());
        assertEquals(event.categories.size(), categoryRepository.count());
        assertEquals(event.categories.get(0).name, categoryRepository.findAll().iterator().next().name);
        assertEquals(event.sellers.size(), sellerRepository.count());
        assertEquals(event.sellers.get(0).id, sellerRepository.findAll().iterator().next().getId().intValue());
        assertEquals(event.reservations.size(), reservationRepository.count());
        de.obfusco.secondhand.storage.model.Reservation firstReservation =
                reservationRepository.findByNumber(1);
        assertEquals(1, firstReservation.number);
        assertEquals(265, firstReservation.seller.getId().intValue());
        assertEquals(event.items.size(), itemRepository.count());
        de.obfusco.secondhand.storage.model.Item firstItem = itemRepository.findAll().iterator().next();
        assertEquals(731, firstItem.getId().intValue());
        assertEquals(2, firstItem.getCategory().getId().intValue());
        assertEquals(301, firstItem.getReservation().getId().intValue());
    }

    @Test
    public void parseCompressedJson() throws IOException, ParseException {
        InputStream fileStream = getClass().getResourceAsStream("/test.json.gz");
        GZIPInputStream unzippedStream = new GZIPInputStream(fileStream);
        parseAndCheckInputStream(unzippedStream);
    }

    @Test
    public void parseJson() throws FileNotFoundException, ParseException {
        InputStream fileStream = getClass().getResourceAsStream("/test.json");
        parseAndCheckInputStream(fileStream);
    }

    private void parseAndCheckInputStream(InputStream inputStream) throws ParseException {
        Event event = parse(inputStream);
        checkEvent(event);
        checkCategories(event.categories);
        checkReservations(event.reservations);
        checkSellers(event.sellers);
        checkItems(event.items);
    }

    private Event parse(InputStream inputStream) {
        return new JsonDataConverter().parse(inputStream);
    }

    private void checkReservations(List<Reservation> reservations) throws ParseException {
        assertEquals(110, reservations.size());
        Reservation firstReservation = reservations.get(0);
        assertEquals(313, firstReservation.id);
        assertEquals(33, firstReservation.number);
        assertEquals(new BigDecimal("5.0"), firstReservation.fee);
        assertEquals(new BigDecimal("0.3"), firstReservation.commissionRate);
    }

    private void checkItems(List<Item> items) throws ParseException {
        assertEquals(5060, items.size());
        Item item = items.get(0);
        assertEquals(8, item.categoryId);
        assertEquals("s. Oliver pink", item.description);
        assertEquals("116/122", item.size);
        assertEquals(new BigDecimal("4.0"), item.price);
        assertEquals(1, item.number);
        assertEquals("04033015", item.code);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX").parse("2015-02-07 13:15:12+01"), item.sold);
        assertEquals(false, item.donation);
    }

    private void checkSellers(List<Seller> sellers) {
        assertEquals(110, sellers.size());
        Seller seller = sellers.get(0);
        assertEquals(53, seller.id);
        assertEquals("Stefanie", seller.firstName);
        assertEquals("Goppelsröder", seller.lastName);
        assertEquals("Veilchenweg 2", seller.street);
        assertEquals("75203", seller.zipCode);
        assertEquals("Königsbach-Stein", seller.city);
        assertEquals("stephanie.goppelsroeder@web.de", seller.email);
        assertEquals("07232 3177592", seller.phone);
    }

    private void checkCategories(List<Category> categories) {
        assertEquals(33, categories.size());
        Category firstCategory = categories.get(0);
        assertEquals(1, firstCategory.id);
        assertEquals("Schuhe", firstCategory.name);
    }

    private void checkEvent(Event event) {
        assertEquals(4, event.id);
        assertEquals("Frühlingsflohmarkt \"Rund ums Kind\" 2014", event.name);
        assertEquals(new BigDecimal("0.1"), event.pricePrecision);
        assertEquals(new BigDecimal("0.2"), event.commissionRate);
        assertEquals(new BigDecimal("2.0"), event.sellerFee);
        assertEquals(false, event.donationOfUnsoldItemsEnabled);
    }
}