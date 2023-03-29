import Models.DataGeneration;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class GetThread implements Runnable {
  private CountDownLatch completed;
  private final static String BASE_PATH = "http://localhost:8080/Servlet_war_exploded/";
//  private final static String BASE_PATH = "http://35.155.185.142:8080/Servlet_war/";

  private volatile boolean running = true;
  private final CountDownLatch postingThreadsStarted;
  public GetThread(CountDownLatch completed,
      CountDownLatch postingThreadsStarted) {
    this.completed = completed;
    this.postingThreadsStarted = postingThreadsStarted;
  }

  @Override
  public void run() {
    try {
      postingThreadsStarted.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    MatchesApi matchesApiInstance = new MatchesApi(apiClient);
    StatsApi statsApiInstance = new StatsApi(apiClient);

    ArrayList<Long> latencies = new ArrayList<>();

    while (running) {
      try {
        for (int i = 0; i < 5; i++) {
          long startTime = System.currentTimeMillis();
          String randomSwiper = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5000));
          ApiResponse<MatchStats> statsApiResponse = statsApiInstance.matchStatsWithHttpInfo(randomSwiper);
          ApiResponse<Matches> matchesApiResponse = matchesApiInstance.matchesWithHttpInfo(randomSwiper);

          long endTime = System.currentTimeMillis();
          long latency = endTime - startTime;
          latencies.add(latency);
        }
        Thread.sleep(1000);
      } catch (ApiException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    if (!latencies.isEmpty()) {
      Collections.sort(latencies);
      long minLatency = latencies.get(0);
      long maxLatency = latencies.get(latencies.size() - 1);
      long sumLatency = 0;
      for (Long latency : latencies) {
        sumLatency += latency;
      }
      double meanLatency = (double) sumLatency / latencies.size();

      System.out.println("Min latency: " + minLatency + " nanoseconds");
      System.out.println("Max latency: " + maxLatency + " nanoseconds");
      System.out.println("Mean latency: " + meanLatency + " nanoseconds");
    } else {
      System.out.println("No latencies were recorded.");
    }
  }

  public void terminate() {
    running = false;
  }
}

