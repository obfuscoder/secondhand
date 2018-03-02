package de.obfusco.secondhand.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static Logger LOG = LoggerFactory.getLogger(MainGui.class);

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        LOG.error("Uncaught exception occurred!", throwable);
    }
}
