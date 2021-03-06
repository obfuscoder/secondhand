package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Network implements Closeable,DiscoveryObserver,PeerObserver, ConnectionObserver {

    private final static Logger LOG = LoggerFactory.getLogger(Network.class);

    private Discovery discovery;
    private PeerListener server;
    private int port;
    private Map<String,Peer> peers = new HashMap<>();
    private List<String> localHostAddresses;
    private MessageBroker broker;
    private String name;
    private NetworkInterface networkInterface;

    public Network(int port, MessageBroker broker, String name) throws IOException {
        this.broker = broker;
        this.port = port;
        this.name = name;
        localHostAddresses = getLocalHostAddresses();
        discovery = new Discovery(port, this, name, networkInterface);
        server = new PeerListener(port, this);
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
        for(Iterator<Map.Entry<String,Peer>> it = peers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String,Peer> entry = it.next();
            entry.getValue().close();
            it.remove();
        }
    }

    public void peerDiscovered(InetAddress address) {
        LOG.debug("Announcement received from {}({})", address.getHostAddress(), address.getHostName());
        if (!isLocalAddress(address.getHostAddress()) && !isPeered(address.getHostAddress())) {
            LOG.info("Accepted announcement from {}", address);
            try {
                createPeer(new Socket(address, port), address.getHostAddress());
            } catch (IOException e) {
                LOG.error("Could not create peer client to host " + address, e);
            }
        }
    }

    private boolean isPeered(String hostAddress) {
        return peers.containsKey(hostAddress);
    }

    public void send(String message) {
        for(Peer peer : peers.values()) {
            peer.send(message);
        }
    }

    private boolean isLocalAddress(String hostAddress) {
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
            if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                LOG.info("Using network interface {}");
                this.networkInterface = networkInterface;
            }
        }
        return hostAddresses;
    }

    @Override
    public void connected(Peer peer) {
        broker.connected(peer);
    }

    @Override
    public void disconnected(Peer peer) {
        LOG.info("Peer disconnected: " + peer.getAddress());
        removeAndClosePeer(peer);
    }

    @Override
    public void errorOccurred(Peer peer) {
        LOG.error("Error with peer " + peer.getAddress());
        removeAndClosePeer(peer);
    }

    private void removeAndClosePeer(Peer peer) {
        peers.remove(peer.getHostAddress());
        peer.close();
        broker.disconnected();
    }

    @Override
    public void messageReceived(Peer peer, String message) {
        broker.messageReceived(peer, message);
    }

    @Override
    public void connectionEstablished(Socket socket) {
        String hostAddress = socket.getInetAddress().getHostAddress();
        LOG.info("Peering request from " + hostAddress);
        if (isPeered(hostAddress)) {
            try {
                LOG.warn("Already peered with this host");
                socket.getOutputStream().write("ERROR! Already peered. Closing connection.\n".getBytes());
                socket.close();
            } catch (IOException e) {
                LOG.error("Could not communicate/close (with) host " + hostAddress, e);
            }
        } else {
            try {
                createPeer(socket, hostAddress);
            } catch (IOException e) {
                LOG.error("Error while peering with " + hostAddress, e);
            }
        }
    }

    private void createPeer(Socket socket, String hostAddress) throws IOException {
        Peer peer = new Peer(socket, this, name);
        peer.start();
        peers.put(hostAddress, peer);
        connected(peer);
    }

    public int getNumberOfPeers() {
        return peers.size();
    }

    public Collection<Peer> getPeers() {
        return peers.values();
    }
}
