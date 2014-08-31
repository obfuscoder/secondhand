package de.obfusco.secondhand.secondhand.net;

import java.io.IOException;

public class SecondHandNet {

    public static void main(String[] args) throws InterruptedException {
        try (Discovery discovery = new Discovery(31337)) {
            discovery.start();
            //while (true) {
            Thread.sleep(10000);
            //}
        } catch (IOException ex) {
            System.out.println("Exception in setup");
            ex.printStackTrace();
        }
    }
}
