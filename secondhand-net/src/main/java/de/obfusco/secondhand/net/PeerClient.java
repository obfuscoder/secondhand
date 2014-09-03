package de.obfusco.secondhand.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PeerClient implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(PeerClient.class);

    private final PrintWriter out;
    private final BufferedReader in;
    private Socket socket;

    public PeerClient(String hostAddress, int port) throws IOException {
        socket = new Socket(hostAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            LOG.warn("Could not close socket", e);
        }
        out.close();
        try {
            in.close();
        } catch (IOException e) {
            LOG.warn("Could not close input reader", e);
        }
    }

    public String send(String message) throws IOException {
        out.println(message);
        return in.readLine();
    }
}
