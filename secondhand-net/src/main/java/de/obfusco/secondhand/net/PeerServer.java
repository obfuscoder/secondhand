package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PeerServer extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(PeerServer.class);

    private ServerSocket serverSocket;
    private Map<String,Peer> peers = new HashMap<>();
    private MessageBroker broker;

    public PeerServer(int port, MessageBroker broker) throws IOException {
        serverSocket = new ServerSocket(port);
        this.broker = broker;
        LOG.info("Listening on port " + port);
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                String hostAddress = socket.getInetAddress().getHostAddress();
                LOG.info("Peering request from " + hostAddress);
                if (isPeered(hostAddress)) {
                    socket.getOutputStream().write("ERROR! Already peered. Closing connection.\n".getBytes());
                    socket.close();
                } else {
                    Peer peer = new Peer(socket, this);
                    peer.start();
                    peers.put(hostAddress, peer);
                }
            } catch (IOException e) {
                LOG.error("Communication error", e);
            }
        }
    }

    @Override
    public void close() {
        LOG.info("Closing server socket");
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.warn("Could not close server socket", e);
        }
        for(Iterator<Map.Entry<String,Peer>> it = peers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Peer> entry = it.next();
            entry.getValue().close();
            it.remove();
        }
    }

    public boolean isPeered(String hostAddress) {
        return peers.containsKey(hostAddress);
    }

    public void peerError(String hostAddress) {
        LOG.error("Error with peer " + hostAddress);
        Peer erroneousPeer = peers.remove(hostAddress);
        if (erroneousPeer != null) erroneousPeer.close();
    }

    public void peerDisconnected(String hostAddress) {
        LOG.warn("Peer disconnected: " + hostAddress);
        Peer peer = peers.remove(hostAddress);
        peer.close();
    }

    public void packetReceived(Peer peer, String message) {
        peer.send(broker.message(message));
    }
}