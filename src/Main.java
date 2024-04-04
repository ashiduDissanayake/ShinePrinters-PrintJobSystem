import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;

// PrintJob class
class PrintJob {
    private String fileName;
    private String fileType;
    private String content;

    public PrintJob(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public PrintJob(String fileName, String fileType, String content) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.content = content;
    }

    // Getters and setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "PrintJob{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }

}

// SharedQueue class
class SharedQueue {
    private BlockingQueue<PrintJob> queue;
    private int capacity;

    public SharedQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    // Enqueue a print job
    public void enqueue(PrintJob job) throws InterruptedException {
        queue.put(job);
    }


    // Dequeue a print job
    public PrintJob dequeue() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    public boolean isFull() {
        return queue.size() >= capacity;
    }
}

// TypeNotSupportedException class handler
class TypeNotSupportedException extends Exception {
    public TypeNotSupportedException(String message) {
        super(message);
    }
}

// Computer class
class Computer implements Runnable {
    private Queue<String> computerQueue;
    private SharedQueue sharedQueue;
    private List<String> supportedTypes = Arrays.asList("txt", "pdf", "png");

    // Constructor
    public Computer(Queue<String> computerQueue, SharedQueue sharedQueue) {
        this.computerQueue = computerQueue;
        this.sharedQueue = sharedQueue;
    }

    public void run() {
        while (!computerQueue.isEmpty()) {
            String file = computerQueue.poll();
            submitJob(file, sharedQueue);
        }
        // Ensure file10.pdf is submitted even if the queue is empty
        if (Thread.currentThread().getName().equals("Thread-0") && !sharedQueue.isFull()) {
            try {
                String file10 = "file10.pdf";
                submitJob(file10, sharedQueue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Submit a print job
    public synchronized void submitJob(String file, SharedQueue sharedQueue) {
        String fileName = file.split("\\.")[0];
        String fileType = file.split("\\.")[1];

        String thread_name = Thread.currentThread().getName();
        //match case
        switch (thread_name) {
            case "Thread-0":
                thread_name = "Computer 1";
                break;
            case "Thread-1":
                thread_name = "Computer 2";
                break;
            case "Thread-2":
                thread_name = "Computer 3";
                break;
        }

        // error handling
        try {
            if (supportedTypes.contains(fileType)) {
                PrintJob job = new PrintJob(fileName, fileType);
                sharedQueue.enqueue(job);
                System.out.println(thread_name + " submitted: " + job.getFileName());
            } else {
                throw new TypeNotSupportedException(" File type " + fileType + " is not supported.");
            }
        } catch (TypeNotSupportedException e) {
            System.out.println(thread_name + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Printer class
class Printer implements Runnable {
    private SharedQueue sharedQueue;
    private List<String> supportedTypes = Arrays.asList("txt", "pdf", "png");

    // Constructor
    public Printer(SharedQueue sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

    public void run() {
        while (sharedQueue!=null && sharedQueue.size() > 0){
            printJob(sharedQueue);
        }
    }

    // Print a job
    public synchronized void printJob(SharedQueue sharedQueue) {
        String thread_name = Thread.currentThread().getName();
        switch (thread_name) {
            case "Thread-3":
                thread_name = "Printer 1";
                break;
            case "Thread-4":
                thread_name = "Printer 2";
                break;
        }
        //error handling
        try {
            PrintJob job = sharedQueue.dequeue();
            String fileType = job.getFileType();
            if (supportedTypes.contains(fileType)) {
                System.out.println(thread_name + " printing: " + job.getFileName() + "." + job.getFileType());
            } else {
                throw new TypeNotSupportedException("File type " + fileType + " is not supported.");
            }
        } catch (TypeNotSupportedException e) {
            System.out.println(thread_name + " " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// TextFile class
class TextFile {
    private Queue<String> fileQueue;

    public TextFile() {
        fileQueue = new LinkedList<>();
    }

    public Queue<String> readAFile() throws IOException {
        // Create a new file queue
        fileQueue.clear();

        // Read file from BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader("webView.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileQueue.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

        return fileQueue;
    }
} 


// Main class
public class Main {

    // Game loop contain the main logic of the game
    public static void GameLoop(Scanner scanner){
        Queue<String> fileQueue = new LinkedList<>();
        SharedQueue sharedQueue = new SharedQueue(5);
        System.out.println();
        System.out.println("------------------------------------");
        System.out.println("Welcome to the Shine Printers");
        System.out.println("------------------------------------");
        System.out.println("Please select the mode");
        System.out.println("Enter (1) for  Physical mode");
        System.out.println("Enter (2) Web interface mode");
        System.out.println("Enter (3) to exit");
        System.out.println("------------------------------------");

        
        int mode = scanner.nextInt();

        if (mode == 1) {
            fileQueue = new LinkedList<>(Arrays.asList("file1.txt", "file2.pdf", "file3.png", "file4.jpg", "file5.txt", "file6.pdf", "file7.png", "file8.java", "file9.txt"));

        } else if (mode == 2) {
            TextFile textFile = new TextFile();
            try {
                fileQueue = textFile.readAFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
        else if (mode == 3){
            System.exit(0);
        
        }else {
            System.out.println("Invalid mode");
            GameLoop(scanner);
        }

        Thread[] computers = new Thread[3];
        int size = fileQueue.size();
        size = size > 3 ? size/3 : size;
        for (int i = 0; i < size; i++) {
            try
            {
                Queue<String> computerQueue = new LinkedList<>(Arrays.asList(fileQueue.poll(), fileQueue.poll(), fileQueue.poll()));
                computers[i] = new Thread(new Computer(computerQueue, sharedQueue));
                //computers[i].setPriority(Thread.MAX_PRIORITY);
                computers[i].start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
        }

        Thread[] printers = new Thread[2];
        for (int i = 0; i < 2; i++) {
            printers[i] = new Thread(new Printer(sharedQueue));
            //printers[i].setPriority(Thread.MIN_PRIORITY);
            printers[i].start();
        }
        for (int i = 0; i < 3; i++) {
            try {
                computers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 2; i++) {
            try {
                printers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All jobs are done");
        System.out.println();
        GameLoop(scanner);
    }

    // Main method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameLoop(scanner);
    }
}


