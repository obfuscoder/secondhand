package de.obfusco.secondhand.net;

import de.obfusco.secondhand.net.dto.Category;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.net.dto.Item;
import de.obfusco.secondhand.net.dto.Reservation;
import de.obfusco.secondhand.net.dto.Seller;
import de.obfusco.secondhand.storage.repository.*;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStorageConverter {
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

    public void storeEvent(Event event) {
        itemRepository.deleteAll();
        reservationRepository.deleteAll();
        sellerRepository.deleteAll();
        categoryRepository.deleteAll();
        eventRepository.deleteAll();
        MapperFactory mapperFactory = createMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        eventRepository.save(mapper.map(event, de.obfusco.secondhand.storage.model.Event.class));
        storeCategories(event.categories, mapper);
        storeSellers(event.sellers, mapper);
        storeReservations(event.reservations, mapper);
        storeItems(event.items, mapper);
    }

    private MapperFactory createMapperFactory() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapReservation(mapperFactory);
        mapItem(mapperFactory);
        return mapperFactory;
    }

    private void mapItem(MapperFactory mapperFactory) {
        mapperFactory.classMap(Item.class, de.obfusco.secondhand.storage.model.Item.class)
                .byDefault()
                .customize(
                        new CustomMapper<Item, de.obfusco.secondhand.storage.model.Item>() {
                            @Override
                            public void mapAtoB(Item a, de.obfusco.secondhand.storage.model.Item b, MappingContext context) {
                                b.category = categoryRepository.findOne(a.categoryId);
                                b.reservation = reservationRepository.findOne(a.reservationId);
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Item b, Item a, MappingContext context) {
                                a.categoryId = b.category.id;
                                a.reservationId = b.reservation.id;
                            }
                        })
                .register();
    }

    private void mapReservation(MapperFactory mapperFactory) {
        mapperFactory.classMap(Reservation.class, de.obfusco.secondhand.storage.model.Reservation.class)
                .byDefault()
                .customize(
                        new CustomMapper<Reservation, de.obfusco.secondhand.storage.model.Reservation>() {
                            @Override
                            public void mapAtoB(Reservation a, de.obfusco.secondhand.storage.model.Reservation b, MappingContext context) {
                                b.seller = sellerRepository.findOne(a.sellerId);
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Reservation b, Reservation a, MappingContext context) {
                                a.sellerId = b.seller.id;
                            }
                        })
                .register();
    }

    private void storeCategories(List<Category> categories, MapperFacade mapper) {
        for (Category category : categories) {
            categoryRepository.save(mapper.map(category, de.obfusco.secondhand.storage.model.Category.class));
        }
    }

    private void storeSellers(List<Seller> sellers, MapperFacade mapper) {
        for (Seller seller : sellers) {
            sellerRepository.save(mapper.map(seller, de.obfusco.secondhand.storage.model.Seller.class));
        }
    }

    private void storeReservations(List<Reservation> reservations, MapperFacade mapper) {
        for (Reservation reservation : reservations) {
            reservationRepository.save(mapper.map(reservation, de.obfusco.secondhand.storage.model.Reservation.class));
        }
    }

    private void storeItems(List<Item> items, MapperFacade mapper) {
        for (Item item : items) {
            itemRepository.save(mapper.map(item, de.obfusco.secondhand.storage.model.Item.class));
        }
    }

}
