package de.obfusco.secondhand.net;

import java.net.Socket;

interface ConnectionObserver {
    void connectionEstablished(Socket socket);
}
