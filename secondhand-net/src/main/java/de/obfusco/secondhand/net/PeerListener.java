package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class PeerListener extends Thread implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(PeerListener.class);

    private ServerSocket serverSocket;

    private ConnectionObserver observer;

    public PeerListener(int port, ConnectionObserver observer) throws IOException {
        this.observer = observer;
        serverSocket = new ServerSocket(port);
        LOG.info("Listening on port " + port);
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                observer.connectionEstablished(socket);
            } catch (IOException e) {
                LOG.error("Communication error", e);
            }
        }
    }

    @Override
    public void close() {
        LOG.info("Closing server socket");
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.warn("Could not close server socket", e);
        }
    }
}
