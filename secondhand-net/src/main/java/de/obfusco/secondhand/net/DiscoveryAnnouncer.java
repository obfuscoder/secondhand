package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Random;

class DiscoveryAnnouncer extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryAnnouncer.class);

    private volatile MulticastSocket socket;
    private InetAddress multicastAddress;

    public DiscoveryAnnouncer(MulticastSocket socket, InetAddress multicastAddress) throws SocketException {
        this.socket = socket;
        this.multicastAddress = multicastAddress;
    }

    @Override
    public void run() {
        Random random = new Random();
        int counter = 0;
        while (true) {
            if (socket == null) {
                LOG.info("Terminating announcement. Sent " + counter + " packets");
                return;
            }
            try {
                sendAnnouncement();
                counter++;
            } catch (IOException ex) {
                LOG.error("Failed to send announcement", ex);
            }
            try {
                sleep(random.nextInt(10000)+8000);
            } catch (InterruptedException ex) {
                LOG.warn("Interrupted", ex);
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
