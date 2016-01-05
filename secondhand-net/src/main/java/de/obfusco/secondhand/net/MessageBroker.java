package de.obfusco.secondhand.net;

import java.util.Date;

public interface MessageBroker {
    void messageReceived(Peer peer, String message);
    void connected(Peer peer);
    void disconnected();
}
