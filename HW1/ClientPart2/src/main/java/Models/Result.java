package Models;

import java.util.concurrent.atomic.AtomicInteger;

public class Result {
  private AtomicInteger successfulRequests;
  private AtomicInteger unsuccessfulRequests;
  private AtomicInteger totalRequests;

  public Result(AtomicInteger successfulRequests,
      AtomicInteger unsuccessfulRequests, AtomicInteger totalRequests) {
    this.successfulRequests = successfulRequests;
    this.unsuccessfulRequests = unsuccessfulRequests;
    this.totalRequests = totalRequests;
  }
  public void addSuccessfulRequests(int num) {
    this.successfulRequests.addAndGet(num);
  }

  public void addUnsuccessfulRequests(int num) {
    this.unsuccessfulRequests.addAndGet(num);
  }

  public void addTotalRequests(int num) {
    this.totalRequests.addAndGet(num);
  }

  public AtomicInteger getSuccessfulRequests() {
    return this.successfulRequests;
  }

  public AtomicInteger getUnsuccessfulRequests() {
    return this.unsuccessfulRequests;
  }

  public AtomicInteger getTotalRequests() {
    return this.totalRequests;
  }
}
