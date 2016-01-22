package de.obfusco.secondhand.net;

import de.obfusco.secondhand.net.dto.Event;

import java.io.IOException;

public interface DataPusher {
    void push(Event event) throws IOException;
}
