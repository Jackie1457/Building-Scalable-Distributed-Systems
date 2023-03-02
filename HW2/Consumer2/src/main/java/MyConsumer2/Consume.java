package MyConsumer2;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class Consume {
  private final static Integer NUM_OF_THREADS = 20;

  public static void main(String[] args) throws IOException, TimeoutException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setPort(5672);
    factory.setHost("54.184.113.242");
    factory.setUsername("test");
    factory.setPassword("test");
//    factory.setHost("localhost");
//    factory.setUsername("guest");
//    factory.setUsername("guest");

    Connection connection = factory.newConnection();
    HashMap<String, ArrayList<String>> numOfUsersSwipeRightOn = new HashMap<>();


    for(int i = 0; i < NUM_OF_THREADS; i++) {
      Thread thread = new Thread(new RabbitMQChannel(connection, numOfUsersSwipeRightOn));
      thread.start();
    }
  }
}
