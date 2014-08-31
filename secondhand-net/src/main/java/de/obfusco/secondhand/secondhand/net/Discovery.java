package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Discovery implements Closeable {

    private DiscoveryListener discoveryListener;
    private DiscoveryAnnouncer discoveryAnnouncer;
    private MulticastSocket socket;
    private InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");

    public Discovery(int port) throws IOException {
        socket = new MulticastSocket(port);
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);
        socket.joinGroup(multicastAddress);
        discoveryListener = new DiscoveryListener(socket);
        discoveryAnnouncer = new DiscoveryAnnouncer(socket, multicastAddress);
    }

    public void start() {
        discoveryListener.start();
        discoveryAnnouncer.start();
    }

    @Override
    public void close() throws IOException {
        if (discoveryListener != null) {
            discoveryListener.close();
        }
        if (discoveryAnnouncer != null) {
            discoveryAnnouncer.close();
        }
    }
}
