package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Peer extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(Peer.class);

    private final PrintWriter sender;
    private final BufferedReader receiver;
    private Socket socket;
    private PeerObserver peerObserver;

    public Peer(Socket socket, PeerObserver peerObserver) throws IOException {
        this.socket = socket;
        this.peerObserver = peerObserver;
        sender =
                new PrintWriter(socket.getOutputStream(), true);
        receiver = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

    }

    @Override
    public void run() {
        peerObserver.connected(this);
        while (!socket.isClosed()) {
            try {
                String line = receiver.readLine();
                if (line == null) {
                    LOG.info("Connection closed by peer.");
                    peerObserver.disconnected(this);
                    return;
                }
                peerObserver.messageReceived(this, line);
            } catch (IOException e) {
                LOG.error("Error while communication with peer " + socket.getInetAddress().getHostAddress(), e);
                peerObserver.errorOccurred(this);
            }
        }
    }

    @Override
    public void close() {
        LOG.info("Closing connection");
        sender.close();
        try {
            receiver.close();
        } catch (IOException e) {
            LOG.warn("Could not close receiver", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOG.warn("Could not close socket", e);
        }
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public void send(String message) {
        sender.println(message);
    }
}
