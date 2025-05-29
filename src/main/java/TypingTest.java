import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TypingTest {

    private static String lastInput = "";
    private static Scanner scanner = new Scanner(System.in);
    private static List<Boolean> isCorrect = new ArrayList<>();
    private static boolean hasInput = false;
    private final static Lock lock = new ReentrantLock();
    public static class InputRunnable implements Runnable {


        @Override
        public void run() {
            try {
                String input = scanner.nextLine();
                lock.lock();
                try {
                    lastInput = input;
                    hasInput = true;
                }
                finally {
                    lock.unlock();
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void testWord(String wordToTest) {
        try {
            lock.lock();
            try {
                lastInput = "";
                hasInput = false;
            } finally {
                lock.unlock();
            }
            System.out.println(wordToTest);
            lastInput = "";

            Thread thread = new Thread(new InputRunnable());
            thread.start();

            long startTime = System.currentTimeMillis();
            boolean inputExists = false;
            while (System.currentTimeMillis() - startTime < 5000) {
                lock.lock();
                try {
                    if (hasInput) {
                        inputExists = true;
                        break;
                    }
                } finally {
                    lock.unlock();}
                Thread.sleep(100);
            }
            lock.lock();
            try {
                System.out.println("You typed: " + lastInput);
                if (lastInput.equalsIgnoreCase(wordToTest) && inputExists) {
                    System.out.println("Correct!");
                    isCorrect.add(true);
                } else {
                    System.out.println(inputExists ? "Incorrect!" : "Time's up!");
                    isCorrect.add(false);
                }
            } finally {
                lock.unlock();
            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void typingTest(List<String> inputList) throws InterruptedException {

        for (String wordToTest : inputList) {
            testWord(wordToTest);
            Thread.sleep(2000); // Pause briefly before showing the next word
        }

        for(int i = 0; i < inputList.size(); i++) {
            if (isCorrect.get(i)) {
                System.out.println(inputList.get(i) + " is correct");
            }
            else {
                System.out.println(inputList.get(i) + " is not correct");
            }
        }
    }

    public static List<String> readFromFile(String filePath) {
        List<String> words = new ArrayList<>();
        URL resource = TypingTest.class.getClassLoader().getResource(filePath);
        if (resource == null) {
            throw new RuntimeException("File not found: " + filePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return words;
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> words = readFromFile("Words.txt");

        typingTest(words);

        System.out.println("Press enter to exit.");
    }
}