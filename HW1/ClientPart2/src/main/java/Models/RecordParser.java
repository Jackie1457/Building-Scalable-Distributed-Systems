package Models;

import Models.Record;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordParser {
  private List<Record> records;
  private static final String filePath = "records.csv";

  public RecordParser(List<Record> records) {
    this.records = records;
  }

  public void writeToCSV() throws IOException {
    try {
      FileWriter writer = new FileWriter(filePath);
      writer.append("startTime,requestType,latency,responseCode\n");
      for (int i = 0; i < records.size(); i++){
        writer.append(records.get(i).toCSV());
      }
      writer.flush();
      writer.close();
    }catch (IOException e){
      System.out.println("Write Data to CSV file failed!");
    }
  }

  public long calculateMeanResponseTime() {
    long latency = 0;
    for (int i = 0; i < records.size(); i++) {
      latency += records.get(i).getLatency();
    }
    return latency / records.size();
  }

  public List<Long> calculateSortedResponseTime() {
    List<Long> totalResponseTime = new ArrayList<>();
    for (int i = 0; i < records.size(); i++) {
      totalResponseTime.add(records.get(i).getLatency());
    }
    Collections.sort(totalResponseTime);
    return totalResponseTime;
  }

  public long calculateMedianResponseTime() {
    List<Long> totalResponseTime = calculateSortedResponseTime();
    if (totalResponseTime.size() % 2 == 0) {
      return (totalResponseTime.get(totalResponseTime.size() / 2 - 1) +
              totalResponseTime.get(totalResponseTime.size() / 2)) / 2;
    } else {
      return totalResponseTime.get(totalResponseTime.size() / 2);
    }
  }

  public long calculateP99ResponseTime() {
    List<Long> totalResponseTime = calculateSortedResponseTime();
    int index = (int) Math.ceil(totalResponseTime.size() * 0.99);
    return totalResponseTime.get(index);
  }

  public long calculateMinResponseTime() {
    List<Long> totalResponseTime = calculateSortedResponseTime();
    return totalResponseTime.get(0);
  }

  public long calculateMaxResponseTime() {
    List<Long> totalResponseTime = calculateSortedResponseTime();
    return totalResponseTime.get(totalResponseTime.size() - 1);
  }
}
