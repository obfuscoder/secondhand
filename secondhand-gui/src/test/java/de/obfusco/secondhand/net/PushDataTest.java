package de.obfusco.secondhand.net;

import de.obfusco.secondhand.gui.MainConfiguration;
import de.obfusco.secondhand.net.dto.Category;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.storage.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MainConfiguration.class)
public class PushDataTest {
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
    public void convertToEvent() {
        Event event = storageConverter.convertToEvent();
        assertEquals(4, event.id);
        assertEquals(categoryRepository.count(), event.categories.size());
        assertEquals(1, event.categories.get(0).id);
        assertEquals(sellerRepository.count(), event.sellers.size());
        assertEquals(53, event.sellers.get(0).id);
        assertEquals(reservationRepository.count(), event.reservations.size());
        assertEquals(281, event.reservations.get(0).id);
        assertEquals(itemRepository.count(), event.items.size());
        assertEquals(731, event.items.get(0).id);
    }

    @Test
    public void eventToJson() {
        Event event = new Event();
        event.id = 2;
        event.name = "Eventname";
        event.pricePrecision = BigDecimal.valueOf(0.5);
        event.donationOfUnsoldItemsEnabled = true;
        event.categories = new ArrayList<>();
        Category category = new Category();
        category.id = 3;
        category.name = "Categoryname";
        event.categories.add(category);
        JsonEventConverter converter = new JsonEventConverter();
        String json = converter.toJson(event);
        assertEquals("{\"id\":2,\"name\":\"Eventname\",\"price_precision\":0.5,\"donation_of_unsold_items_enabled\":true," +
                "\"categories\":[{\"id\":3,\"name\":\"Categoryname\"}]}", json);
    }

    @Test
    public void jsonToEvent() {
        String json = "{\"id\":2,\"name\":\"Eventname\",\"price_precision\":0.5,\"donation_of_unsold_items_enabled\":true," +
                "\"categories\":[{\"id\":3,\"name\":\"Categoryname\"}]}";
        JsonEventConverter converter = new JsonEventConverter();
        Event event = converter.parse(json);
        assertEquals(2, event.id);
        assertEquals("Eventname", event.name);
        assertEquals(BigDecimal.valueOf(0.5), event.pricePrecision);
        assertEquals(true, event.donationOfUnsoldItemsEnabled);
        assertEquals(3, event.categories.get(0).id);
        assertEquals("Categoryname", event.categories.get(0).name);
    }
}