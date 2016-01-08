package de.obfusco.secondhand.sync;

import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
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
    private TransactionListener transactionListener;
    @Autowired
    private StorageService storageService;
    private boolean stop = false;

    public void synchronize(final String path, final String localName) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stop) {
                    int sleepTime = 10000;
                    File folder = new File(path);
                    if (folder.exists() && folder.isDirectory()) {
                        sleepTime = 60000;
                        File localFile = new File(folder, localName + ".transactions");
                        LOG.info("Writing transactions to {}.", localFile);
                        try (PrintWriter writer = new PrintWriter(localFile)) {
                            for (Transaction transaction : transactionRepository.findAll(new Sort("created"))) {
                                writer.println(transaction);
                            }
                        } catch (FileNotFoundException e) {
                            LOG.error("could not open file for writing transactions", e);
                        }

                        File[] files = folder.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File folder, String fileName) {
                                return !fileName.startsWith(localName) && fileName.endsWith(".transactions");
                            }
                        });
                        for (File file : files) {
                            LOG.info("Reading transactions from file {}", file);
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                while (true) {
                                    String line = reader.readLine();
                                    if (line == null) break;
                                    Transaction transaction = storageService.parseTransactionMessage(line);
                                    transactionListener.transactionReceived(transaction);
                                }
                            } catch (FileNotFoundException e) {
                                LOG.error("Cannot read file {}: {}", file, e);
                            } catch (IOException e) {
                                LOG.error("Error while reading file {}: {}", e);
                            }
                        }
                    } else {
                        LOG.info("Path {} does not exist or is not a directory, skipping synchronisation", path);
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        LOG.warn("Got interrupted while sleeping. Exiting!");
                        break;
                    }
                }
            }
        }).start();

    }


}
