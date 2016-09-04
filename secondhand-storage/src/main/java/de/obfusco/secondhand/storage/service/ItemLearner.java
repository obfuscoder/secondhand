package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.Item;

public interface ItemLearner {
    Item learn(String code);
}
