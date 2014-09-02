package de.obfusco.secondhand.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Network implements Closeable,DiscoveryObserver {

    private final static Logger LOG = LoggerFactory.getLogger(Network.class);

    private Discovery discovery;
    private PeerServer server;
    private int port;
    private Map<String,PeerClient> peers = new HashMap<>();
    private List<String> localHostAddresses;

    public Network(int port, MessageBroker broker) throws IOException {
        localHostAddresses = getLocalHostAddresses();
        this.port = port;
        discovery = new Discovery(port, this);
        server = new PeerServer(port, broker);
    }
    public void start() {
        discovery.start();
        server.start();
    }

    @Override
    public void close() throws IOException {
        discovery.close();
        server.close();
        closePeers();
    }

    private void closePeers() {
        for(Iterator<Map.Entry<String, PeerClient>> it = peers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, PeerClient> entry = it.next();
            entry.getValue().close();
            it.remove();
        }
    }

    public void peerDiscovered(String hostAddress) {
        if (!isLocalAddress(hostAddress) && !isPeered(hostAddress)) {
            LOG.info("Received announcement from + {}", hostAddress);
            try {
                peers.put(hostAddress, new PeerClient(hostAddress, port));
            } catch (IOException e) {
                LOG.error("Could not create peer client to host " + hostAddress, e);
            }
        }
    }

    private boolean isPeered(String hostAddress) {
        return peers.containsKey(hostAddress);
    }

    public List<String> send(String request) {
        List<String> responses = new ArrayList<>();

        for(Iterator<Map.Entry<String, PeerClient>> it = peers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, PeerClient> entry = it.next();
            try {
                responses.add(entry.getValue().send(request));
            } catch (IOException e) {
                LOG.error("Could not send data to a peer", e);
                it.remove();
            }
        }
        return responses;
    }

    public boolean isLocalAddress(String hostAddress) {
        return localHostAddresses.contains(hostAddress);
    }

    private List<String> getLocalHostAddresses() throws SocketException {
        List<String> hostAddresses = new ArrayList<>();
        for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
            NetworkInterface networkInterface = interfaces.nextElement();

            for (Enumeration<InetAddress> addresses = networkInterface.getInetAddresses(); addresses.hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                hostAddresses.add(address.getHostAddress());
            }
        }
        return hostAddresses;
    }
}
