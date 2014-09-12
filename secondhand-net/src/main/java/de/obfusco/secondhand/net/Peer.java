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
    private Ping ping;

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
        try {
            LOG.info("Peer starting");
            ping = new Ping(this);
            ping.start();
            while (isConnected()) {
                try {
                    socket.setSoTimeout(30000);
                    String line = receiver.readLine();
                    if (line == null) {
                        LOG.info("Connection closed by peer.");
                        peerObserver.disconnected(this);
                        return;
                    }
                    if (!line.equals("PING")) peerObserver.messageReceived(this, line);
                } catch (IOException e) {
                    LOG.error("Error while communication with peer " + socket.getInetAddress().getHostAddress(), e);
                    peerObserver.errorOccurred(this);
                }
            }
        }
        catch(Exception ex) {
            LOG.error("Exception!", ex);
            peerObserver.errorOccurred(this);
        }
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    @Override
    public void close() {
        LOG.info("Closing connection");
        ping.quit();
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

    public synchronized void send(String message) {
        if (isConnected()) sender.println(message);
    }

    private class Ping extends Thread {
        private Peer peer;
        private volatile boolean running;

        public Ping(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            LOG.info("Ping running");
            running = true;
            while(running && peer.isConnected()) {
                try {
                    synchronized (this) {
                        wait(5000);
                    }
                } catch (InterruptedException ex) {
                    LOG.error("Caught exception while waiting for next ping", ex);
                    return;
                }
                if (!running) break;
                peer.send("PING");
            }
        }

        public void quit() {
            running = false;
        }
    }
}
