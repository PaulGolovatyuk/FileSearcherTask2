import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class App {

    private static BlockingQueue<Path> queue = new ArrayBlockingQueue<>(3);
    private static int counter;

    public static void main(String[] args) throws InterruptedException {
        if (args.length!=3){
            throw new IllegalArgumentException("You should pass all (3) arguments");
        }
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Path rootPath = Paths.get(args[0]);
        int depth = Integer.parseInt(args[1]);
        String mask = args[2];

        Thread thread1 = new Thread(() -> {
            try {
                searchFiles(rootPath, depth, mask);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                showFiles();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        executorService.submit(thread1);
        executorService.submit(thread2);
        executorService.shutdown();

        executorService.awaitTermination(20, TimeUnit.SECONDS);
        System.out.println("There were " + counter +" files found.");
    }

    private static void showFiles() throws InterruptedException{
        while (true){
            Thread.sleep(30);
            Path path = queue.take();
            System.out.println(path);
            if (queue.isEmpty()){
                Thread.sleep(10);
                break;
            }
        }
    }

    private static void searchFiles(Path aPath, int depth, String mask) throws IOException {
        Stream<Path> stream =
                Files.find(aPath, depth, (path, basicFileAttributes) -> {
                    File file = path.toFile();
                    return file.getName().contains(mask);
                });
        stream.forEach(e -> {
            try {
                queue.put(e);
                counter++;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}
