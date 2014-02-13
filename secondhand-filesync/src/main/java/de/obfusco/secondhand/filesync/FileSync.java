package de.obfusco.secondhand.filesync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
class FileSync {

    private final static Logger LOG = LoggerFactory.getLogger(FileSync.class);

    @Autowired
    ReservedItemRepository reservedItemRepository;

    void start(String path) {
        List<ReservedItem> soldItemsInDatabase = reservedItemRepository.findBySoldNotNull();
        synchronizePathWithItemsInDatabase(Paths.get(path, "sold").toString(), soldItemsInDatabase, new SoldAction());

        List<ReservedItem> refundedItemsInDatabase = reservedItemRepository.findByRefundedNotNull();
        synchronizePathWithItemsInDatabase(Paths.get(path, "refunded").toString(), refundedItemsInDatabase, new RefundedAction());
    }

    private void synchronizePathWithItemsInDatabase(
            String path, List<ReservedItem> itemsInDatabase, Action action) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        File syncFolder = new File(path);
        syncFolder.mkdirs();
        Set<String> filesToRead = new HashSet<>();
        filesToRead.addAll(Arrays.asList(syncFolder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.length() == 8 && Pattern.matches("\\d{8}", name);
            }
        })));
        LOG.info("Found {} files in folder {}", filesToRead.size(), path);
        LOG.info("Found {} items in database", itemsInDatabase.size());
        for (ReservedItem reservedItem : itemsInDatabase) {
            String code = reservedItem.getCode();
            if (!filesToRead.contains(code)) {
                LOG.info("Creting new file sold date on item with code {}", code);
                File newFile = new File(syncFolder, code);
                try (PrintWriter printWriter = new PrintWriter(newFile)) {
                    printWriter.print(dateFormat.format(action.getDate(reservedItem)));
                } catch (FileNotFoundException ex) {
                    LOG.error("File {} could not be opened for writing!", newFile);
                }
            }
            filesToRead.remove(code);
        }
        System.out.println("After matching " + filesToRead.size() + " files remain to be read");
        for (String code : filesToRead.toArray(new String[0])) {
            File fileToRead = new File(syncFolder, code);
            String line = null;
            try {
                List<String> lines = Files.readAllLines(fileToRead.toPath(), StandardCharsets.UTF_8);
                line = lines.get(0);
                Date parsedDate = dateFormat.parse(line);
                ReservedItem reservedItem = reservedItemRepository.findByCode(code);
                if (reservedItem != null) {
                    LOG.info("Setting date on item with code {}", code);
                    action.execute(reservedItem, parsedDate);
                    reservedItemRepository.save(reservedItem);
                }
            } catch (IOException ex) {
                LOG.error("Could not read content of file " + fileToRead, ex);
            } catch (ParseException ex) {
                LOG.error("Could not parse content " + line + " of file " + fileToRead, ex);
            }
        }
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(FileSyncConfig.class);
        FileSync sync = applicationContext.getBean(FileSync.class);
        sync.start("data/sync");
    }

    private static class SoldFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name != null && name.length() == 8 && Pattern.matches("\\d{8}", name);
        }
    }

    private interface Action {

        void execute(ReservedItem item, Date date);

        Date getDate(ReservedItem item);
    }

    private static class SoldAction implements Action {

        @Override
        public void execute(ReservedItem item, Date date) {
            item.setSold(date);
        }

        @Override
        public Date getDate(ReservedItem item) {
            return item.getSold();
        }
    }

    private static class RefundedAction implements Action {

        @Override
        public void execute(ReservedItem item, Date date) {
            item.setRefunded(date);
        }

        @Override
        public Date getDate(ReservedItem item) {
            return item.getRefunded();
        }
    }
}
