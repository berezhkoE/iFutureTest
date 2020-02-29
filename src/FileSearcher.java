import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FileSearcher implements Callable<MutableTreeNode> {
    final ExecutorService service = Executors.newFixedThreadPool(4);

    File directory;
    String extension;
    String text;

    public FileSearcher(File directory, String extension, String text) {
        this.directory = directory;
        this.extension = extension;
        this.text = text;
    }

    @Override
    public MutableTreeNode call() throws Exception {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(directory.getName());

        try {
            File[] files = directory.listFiles();
            ArrayList<Future> futures = new ArrayList();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    futures.add(service.submit(new FileSearcher(file, extension, text)));
                } else if (file.getName().endsWith(extension) && search(file)) {
                    result.add(new DefaultMutableTreeNode(file.getName()));
                }
            }
            for (Future future : futures) {
                DefaultMutableTreeNode d = (DefaultMutableTreeNode) future.get();
                if (d.getLastLeaf().toString().endsWith(extension))
                    result.add(d);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            service.shutdown();
        }

        return result;
    }

    private boolean search(File file) {
        boolean result = false;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine())!=null) {
                if (s.contains(text)) {
                    result = true;
                }
            }
        }
        catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }
}
