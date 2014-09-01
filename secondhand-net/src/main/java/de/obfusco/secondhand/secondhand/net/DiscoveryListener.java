package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

class DiscoveryListener extends Thread implements Closeable {

    private volatile MulticastSocket socket;
    private DiscoveryObserver observer;

    public DiscoveryListener(MulticastSocket socket, DiscoveryObserver observer) throws SocketException {
        this.socket = socket;
        this.observer = observer;
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            if (socket == null) {
                System.out.println("DISCO - Terminating listener.");
                System.out.println("DISCO - Received " + counter + " packets");
                return;
            }
            try {
                byte[] buffer = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);
                counter++;
                System.out.println("DISCO - Received " + new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + " from " + datagramPacket.getAddress().getHostAddress());
                observer.peerDiscovered(datagramPacket.getAddress().getHostAddress());
            } catch (IOException ex) {
                System.out.println("DISCO - Receive failed!");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
