package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Discovery implements Closeable {

    private DiscoveryListener discoveryListener;
    private DiscoveryAnnouncer discoveryAnnouncer;
    private DatagramSocket datagramSocket;

    public Discovery(int port) throws SocketException {
        datagramSocket = new DatagramSocket(port);
        discoveryListener = new DiscoveryListener(datagramSocket);
        discoveryAnnouncer = new DiscoveryAnnouncer(datagramSocket);
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
