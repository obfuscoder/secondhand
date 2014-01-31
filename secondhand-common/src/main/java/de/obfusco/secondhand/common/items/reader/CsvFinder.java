package de.obfusco.secondhand.common.items.reader;

import java.util.List;
import java.util.Map;

import de.obfusco.secondhand.common.csvread.CsvReader;

public class CsvFinder {

    String _file = "C:\\flohmarkt\\completion\\completeItemFile.csv";

    ;
	public CsvFinder() {
    }

    public CsvFinder(String file) {
        _file = file;
    }

    public Object[] getItemForNr(String itemNr) {
        return new CsvReader(_file).getItem(itemNr);
    }

    public Map getAllItems() {
        return new CsvReader(_file).getAllItems();
    }

    public static List<String[]> getAllPostCodes() {
        return new CsvReader(true).getAllPostCodes();
    }

    public static Map getAllCustomerItems(String customer) {
        return new CsvReader(false).readcustomerItems(customer);
    }

    public static Map getAllCustomer() {
        return new CsvReader(false).getAllCustomer();
    }
}
