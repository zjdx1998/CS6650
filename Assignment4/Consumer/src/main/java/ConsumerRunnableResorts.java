import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.apache.log4j.BasicConfigurator;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.logging.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConsumerRunnableResorts implements Runnable{
    private String queueName;
    private Connection con;
    private JedisPool jedisPool;
    private final Gson gson = new Gson();
    public ConsumerRunnableResorts(JedisPool jedisPool, String queueName, Connection con){
        this.jedisPool = jedisPool;
        this.queueName = queueName;
        this.con = con;
        BasicConfigurator.configure();
    }
    @Override
    public void run() {
        try {
            Channel channel = con.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicQos(1);
            DeliverCallback deliverCallback = (consumerTag, delivery)->{
                String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                storeMessages(msg);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                Logger.getLogger(ConsumerRunnable.class.getName()).log(Level.INFO,Thread.currentThread().getId() + " - thread received " + msg);
            };
            channel.basicConsume(this.queueName, false ,deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            Logger.getLogger(ConsumerRunnable.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private void storeMessages(String msg){
        try(Jedis jedis = jedisPool.getResource()){
            JsonObject json = gson.fromJson(msg, JsonObject.class);
            String skierId = String.valueOf(json.get("skierID"));
            String seasonId = String.valueOf(json.get("seasonID"));
            String dayId = String.valueOf(json.get("dayID"));
            String liftId = String.valueOf(json.get("liftID"));
            String resortId = String.valueOf(json.get("resortID"));
            String time = String.valueOf(json.get("time"));
            String key = "day" + dayId;
            Map<String, String> current = jedis.hgetAll(key);
            current.put("seasonID", current.getOrDefault("seasonID", "") + "|" + seasonId);
            current.put("resortID", current.getOrDefault("resortID", "") + "|" + resortId);
            current.put("skierID", current.getOrDefault("skierID", "") + "|" + skierId);
            current.put("liftID", current.getOrDefault("liftID", "") + "|" + liftId);
            current.put("time", current.getOrDefault("time", "") + "|" + time);
            jedis.hmset(key, current);
        }
    }
}
