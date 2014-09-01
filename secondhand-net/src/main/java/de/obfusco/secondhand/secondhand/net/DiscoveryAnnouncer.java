package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

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
                System.out.println("DISCO - Terminating announcement.");
                System.out.println("DISCO - Sent " + counter + " packets");
                return;
            }
            try {
                sendAnnouncement();
                counter++;
            } catch (IOException ex) {
                System.out.println("DISCO - Send failed!");
                ex.printStackTrace();
            }
            try {
                sleep(5000);
            } catch (InterruptedException ex) {
                System.out.println("DISCO - Interrupted. Exiting.");
                return;
            }
        }
    }

    private void sendAnnouncement() throws IOException {
        byte[] buffer = ("HELLO " + System.currentTimeMillis()).getBytes();
        DatagramPacket datagramPacket;
        datagramPacket = new DatagramPacket(buffer, buffer.length, multicastAddress, socket.getLocalPort());
        socket.send(datagramPacket);
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
