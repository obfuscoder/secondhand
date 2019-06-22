package de.obfusco.secondhand.payoff;

public class Rounder {
    public static double round(double value, boolean precise, double precision) {
        if (precise) {
            return (double)Math.round(value*100)/100;
        } else {
            return Math.ceil(value / precision) * precision;
        }
    }
}
