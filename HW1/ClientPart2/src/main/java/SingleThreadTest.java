import Models.DataGeneration;
import Models.Record;
import Models.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadTest {
  private final static int TOTAL_REQUESTS = 10000;

  public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();
    BlockingQueue<DataGeneration> buffer = new LinkedBlockingQueue<>();

    Producer producer = new Producer(TOTAL_REQUESTS, buffer); // instantiate a producer
    Thread producerThread = new Thread(producer);
    producerThread.start();

    AtomicInteger successfulRequests = new AtomicInteger(0);
    AtomicInteger unsuccessfulRequests = new AtomicInteger(0);
    AtomicInteger totalRequests = new AtomicInteger(0);
    Result result = new Result(successfulRequests, unsuccessfulRequests, totalRequests);
    CountDownLatch countDownLatch = new CountDownLatch(1); // single thread test
    List<Record> records = new ArrayList<>();
    Consumer consumer = new Consumer(buffer, result, countDownLatch, TOTAL_REQUESTS, records); // instantiate a consumer
    Thread consumerThreads = new Thread(consumer); // instantiate a consumer thread
    consumerThreads.start();
    consumerThreads.join(); // A simple and robust mechanism for one thread to wait until another has completed its work is to use the join() method

    long end = System.currentTimeMillis();
    long wallTime = (end - start) / 1000;
    long throughput = result.getTotalRequests().get() / wallTime;
    System.out.println("The number of successful requests sent: " + result.getSuccessfulRequests().get());
    System.out.println("The number of unsuccessful requests sent: " + result.getUnsuccessfulRequests().get());
    System.out.println("The total run time (wall time) for all threads to complete: " + wallTime +" seconds");
    System.out.println("The total throughput in requests per second: " + throughput);
  }
}
