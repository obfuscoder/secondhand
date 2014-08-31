package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Collections;

class DiscoveryAnnouncer extends Thread implements Closeable {

    private volatile MulticastSocket socket;
    private InetAddress multicastAddress;

    public DiscoveryAnnouncer(MulticastSocket socket, InetAddress multicastAddress) throws SocketException {
        this.socket = socket;
        this.multicastAddress = multicastAddress;
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            if (socket == null) {
                System.out.println("Terminating announcing.");
                System.out.println("Sent " + counter + " packets");
                return;
            }
            try {
                sendAnnouncement();
                counter++;
            } catch (IOException ex) {
                System.out.println("Send failed!");
                ex.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Interrupted. Exiting.");
                return;
            }
        }
    }

    private void sendAnnouncement() throws IOException {
        byte[] buffer = "HELLO".getBytes();
        DatagramPacket datagramPacket;
        datagramPacket = new DatagramPacket(buffer, buffer.length, multicastAddress, socket.getLocalPort());
        socket.send(datagramPacket);
        System.out.println("Sent announcement");
    }

    @Override
    public void close() throws IOException {
        socket = null;
    }
}
