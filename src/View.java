import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.event.*;
import java.io.File;

public class View extends JDialog {
    Controller controller;

    private JFileChooser fileChooser = new JFileChooser();
    private File directory;

    private JPanel contentPane;
    private JTextField textFieldExtension;
    private JTextField textField;
    private JButton ButtonFolder;
    private JButton buttonSearch;
    private JTree tree;
    private JTextField textFieldDir;
    private JTextArea textArea;
    private JRadioButton highlightRadioButton;
    private JButton previousButton;
    private JButton nextButton;
    private JLabel Status;

    public View(Controller controller) {
        this.controller = controller;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSearch);

        tree.setModel(new JTree(controller.getTreeView()).getModel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        tree.addTreeSelectionListener(this::onSelection);

        ButtonFolder.addActionListener(e -> onButtonFolder());
        buttonSearch.addActionListener(e -> onButtonSearch());

        textFieldDir.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reaction();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reaction();
            }

            public void reaction() {
                directory = new File(textFieldDir.getText());
            }
        });

        highlightRadioButton.addActionListener(e -> {
            if (highlightRadioButton.isSelected()) {
                controller.highlightMatches();
                nextButton.setEnabled(false);
                previousButton.setEnabled(false);
            }
            else {
                textArea.getHighlighter().removeAllHighlights();
                nextButton.setEnabled(true);
                previousButton.setEnabled(true);
            }});

        nextButton.addActionListener(e -> highlightNext());
        previousButton.addActionListener(e -> highlightPrevious());
    }

    private void highlightPrevious() {
        controller.highlightPrevious();
    }

    private void highlightNext() {
        controller.highlightNext();
    }

    private void onSelection(TreeSelectionEvent e) {
        Thread openFileThread = new Thread(new OpenFileThread(e.getPath().toString()), "openFileThread");
        openFileThread.setDaemon(true);
        openFileThread.start();
    }

    private void onCancel() {
        dispose();
    }

    private void onButtonFolder() {
        fileChooser.setDialogTitle("Выбор директории");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showOpenDialog(contentPane);

        textFieldDir.setText(fileChooser.getSelectedFile().toString());
        directory = fileChooser.getSelectedFile();
        textArea.setText("");
        tree.setModel(null);
    }

    private void onButtonSearch() {
        textArea.setText("");

        Thread directoryThread = new Thread(new SearchThread(), "searchThread");
        directoryThread.setDaemon(true);
        directoryThread.start();
    }

    class SearchThread implements Runnable {
        @Override
        public void run() {
            Status.setText("searching...");
            controller.searchFiles();
            Status.setText("");
            tree.setModel(new JTree(controller.getTreeView()).getModel());
        }
    }

    class OpenFileThread implements Runnable {
        String path;

        public OpenFileThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            controller.openFile(path);
            if (textArea.getText().equals(""))
                highlightRadioButton.setEnabled(false);
            else {
                highlightRadioButton.setEnabled(true);
                if (highlightRadioButton.isSelected())
                    controller.highlightMatches();
            }
        }
    }

    public String getExtension() {
        return textFieldExtension.getText();
    }

    public String getText() {
        return textField.getText();
    }

    public File getDirectory() {
        return directory;
    }

    public JTextArea getTextArea() {
        return textArea;
    }


}
