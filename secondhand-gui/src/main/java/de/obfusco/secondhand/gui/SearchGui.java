package de.obfusco.secondhand.gui;

import de.obfusco.secondhand.storage.model.Item;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
class SearchGui extends JFrame {

    private static final String TITLE = "Artikelsuche";

    @Autowired
    de.obfusco.secondhand.storage.repository.ItemRepository ItemRepository;
    private JTextField searchText;
    private JTable resultTable;

    private SearchGui() {
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
        JButton searchButton = new JButton("Suchen");
        JLabel hint = new JLabel("Geben Sie ein bis drei Suchwörter ein.");
        searchText = new JTextField();
        JPanel topPanel = new JPanel(new BorderLayout(2, 2));
        topPanel.add(hint, BorderLayout.NORTH);
        topPanel.add(searchText, BorderLayout.CENTER);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String[] keywords = searchText.getText().split(" +");
                List<Item> results;
                switch (keywords.length) {
                    case 0:
                        return;
                    case 1:
                        results = ItemRepository.findByKeywords(makeLike(keywords[0]));
                        break;
                    case 2:
                        results = ItemRepository.findByKeywords(makeLike(keywords[0]), makeLike(keywords[1]));
                        break;
                    default:
                        results = ItemRepository.findByKeywords(makeLike(keywords[0]), makeLike(keywords[1]), makeLike(keywords[2]));
                }
                ResultsModel resultsModel = new ResultsModel(results);
                resultTable.setModel(resultsModel);
            }

            private String makeLike(String keyword) {
                return '%' + keyword + '%';
            }
        });
        topPanel.add(searchButton, BorderLayout.LINE_END);
        pane.add(topPanel, BorderLayout.NORTH);
        getRootPane().setDefaultButton(searchButton);
        resultTable = new JTable(new ResultsModel(null));
        resultTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    int row = resultTable.rowAtPoint(mouseEvent.getPoint());
                    String code = (String) resultTable.getModel().getValueAt(row, 0);
                    StringSelection selection = new StringSelection(code);
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    Clipboard clipboard = toolkit.getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        resultTable.setAutoCreateRowSorter(true);
        pane.add(new JScrollPane(resultTable));
        pane.add(new JLabel("Doppelklick zum Kopieren der Artikelnummer"), BorderLayout.SOUTH);
    }

    private static class ResultsModel extends AbstractTableModel {

        private final static NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        private final static String[] COLUMN_NAMES = new String[] {
                "ArtNr", "Kategorie", "Bezeichnung", "Größe", "Preis", "verkauft" };
        private final List<Item> results;

        ResultsModel(List<Item> results) {
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
            Item result = results.get(row);
            switch (column) {
                case 0:
                    return result.code;
                case 1:
                    return result.getCategoryName();
                case 2:
                    return result.description;
                case 3:
                    return result.getSize();
                case 4:
                    return currency.format(result.price);
                case 5:
                    return result.wasSold() ? "ja" : "nein";
                default:
                    return "";
            }
        }
    }
}