package de.obfusco.secondhand.net;

import java.net.InetAddress;

interface DiscoveryObserver {
    void peerDiscovered(InetAddress hostAddress);
}
