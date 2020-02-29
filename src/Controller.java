import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    private View view;

    private MutableTreeNode fileTree;

    private List<Integer> matchesPos;
    private ListIterator<Integer> matchesIterator;
    private JTextArea textArea;

    Controller () {
        createView();
    }

    public void createView() {
        view = new View(this);
        view.pack();
        view.setVisible(true);
        System.exit(0);
    }

    public void searchFiles() {
        FileSearcher fileSearcher = new FileSearcher(view.getDirectory(), view.getExtension(), view.getText());
        FutureTask task = new FutureTask(fileSearcher);
        Thread thread = new Thread(task);
        thread.start();
        try {
            fileTree = (MutableTreeNode) task.get();
        }
        catch(ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public MutableTreeNode getTreeView() {
        return fileTree;
    }

    public void openFile(String path) {
        File file = new File(view.getDirectory(), path.substring(path.indexOf(" ") + 1,
                path.length() - 1).replace(", ", "\\"));

        textArea = view.getTextArea();
        if (file.isFile()) {
            textArea.setText("");
            matchesPos = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                String s;
                int i = 0;
                while ((s = br.readLine())!=null) {
                    textArea.append(s + "\n");
                    Matcher m = Pattern.compile("(?=(" + view.getText() + "))").matcher(s);
                    while (m.find()) {
                        matchesPos.add(i + m.start());
                    }
                    i += s.length() + 1;
                }
            }
            catch(IOException ex) {
                System.out.println(ex.getMessage());
            }
            matchesIterator = matchesPos.listIterator();
        }
    }

    public void highlightMatches() {
        for (int p: matchesPos) {
            try {
                textArea.getHighlighter().addHighlight(p, p + view.getText().length(),
                        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void highlightNext() {
        if (matchesIterator.hasNext()) {
            int  pos = matchesIterator.next();
            textArea.grabFocus();
            textArea.select(pos, pos + view.getText().length());
        }
        else {
            JOptionPane.showMessageDialog(null, "Not found", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void highlightPrevious() {
        if (matchesIterator.hasPrevious()) {
            int  pos = matchesIterator.previous();
            textArea.grabFocus();
            textArea.select(pos, pos + view.getText().length());
        }
        else {
            JOptionPane.showMessageDialog(null, "Not found", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
