package de.obfusco.secondhand.net.dto;

import java.util.Date;
import java.util.List;

public class Transaction {
    public enum Type { PURCHASE, REFUND };
    public String id;
    public String zipCode;
    public List<String> itemCodes;
    public Type type;
    public Date date;
}
