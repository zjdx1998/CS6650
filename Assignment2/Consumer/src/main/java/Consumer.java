import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.*;

public class Consumer {
    private ConnectionFactory conFactory;
    private Connection con;
    private Integer numThreads = 10;
    private String queueName;
    public static final Map<Integer, List<JsonObject>> record = new ConcurrentHashMap<>();
    Consumer(){
        conFactory = new ConnectionFactory();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("rabbitmq.conf");
        assert is != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        System.out.println("Connecting...");
        try {
            if(!reader.ready()) return;
            conFactory.setHost(reader.readLine());
            conFactory.setPort(Integer.parseInt(reader.readLine()));
            conFactory.setUsername(reader.readLine());
            conFactory.setPassword(reader.readLine());
            setNumThreads(Integer.valueOf(reader.readLine()));
            setQueueName(reader.readLine());
            con = conFactory.newConnection();
            System.out.println("Connect successfully");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        ExecutorService pool = Executors.newFixedThreadPool(consumer.getNumThreads());
        for(int i=0; i< consumer.getNumThreads(); i++)
            pool.execute(new ConsumerRunnable(consumer.getQueueName(), consumer.getCon()));
    }

    public Integer getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(Integer numThreads) {
        this.numThreads = numThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
}
