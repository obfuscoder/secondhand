package de.obfusco.secondhand.net;

public interface PeerObserver {
    void connected(Peer peer);
    void disconnected(Peer peer);
    void errorOccurred(Peer peer);
    void messageReceived(Peer peer, String message);
}
