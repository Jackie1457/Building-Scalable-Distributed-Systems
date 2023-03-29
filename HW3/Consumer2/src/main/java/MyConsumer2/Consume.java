package MyConsumer2;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Consume {
  private final static Integer NUM_OF_THREADS = 100;

  public static void main(String[] args) throws IOException, TimeoutException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setPort(5672);
//    factory.setHost("54.218.249.163");
//    factory.setUsername("test");
//    factory.setPassword("test");
    factory.setHost("localhost");
    factory.setUsername("guest");
    factory.setUsername("guest");

    Connection connection = factory.newConnection();
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(1024);
    JedisPool pool = new JedisPool(config, "localhost", 6379);
//    JedisPool pool = new JedisPool(config, "34.213.62.172", 6379);

    for(int i = 0; i < NUM_OF_THREADS; i++) {
      Thread thread = new Thread(new RabbitMQChannel(connection, pool));
      thread.start();
    }
  }
}
