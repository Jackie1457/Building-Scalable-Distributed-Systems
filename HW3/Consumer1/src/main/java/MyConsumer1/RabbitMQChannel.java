package MyConsumer1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RabbitMQChannel implements Runnable{
  private final Connection connection;
  private JedisPool pool;
  private static final String QUEUE_NAME = "TWINDER_DATA";
  private static final String EXCHANGE_NAME = "EXCHANGE";

  public RabbitMQChannel(Connection connection, JedisPool pool) {
    this.connection = connection;
    this.pool = pool;
  }

  @Override
  public void run(){

      try{
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "fanout");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicQos(1); // Per consumer limit, receive a maximum of 10 unacknowledged messages at once,accept only 1 unacknowledged message
        Gson gson = new Gson();
        DeliverCallback threadCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          JsonObject json = gson.fromJson(message, JsonObject.class);
          String leftOrRight = String.valueOf(json.get("leftorright"));
          String swiper = String.valueOf(json.get("swiper"));
          swiper = swiper.substring(1, swiper.length() - 1);
          String swipee = String.valueOf(json.get("swipee"));
          String comment = String.valueOf(json.get("comment"));
          Jedis jedis = null;
          try {
            synchronized (pool) {
              jedis = this.pool.getResource();
              Map<String, String> numOfLikesDisLikes =
                  jedis.exists(swiper) ? jedis.hgetAll(swiper) : new HashMap<>();
              if (numOfLikesDisLikes.containsKey("numLikes")) {
                int rightCount = Integer.parseInt(jedis.hget(swiper, "numLikes"));
                jedis.hset(swiper, "numLikes", String.valueOf(rightCount + 1));
              } else {
                jedis.hset(swiper, "numLikes", "1");
              }
              if (numOfLikesDisLikes.containsKey("numDislikes")) {
                int leftCount = Integer.parseInt(jedis.hget(swiper, "numDislikes"));
                jedis.hset(swiper, "numDislikes", String.valueOf(leftCount + 1));
              } else {
                jedis.hset(swiper, "numDislikes", "1");
              }
            }
          }catch (Exception e) {
            System.out.println("Error while handling Redis connection: " + e.getMessage());
          }finally {
            if (jedis != null)
              jedis.close();
          }
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, threadCallback, consumerTag -> { });
      } catch (IOException e) {
        e.printStackTrace();
      }
  }
}

