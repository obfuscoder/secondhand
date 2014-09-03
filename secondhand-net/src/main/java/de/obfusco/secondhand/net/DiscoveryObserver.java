package de.obfusco.secondhand.net;

public interface DiscoveryObserver {
    void peerDiscovered(String hostAddress);
}
