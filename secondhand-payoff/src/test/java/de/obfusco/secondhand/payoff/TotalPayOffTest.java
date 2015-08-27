package de.obfusco.secondhand.payoff;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TotalPayOffTest {
    @Test
    public void calculateCoinsTest() {
        double value = 47.83;
        Map<Integer, Integer> coins = calculateCoins((int)(value*100));
        assertEquals(2, coins.get(2000).intValue());
        assertEquals(1, coins.get(500).intValue());
        assertEquals(1, coins.get(200).intValue());
        assertEquals(1, coins.get(50).intValue());
        assertEquals(1, coins.get(20).intValue());
        assertEquals(1, coins.get(10).intValue());
        assertEquals(1, coins.get(2).intValue());
        assertEquals(1, coins.get(1).intValue());
    }

    private Map<Integer, Integer> calculateCoins(int cents) {
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
}