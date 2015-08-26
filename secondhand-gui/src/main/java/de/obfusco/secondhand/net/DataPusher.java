package de.obfusco.secondhand.net;

import de.obfusco.secondhand.net.dto.Event;

public interface DataPusher {
    void push(Event event);
}
