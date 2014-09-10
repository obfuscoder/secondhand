package de.obfusco.secondhand.net;

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
    public void messageReceived(Peer peer, String message) {
        System.out.println("MESSAGE from peer " + peer.getAddress() + ": " + message);
    }

    @Override
    public void connected(Peer peer) {
        System.out.println("CONNECTED with peer " + peer.getAddress());
    }

    @Override
    public void disconnected() {
        System.out.println("DISCONNECTED");
    }
}
