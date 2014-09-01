package de.obfusco.secondhand.secondhand.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Peer extends Thread implements Closeable {
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
        log("Connection established.");
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            String line = null;
            try {
                line = receiver.readLine();
                if (line == null) {
                    log("Connection closed by peer.");
                    peerServer.peerDisconnected(socket.getInetAddress().getHostAddress());
                    return;
                }
                peerServer.packetReceived(this, line);
            } catch (IOException e) {
                e.printStackTrace();
                peerServer.peerError(socket.getInetAddress().getHostAddress());
            }
        }
    }

    private void log(String message) {
        System.out.println("PEER " + socket.getInetAddress().getHostAddress() + " - " + message);
    }

    @Override
    public void close() {
        log("Closing.");
        sender.close();
        try {
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        sender.println(message);
    }
}
