import Models.DataGeneration;
import Models.Result;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadsTest {
  private final static int NUM_THREADS = 200;
  private final static int TOTAL_REQUESTS_500K = 500000;
  private final static int PER_THREAD_REQUESTS = 2500;

  public static void main(String[] args) throws InterruptedException {
    BlockingQueue<DataGeneration> buffer = new LinkedBlockingQueue<>();
    Producer producer = new Producer(TOTAL_REQUESTS_500K, buffer);
    long start = System.currentTimeMillis();
    new Thread(producer).start();
    CountDownLatch completed = new CountDownLatch(NUM_THREADS); // count down threads

    AtomicInteger successfulRequests = new AtomicInteger(0);
    AtomicInteger unsuccessfulRequests = new AtomicInteger(0);
    AtomicInteger totalRequests = new AtomicInteger(0);
    Result result = new Result(successfulRequests, unsuccessfulRequests, totalRequests);
    for (int i = 0; i < NUM_THREADS; i++){
      Consumer consumer = new Consumer(buffer, result, completed, PER_THREAD_REQUESTS);
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
  }
}
