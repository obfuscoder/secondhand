package de.obfusco.secondhand.net;

import de.obfusco.secondhand.net.dto.*;
import de.obfusco.secondhand.storage.repository.*;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StorageConverter {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    StockItemRepository stockItemRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    TransactionRepository transactionRepository;

    private Map<Integer, de.obfusco.secondhand.storage.model.Category> categoryMap;
    private Map<Integer, de.obfusco.secondhand.storage.model.Seller> sellerMap;
    private Map<Integer, de.obfusco.secondhand.storage.model.Reservation> reservationMap;

    public void storeEvent(Event event) {
        initHashMaps();
        emptyDatabase();
        MapperFactory mapperFactory = createMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        eventRepository.save(mapper.map(event, de.obfusco.secondhand.storage.model.Event.class));
        storeCategories(event.categories, mapper);
        storeSellers(event.sellers, mapper);
        storeReservations(event.reservations, mapper);
        storeItems(event.items, mapper);
        storeStockItems(event.stockItems, mapper);
    }

    private void initHashMaps() {
        categoryMap = new HashMap<>();
        sellerMap = new HashMap<>();
        reservationMap = new HashMap<>();
    }

    private void emptyDatabase() {
        transactionRepository.deleteAll();
        itemRepository.deleteAll();
        reservationRepository.deleteAll();
        sellerRepository.deleteAll();
        categoryRepository.deleteAll();
        eventRepository.deleteAll();
        stockItemRepository.deleteAll();
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
                .exclude("gender")
                .byDefault()
                .customize(
                        new CustomMapper<Item, de.obfusco.secondhand.storage.model.Item>() {
                            @Override
                            public void mapAtoB(Item a, de.obfusco.secondhand.storage.model.Item b, MappingContext context) {
                                b.setCategory(categoryMap.get(a.categoryId));
                                b.setReservation(reservationMap.get(a.reservationId));
                                b.setSize(a.size);
                                if (a.gender != null) {
                                    b.setGender(de.obfusco.secondhand.storage.model.Item.Gender.valueOf(a.gender.toUpperCase()));
                                }
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Item b, Item a, MappingContext context) {
                                a.categoryId = b.getCategory().getId();
                                a.reservationId = b.getReservation().getId();
                                a.size = b.getSize();
                                if (b.getGender() != null) {
                                    a.gender = b.getGender().toString().toLowerCase();
                                }
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
                                b.seller = sellerMap.get(a.sellerId);
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Reservation b, Reservation a, MappingContext context) {
                                a.sellerId = b.seller.getId();
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
                                b.setItems(itemRepository.findByCodeIn(a.items));
                                b.setStockItems(stockItemRepository.findByCodeIn(a.items));
                            }

                            @Override
                            public void mapBtoA(de.obfusco.secondhand.storage.model.Transaction b, Transaction a, MappingContext context) {
                                a.date = b.created;
                                a.items = b.getAllItemCodes();
                            }
                        })
                .register();
    }

    private void storeCategories(List<Category> categories, MapperFacade mapper) {
        for (Category category : categories) {
            categoryMap.put(category.id, categoryRepository.save(mapper.map(category, de.obfusco.secondhand.storage.model.Category.class)));
        }
    }

    private void storeSellers(List<Seller> sellers, MapperFacade mapper) {
        for (Seller seller : sellers) {
            sellerMap.put(seller.id, sellerRepository.save(mapper.map(seller, de.obfusco.secondhand.storage.model.Seller.class)));
        }
    }

    private void storeReservations(List<Reservation> reservations, MapperFacade mapper) {
        for (Reservation reservation : reservations) {
            reservationMap.put(reservation.id, reservationRepository.save(mapper.map(reservation, de.obfusco.secondhand.storage.model.Reservation.class)));
        }
    }

    private void storeItems(List<Item> items, MapperFacade mapper) {
        for (Item item : items) {
            itemRepository.save(mapper.map(item, de.obfusco.secondhand.storage.model.Item.class));
        }
    }

    private void storeStockItems(List<StockItem> stockItems, MapperFacade mapper) {
        for (StockItem stockItem : stockItems) {
            stockItemRepository.save(mapper.map(stockItem, de.obfusco.secondhand.storage.model.StockItem.class));
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
        event.stockItems = new ArrayList<>();
        for (de.obfusco.secondhand.storage.model.StockItem stockItem : stockItemRepository.findAll()) {
            event.stockItems.add(mapper.map(stockItem, StockItem.class));
        }
        return event;
    }

    public List<Transaction> convertToTransactions(Iterable<de.obfusco.secondhand.storage.model.Transaction> transactions) {
        MapperFactory mapperFactory = createMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        return mapper.mapAsList(transactions, Transaction.class);
    }
}
