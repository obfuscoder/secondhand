package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Peer extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(Peer.class);

    private final PrintWriter sender;
    private final BufferedReader receiver;
    private Socket socket;
    private PeerObserver peerObserver;
    private Ping ping;
    private long timeDiff;
    private String localName;
    private String peerName;

    public Peer(Socket socket, PeerObserver peerObserver, String localName) throws IOException {
        this.socket = socket;
        this.peerObserver = peerObserver;
        this.localName = localName;
        sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
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
                    LOG.debug("Received from peer {}: {}", socket.getInetAddress().getHostAddress(), line);
                    if (line.startsWith("PING")) {
                        if (line.length() > 4) pingReceived(line.substring(5));
                    } else {
                        peerObserver.messageReceived(this, line);
                    }
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

    private void pingReceived(String message) {
        if (message.isEmpty()) return;
        String[] pingParts = message.split(";");
        long peerTime = Long.parseLong(pingParts[0]);
        timeDiff = new Date().getTime() - peerTime;
        peerName = pingParts[1];
        LOG.debug("Peer {} info update: tdiff={}, name={}", getAddress(), timeDiff, peerName);
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

    public String getHostAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public String getHostName() {
        return socket.getInetAddress().getHostName();
    }

    public String getAddress() {
        return String.format("%s(%s)", getHostAddress(), getHostName());
    }

    public String getPeerName() {
        return peerName;
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public synchronized void send(String message) {
        if (isConnected()) {
            LOG.debug("Sending to peer {}: {}", getAddress(), message);
            sender.println(message);
        } else {
            LOG.warn("Not connected to {} - not sending {}", getAddress(), message);
        }
    }

    public String getLocalName() {
        return localName;
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
            while (running && peer.isConnected()) {
                try {
                    synchronized (this) {
                        wait(5000);
                    }
                } catch (InterruptedException ex) {
                    LOG.error("Caught exception while waiting for next ping", ex);
                    return;
                }
                if (!running) break;
                peer.send(String.format("PING %d;%s", new Date().getTime(), peer.getLocalName()));
            }
        }

        public void quit() {
            running = false;
        }
    }
}
