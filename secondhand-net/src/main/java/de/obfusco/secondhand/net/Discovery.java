package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Discovery implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(Discovery.class);

    private DiscoveryListener discoveryListener;
    private DiscoveryAnnouncer discoveryAnnouncer;
    private MulticastSocket socket;
    private InetAddress multicastAddress = InetAddress.getByName("239.42.13.37");

    public Discovery(int port, DiscoveryObserver observer, String name, NetworkInterface networkInterface) throws IOException {
        socket = new MulticastSocket(port);
        socket.setNetworkInterface(networkInterface);
        socket.setBroadcast(true);
        socket.joinGroup(multicastAddress);
        discoveryListener = new DiscoveryListener(socket, observer);
        discoveryAnnouncer = new DiscoveryAnnouncer(socket, multicastAddress, name);
    }

    public void start() {
        LOG.info("Starting network discovery");
        discoveryListener.start();
        discoveryAnnouncer.start();
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing network discovery");
        if (discoveryListener != null) {
            discoveryListener.close();
        }
        if (discoveryAnnouncer != null) {
            discoveryAnnouncer.close();
        }
    }
}
