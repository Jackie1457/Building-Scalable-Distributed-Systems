import Models.DataGeneration;
import Models.Record;
import Models.RecordParser;
import Models.Result;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadsTest {
  private final static int NUM_THREADS = 100;
  private final static int TOTAL_REQUESTS_500K = 500000;
  private final static int PER_THREAD_REQUESTS = 5000;

  public static void main(String[] args) throws InterruptedException, IOException {
    BlockingQueue<DataGeneration> buffer = new LinkedBlockingQueue<>();
    long start = System.currentTimeMillis();
    Producer producer = new Producer(TOTAL_REQUESTS_500K, buffer);
    new Thread(producer).start();
    CountDownLatch completed = new CountDownLatch(NUM_THREADS); // count down threads

    AtomicInteger successfulRequests = new AtomicInteger(0);
    AtomicInteger unsuccessfulRequests = new AtomicInteger(0);
    AtomicInteger totalRequests = new AtomicInteger(0);
    Result result = new Result(successfulRequests, unsuccessfulRequests, totalRequests);
    List<Record> records = new ArrayList<>();
    for (int i = 0; i < NUM_THREADS; i++){
      Consumer consumer = new Consumer(buffer, result, completed, PER_THREAD_REQUESTS, records);
      Thread consumerThreads = new Thread(consumer);
      consumerThreads.start();
    }
    completed.await(); // wait until all threads complete
    long end = System.currentTimeMillis();
    long wallTime = (end - start) / 1000;
    long throughput = result.getTotalRequests().get() / wallTime;
    System.out.println("The number of successful requests sent: " + result.getSuccessfulRequests().get());
    System.out.println("The number of unsuccessful requests sent: " + result.getUnsuccessfulRequests().get());
    System.out.println("The total run time (wall time) for all threads to complete: " + wallTime + " seconds.");
    System.out.println("The total throughput in requests per second: " + throughput);

    RecordParser recordParser = new RecordParser(records);
    recordParser.writeToCSV();
    System.out.println("\nRecords:");
    System.out.println("Mean response time (millisecs) is: " + recordParser.calculateMeanResponseTime());
    System.out.println("Median response time (millisecs) is: " + recordParser.calculateMedianResponseTime());
    System.out.println("Throughput(requests/second) is: " + throughput);
    System.out.println("p99 response time(millisecs) is: " + recordParser.calculateP99ResponseTime());
    System.out.println("Min response time(millisecs) is: " + recordParser.calculateMinResponseTime());
    System.out.println("Max response time(millisecs) is: " + recordParser.calculateMaxResponseTime());
  }
}
