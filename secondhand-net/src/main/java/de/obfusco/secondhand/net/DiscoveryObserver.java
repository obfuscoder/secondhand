package de.obfusco.secondhand.net;

import java.net.InetAddress;

public interface DiscoveryObserver {
    void peerDiscovered(InetAddress hostAddress);
}
