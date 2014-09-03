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
    private PeerServer peerServer;

    public Peer(Socket socket, PeerServer peerServer) throws IOException {
        this.socket = socket;
        this.peerServer = peerServer;
        sender =
                new PrintWriter(socket.getOutputStream(), true);
        receiver = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        LOG.info("Connection established");
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            String line = null;
            try {
                line = receiver.readLine();
                if (line == null) {
                    LOG.info("Connection closed by peer.");
                    peerServer.peerDisconnected(socket.getInetAddress().getHostAddress());
                    return;
                }
                peerServer.packetReceived(this, line);
            } catch (IOException e) {
                LOG.error("Error while communication with peer " + socket.getInetAddress().getHostAddress(), e);
                peerServer.peerError(socket.getInetAddress().getHostAddress());
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

    public void send(String message) {
        sender.println(message);
    }
}
