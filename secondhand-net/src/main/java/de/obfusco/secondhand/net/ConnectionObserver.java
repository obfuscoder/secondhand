package de.obfusco.secondhand.net;

import java.net.Socket;

public interface ConnectionObserver {
    void connectionEstablished(Socket socket);
}
