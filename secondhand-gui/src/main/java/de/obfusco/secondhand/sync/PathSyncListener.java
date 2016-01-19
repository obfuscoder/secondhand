package de.obfusco.secondhand.sync;

public interface PathSyncListener {
    void syncPathNotAvailable();
    void syncPathAvailable();
    void synchronizationStarted();
    void synchronizationFinished();
    void synchronizationError();
}
