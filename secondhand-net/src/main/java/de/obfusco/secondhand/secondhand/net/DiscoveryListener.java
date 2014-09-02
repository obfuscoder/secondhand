package de.obfusco.secondhand.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

class DiscoveryListener extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryListener.class);

    private volatile MulticastSocket socket;
    private DiscoveryObserver observer;

    public DiscoveryListener(MulticastSocket socket, DiscoveryObserver observer) throws SocketException {
        this.socket = socket;
        this.observer = observer;
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            if (socket == null) {
                LOG.info("Terminating listener. Received " + counter + " packets");
                return;
            }
            try {
                byte[] buffer = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);
                counter++;
                observer.peerDiscovered(datagramPacket.getAddress().getHostAddress());
            } catch (IOException ex) {
                LOG.error("Receive failed", ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
