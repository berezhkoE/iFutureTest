import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileSearcher implements Callable<MutableTreeNode> {
    ExecutorService service = Executors.newFixedThreadPool(8);

    private File directory;
    private String extension;
    private String text;

    public FileSearcher(File directory, String extension, String text) {
        this.directory = directory;
        this.extension = extension;
        this.text = text;
    }

    @Override
    public MutableTreeNode call() throws ExecutionException, InterruptedException {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(directory.getName());

        try {
            File[] files = directory.listFiles();
            List<Future> futures = new ArrayList();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        futures.add(service.submit(new FileSearcher(file, extension, text)));
                    } else if (file.getName().endsWith("." + extension) && search(file)) {
                        result.add(new DefaultMutableTreeNode(file.getName()));
                    }
                }
            }
            for (Future future : futures) {
                DefaultMutableTreeNode d = (DefaultMutableTreeNode) future.get();
                if (d.getLastLeaf().toString().endsWith("." + extension))
                    result.add(d);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean search(File file) {
        boolean result = false;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
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
