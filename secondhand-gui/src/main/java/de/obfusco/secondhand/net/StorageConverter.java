package de.obfusco.secondhand.net;

import de.obfusco.secondhand.net.dto.Category;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.net.dto.Item;
import de.obfusco.secondhand.net.dto.Reservation;
import de.obfusco.secondhand.net.dto.Seller;
import de.obfusco.secondhand.net.dto.Transaction;
import de.obfusco.secondhand.storage.repository.*;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class StorageConverter {
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
    TransactionRepository transactionRepository;

    public void storeEvent(Event event) {
        transactionRepository.deleteAll();
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
        mapTransaction(mapperFactory);
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

    private void mapTransaction(MapperFactory mapperFactory) {
        mapperFactory.classMap(Transaction.class, de.obfusco.secondhand.storage.model.Transaction.class)
                .field("type", "type")
                .field("id", "id")
                .customize(
                        new CustomMapper<Transaction, de.obfusco.secondhand.storage.model.Transaction>() {
                            @Override
                            public void mapAtoB(Transaction a, de.obfusco.secondhand.storage.model.Transaction b, MappingContext context) {
                                b.created = a.date;
                                b.items = new ArrayList<>();
                                for (de.obfusco.secondhand.storage.model.Item item : itemRepository.findAll(a.items)) {
                                    b.items.add(item);
                                }
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Transaction b, Transaction a, MappingContext context) {
                                a.date = b.created;
                                a.items = new ArrayList<>();
                                for (de.obfusco.secondhand.storage.model.Item item : b.items) {
                                    a.items.add(item.id);
                                }
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

    public Event convertToEvent() {
        MapperFactory mapperFactory = createMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        Event event = mapper.map(eventRepository.find(), Event.class);
        event.categories = new ArrayList<>();
        for (de.obfusco.secondhand.storage.model.Category category : categoryRepository.findAll()) {
            event.categories.add(mapper.map(category, Category.class));
        }
        event.sellers = new ArrayList<>();
        for (de.obfusco.secondhand.storage.model.Seller seller : sellerRepository.findAll()) {
            event.sellers.add(mapper.map(seller, Seller.class));
        }
        event.reservations = new ArrayList<>();
        for (de.obfusco.secondhand.storage.model.Reservation reservation : reservationRepository.findAll()) {
            event.reservations.add(mapper.map(reservation, Reservation.class));
        }
        event.items = new ArrayList<>();
        for (de.obfusco.secondhand.storage.model.Item item : itemRepository.findAll()) {
            event.items.add(mapper.map(item, Item.class));
        }
        return event;
    }

    public List<Transaction> convertToTransactions(Iterable<de.obfusco.secondhand.storage.model.Transaction> transactions) {
        MapperFactory mapperFactory = createMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        return mapper.mapAsList(transactions, Transaction.class);
    }
}