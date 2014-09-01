package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Discovery implements Closeable {

    private DiscoveryListener discoveryListener;
    private DiscoveryAnnouncer discoveryAnnouncer;
    private MulticastSocket socket;
    private InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");

    public Discovery(int port, DiscoveryObserver observer) throws IOException {
        socket = new MulticastSocket(port);
        socket.setBroadcast(true);
        socket.joinGroup(multicastAddress);
        discoveryListener = new DiscoveryListener(socket, observer);
        discoveryAnnouncer = new DiscoveryAnnouncer(socket, multicastAddress);
    }

    public void start() {
        discoveryListener.start();
        discoveryAnnouncer.start();
    }

    @Override
    public void close() throws IOException {
        log("Closing discovery.");
        if (discoveryListener != null) {
            discoveryListener.close();
        }
        if (discoveryAnnouncer != null) {
            discoveryAnnouncer.close();
        }
    }

    private void log(String message) {
        System.out.println("DISCO - " + message);
    }
}
