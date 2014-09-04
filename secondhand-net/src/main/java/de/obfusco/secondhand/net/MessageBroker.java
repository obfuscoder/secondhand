package de.obfusco.secondhand.net;

public interface MessageBroker {
    void messageReceived(Peer peer, String message);
    void connected(Peer peer);
}
