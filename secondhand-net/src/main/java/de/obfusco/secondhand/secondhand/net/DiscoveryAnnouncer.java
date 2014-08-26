package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

class DiscoveryAnnouncer extends Thread implements Closeable {

    private volatile DatagramSocket datagramSocket;

    public DiscoveryAnnouncer(DatagramSocket datagramSocket) throws SocketException {
        this.datagramSocket = datagramSocket;
        datagramSocket.setBroadcast(true);
    }

    @Override
    public void run() {
        int counter = 0;
        final InetAddress broadcastAddress;
        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException ex) {
            System.out.println("Broadcast address unknown. Cannot send broadcast. Exiting.");
            return;
        }
        while (true) {
            if (datagramSocket == null) {
                System.out.println("Terminating announcing.");
                System.out.println("Sent " + counter + " packets");
                return;
            }
            try {
                sendAnnouncement(broadcastAddress);
                for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() == null) {
                            continue;
                        }
                        sendAnnouncement(interfaceAddress.getBroadcast());
                    }
                }
            } catch (IOException ex) {
                System.out.println("Send failed!");
                ex.printStackTrace();
            }
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Interrupted. Exiting.");
                return;
            }
        }
    }

    private void sendAnnouncement(final InetAddress broadcastAddress) throws IOException {
        byte[] buffer = "HELLO".getBytes();
        DatagramPacket datagramPacket;
        datagramPacket = new DatagramPacket(buffer, buffer.length, broadcastAddress, datagramSocket.getLocalPort());
        datagramSocket.send(datagramPacket);
        System.out.println("Sent announcement to " + broadcastAddress);
    }

    @Override
    public void close() throws IOException {
        datagramSocket = null;
    }
}
