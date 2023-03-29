import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMQChannelPool {
  private final BlockingQueue<Channel> pool;
  private int poolSize;
  private RabbitMQChannelFactory factory;

  public RabbitMQChannelPool(int maxSize, RabbitMQChannelFactory factory) {
    this.poolSize = maxSize;
    this.pool = new LinkedBlockingQueue<>(maxSize);
    this.factory = factory;
    for (int i = 0; i < maxSize; i++) {
      try {
        Channel channel = factory.create();
        this.pool.put(channel);
      } catch (InterruptedException | IOException ex) {
        Logger.getLogger(RabbitMQChannelPool.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
  }

  public Channel borrowObject() throws IOException {
    try {
      return this.pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException("Error: no channels available" + e.toString());
    }
  }

  public void returnObject(Channel channel) throws Exception {
    if (channel != null) {
      this.pool.add(channel);
    }
  }
}
