package de.obfusco.secondhand.secondhand.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

class DiscoveryListener extends Thread implements Closeable {

    private volatile DatagramSocket datagramSocket;

    public DiscoveryListener(DatagramSocket datagramSocket) throws SocketException {
        this.datagramSocket = datagramSocket;
        datagramSocket.setSoTimeout(1000);
    }

    @Override
    public void run() {
        int counter = 0;
        byte[] buffer = new byte[4096];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        while (true) {
            if (datagramSocket == null) {
                System.out.println("Terminating listener.");
                System.out.println("Received " + counter + " packets");
                return;
            }
            try {
                System.out.println("Waiting for incoming data ...");
                datagramSocket.receive(datagramPacket);
                counter++;
                System.out.println("Received " + new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + " from " + datagramPacket.getAddress().getHostAddress());
                InetAddress address = datagramPacket.getAddress();
                Boolean[] b = {
                    address.isAnyLocalAddress(),
                    address.isLinkLocalAddress(),
                    address.isLoopbackAddress(),
                    address.isMCGlobal(),
                    address.isMCLinkLocal(),
                    address.isMCNodeLocal(),
                    address.isMCOrgLocal(),
                    address.isMCSiteLocal(),
                    address.isMulticastAddress(),
                    address.isSiteLocalAddress()
                };
                System.out.println(Arrays.deepToString(b));
            } catch (SocketTimeoutException ex) {
                // expected to occur after 1 sec of waiting for packets
            } catch (IOException ex) {
                System.out.println("receive failed!");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        datagramSocket = null;
    }
}
