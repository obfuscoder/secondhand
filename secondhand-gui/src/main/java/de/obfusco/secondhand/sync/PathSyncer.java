package de.obfusco.secondhand.sync;

import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import de.obfusco.secondhand.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class PathSyncer {
    private final static Logger LOG = LoggerFactory.getLogger(PathSyncer.class);
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TransactionListener transactionListener;
    @Autowired
    private PathSyncListener pathSyncListener;
    @Autowired
    private StorageService storageService;
    private boolean stop = false;

    public void synchronize(final String path, final String localName) {
        new Thread(() -> {
            int currentEventNumber = eventRepository.find().number;
            while (!stop) {
                int sleepTime = 10000;
                File folder = new File(path);
                if (folder.exists() && folder.isDirectory()) {
                    pathSyncListener.syncPathAvailable();
                    sleepTime = 60000;
                    File localFile = new File(folder, localName + ".transactions");
                    pathSyncListener.synchronizationStarted();
                    LOG.info("Writing transactions to {}.", localFile);
                    try (PrintWriter writer = new PrintWriter(localFile)) {
                        writer.println(currentEventNumber);
                        for (Transaction transaction : transactionRepository.findAll(new Sort("created"))) {
                            writer.println(transaction);
                        }
                    } catch (FileNotFoundException e) {
                        LOG.error("could not open file for writing transactions", e);
                        pathSyncListener.synchronizationError();
                    }

                    File[] files = folder.listFiles((folder1, fileName) -> !fileName.startsWith(localName) && fileName.endsWith(".transactions"));
                    for (File file : files) {
                        LOG.info("Reading transactions from file {}", file);
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            readFirstLineAndCheckEventNumber(file, reader, currentEventNumber);
                            while (true) {
                                String line = reader.readLine();
                                if (line == null) break;
                                Transaction transaction = storageService.parseTransactionMessage(line);
                                transactionListener.transactionReceived(transaction);
                            }
                        } catch (FileNotFoundException e) {
                            LOG.error("Cannot read file {}: {}", file, e.getMessage());
                            pathSyncListener.synchronizationError();
                        } catch (IOException e) {
                            LOG.error("Error while reading file {}: {}", file, e.getMessage());
                            pathSyncListener.synchronizationError();
                        } catch (IllegalArgumentException e) {
                            LOG.error("Error parsing file content of {}: {}", file, e.getMessage());
                            pathSyncListener.synchronizationError();
                        }

                    }
                    pathSyncListener.synchronizationFinished();
                } else {
                    LOG.info("Path {} does not exist or is not a directory, skipping synchronisation", path);
                    pathSyncListener.syncPathNotAvailable();
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    LOG.warn("Got interrupted while sleeping. Exiting!");
                    break;
                }
            }
        }).start();
    }

    private void readFirstLineAndCheckEventNumber(File file, BufferedReader reader, int currentEventNumber) throws IOException {
        LOG.info("Reading first line with event number from file {}", file);
        String line = reader.readLine();
        if (currentEventNumber != Integer.parseInt(line)) {
            LOG.error("Event number {} from transaction file {} does not match current event number {}", line, file, currentEventNumber);
            throw new IllegalArgumentException("Wrong event number");
        }
    }
}
