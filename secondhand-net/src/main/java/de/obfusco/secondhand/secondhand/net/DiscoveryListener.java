package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

class DiscoveryListener extends Thread implements Closeable {

    private volatile MulticastSocket socket;

    public DiscoveryListener(MulticastSocket socket) throws SocketException {
        this.socket = socket;
    }

    @Override
    public void run() {
        int counter = 0;
        byte[] buffer = new byte[4096];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        while (true) {
            if (socket == null) {
                System.out.println("Terminating listener.");
                System.out.println("Received " + counter + " packets");
                return;
            }
            try {
                System.out.println("Waiting for incoming data ...");
                socket.receive(datagramPacket);
                counter++;
                System.out.println("Received " + new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + " from " + datagramPacket.getAddress().getHostAddress());
            } catch (SocketTimeoutException ex) {
                // expected to occur after 1 sec of waiting for packets
            } catch (IOException ex) {
                System.out.println("receive failed!");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket = null;
    }
}
