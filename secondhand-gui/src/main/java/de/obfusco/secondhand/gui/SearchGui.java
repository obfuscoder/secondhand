package de.obfusco.secondhand.gui;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.Container;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
public class SearchGui extends JFrame {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(SearchGui.class);

    private static final String TITLE = "Artikelsuche";

    @Autowired
    ReservedItemRepository reservedItemRepository;
    private JTextField searchText;
    private JTable resultTable;

    public SearchGui() {
        super(TITLE);
        setSize(500, 600);
        setLocationRelativeTo(null);
    }

    public void open() {
        final Container pane = getContentPane();
        pane.removeAll();
        addComponentsToPane(pane);
        setVisible(true);
    }

    private void addComponentsToPane(Container pane) {
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        JLabel hint = new JLabel("Geben Sie ein bis drei Suchwörter ein");
        pane.add(hint);
        searchText = new JTextField();
        searchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                changedUpdate(documentEvent);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                changedUpdate(documentEvent);
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                String[] keywords = searchText.getText().split(" ");
                List<ReservedItem> results = null;
                switch (keywords.length) {
                    case 0:
                        return;
                    case 1:
                        results = reservedItemRepository.findByKeywords(makeLike(keywords[0]));
                        break;
                    case 2:
                        results = reservedItemRepository.findByKeywords(makeLike(keywords[0]), makeLike(keywords[1]));
                        break;
                    default:
                        results = reservedItemRepository.findByKeywords(makeLike(keywords[0]), makeLike(keywords[1]), makeLike(keywords[2]));
                }
                ResultsModel resultsModel = new ResultsModel(results);
                resultTable.setModel(resultsModel);
            }

            private String makeLike(String keyword) {
                return '%' + keyword + '%';
            }
        });
        searchText.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(searchText);
        resultTable = new JTable(new ResultsModel(null));
        pane.add(new JScrollPane(resultTable));
    }

    private static class ResultsModel extends AbstractTableModel {

        private final List<ReservedItem> results;

        private final static NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        private final static String[] COLUMN_NAMES = new String[] {
                "ArtNr", "Kategorie", "Bezeichnung", "Größe", "Preis", "verkauft" };

        public ResultsModel(List<ReservedItem> results) {
            this.results = results;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public int getRowCount() {
            if (results == null) return 0;
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int row, int column) {
            ReservedItem result = results.get(row);
            switch (column) {
                case 0:
                    return result.getCode();
                case 1:
                    return result.getItem().getCategory().getName();
                case 2:
                    return result.getItem().getDescription();
                case 3:
                    return result.getItem().getSize();
                case 4:
                    return currency.format(result.getItem().getPrice());
                case 5:
                    return result.isSold() ? "ja" : "nein";
                default:
                    return "";
            }
        }
    }
}