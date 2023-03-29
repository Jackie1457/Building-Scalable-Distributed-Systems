package MyConsumer1;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Consume {
  private final static Integer NUM_OF_THREADS = 100;

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("52.13.18.92");
//    factory.setUsername("test");
//    factory.setPassword("test");
    factory.setPort(5672);
    factory.setHost("localhost");
    factory.setUsername("guest");
    factory.setUsername("guest");

    Connection connection = factory.newConnection();

    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(1024);
//    config.setMinIdle(100);
//    config.setMaxWaitMillis(1000);
    JedisPool pool = new JedisPool(config, "localhost", 6379, 5000);
//    JedisPool pool = new JedisPool(config, "52.35.159.86", 6379);

    for(int i = 0; i < NUM_OF_THREADS; i++) {
      Thread thread = new Thread(new RabbitMQChannel(connection, pool));
      thread.start();
    }
  }
}
