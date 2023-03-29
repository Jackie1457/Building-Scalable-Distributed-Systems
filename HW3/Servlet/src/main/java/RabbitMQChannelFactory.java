import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RabbitMQChannelFactory extends BasePooledObjectFactory<Channel> {
  private final Connection connection;
  private int count;

  public RabbitMQChannelFactory(Connection connection) {
    this.connection = connection;
    this.count = 0;
  }

  @Override
  synchronized public Channel create() throws IOException {
    count++;
    Channel channel = connection.createChannel(); // open a channel
    return channel;
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }

  public int getCount() {
    return count;
  }
}
