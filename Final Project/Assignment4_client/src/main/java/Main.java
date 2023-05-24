import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Main {

  private static final int TOTAL_REQUESTS = 500000;
  public static final int NUM_THREADS = 200;
  private static BlockingQueue<SwipeData> buffer;
  private static AtomicInteger succCnt;
  private static AtomicInteger failCnt;
  private static CountDownLatch latch;
  private static BlockingQueue<Long> postLatencies;
  public static void main(String[] args) throws InterruptedException {
    buffer = new LinkedBlockingQueue<>();
    (new Thread(new Producer(buffer, TOTAL_REQUESTS, NUM_THREADS))).start();
    succCnt = new AtomicInteger(0);
    failCnt = new AtomicInteger(0);
    latch = new CountDownLatch(NUM_THREADS);
    postLatencies = new LinkedBlockingQueue<>();
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < NUM_THREADS; i++) {
      Thread thread = new Thread(new Consumer(buffer, succCnt, failCnt, latch, postLatencies));
      thread.start();
    }
    latch.await();
    long wallTime = (System.currentTimeMillis() - startTime) / 1000;
    long throughput = (succCnt.get() + failCnt.get()) / wallTime;
    System.out.println("Number of successful requests: " + succCnt.get());
    System.out.println("Number of unsuccessful requests: " + failCnt.get());
    System.out.println("The total run time (secs): " + wallTime);
    System.out.println("The total throughput in requests per second: " + throughput);
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (long latency : postLatencies) {
      stats.addValue(latency);
    }
    System.out.println("POSTs min response time (millisecs): " + stats.getMin());
    System.out.println("POSTs mean response time (millisecs): " + stats.getMean());
    System.out.println("POSTs max response time (millisecs): " + stats.getMax());
  }
}
