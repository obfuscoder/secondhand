package de.obfusco.secondhand.secondhand.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SecondHandNet implements MessageBroker {

    public static void main(String[] args) throws IOException {

        try (Network network = new Network(31337, new SecondHandNet())) {
            network.start();
            BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String line = buffer.readLine();
                if (line == null || line.isEmpty()) break;
                network.send(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String message(String requestMessage) {
        System.out.println("REQUEST from peer: " + requestMessage);
        return requestMessage;
    }
}
