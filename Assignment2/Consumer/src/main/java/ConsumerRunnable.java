import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsumerRunnable implements Runnable{
    private String queueName;
    private Connection con;
    private final Gson gson = new Gson();
    public ConsumerRunnable(String queueName, Connection con){
        this.queueName = queueName;
        this.con = con;
    }
    @Override
    public void run() {
        try {
            Channel channel = con.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicQos(10);
            DeliverCallback deliverCallback = (consumerTag, delivery)->{
                String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(msg, JsonObject.class);
                Integer skierID = json.get("skierID").getAsInt();
                if(Consumer.record.containsKey(skierID)){
                    Consumer.record.get(skierID).add(json);
                }else{
                    List<JsonObject> newRecord = Collections.synchronizedList(new ArrayList<>());//new CopyOnWriteArrayList<>();
                    newRecord.add(json);
                    Consumer.record.put(skierID, newRecord);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println(Thread.currentThread().getId() + " - thread received " + json);
            };
            channel.basicConsume(this.queueName, false ,deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
