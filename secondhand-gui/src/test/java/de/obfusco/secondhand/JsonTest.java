package de.obfusco.secondhand;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.obfusco.secondhand.dto.*;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

public class JsonTest {

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

    private void parseAndCheckInputStream(InputStream fileStream) throws ParseException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
        Event event = gson.fromJson(new InputStreamReader(fileStream), Event.class);
        checkEvent(event);
        checkCategories(event.categories);
        checkReservations(event.reservations);
    }

    private void checkReservations(List<Reservation> reservations) throws ParseException {
        assertEquals(110, reservations.size());
        Reservation firstReservation = reservations.get(0);
        assertEquals(313, firstReservation.id);
        assertEquals(33, firstReservation.number);
        checkSeller(firstReservation.seller);
        checkItems(firstReservation.items);
    }

    private void checkItems(List<Item> items) throws ParseException {
        assertEquals(50, items.size());
        Item item = items.get(0);
        assertEquals(4683, item.id);
        assertEquals(8, item.categoryId);
        assertEquals("s. Oliver pink", item.description);
        assertEquals("116/122", item.size);
        assertEquals(new BigDecimal("4.0"), item.price);
        assertEquals(1, item.number);
        assertEquals("04033015", item.code);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX").parse("2015-02-07 13:15:12+01"), item.sold);
        assertEquals(false, item.donation);
    }

    private void checkSeller(Seller seller) {
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
        assertEquals("Frühlingsflohmarkt \"Rund ums Kind\"", event.name);
        assertEquals(new BigDecimal("0.1"), event.pricePrecision);
        assertEquals(new BigDecimal("0.2"), event.commissionRate);
        assertEquals(new BigDecimal("2.0"), event.sellerFee);
        assertEquals(false, event.donationOfUnsoldItemsEnabled);
    }
}