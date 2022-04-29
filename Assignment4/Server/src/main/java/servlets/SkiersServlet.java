package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import constants.Constant;
import constants.Message;
import io.swagger.client.model.*;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author jeromy zhang
 * @date Feb-17-22
 */
@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkiersServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private SkierVertical skierVertical = new SkierVertical().
            addResortsItem(new SkierVerticalResorts().seasonID(Constant.SUMMER).totalVert(Constant.THREE))
            .addResortsItem(new SkierVerticalResorts().seasonID(Constant.SPRING).totalVert(Constant.SEVEN));

    private ConnectionFactory conFactory = new ConnectionFactory();
    private EventCountCircuitBreaker breaker;
    public final Integer NUM_CHANNEL = 20;
    private BlockingQueue<Channel> channelPool;
    private static JedisPool jedisPool;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("rabbitmq.conf");
            assert is != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String ip = reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            jedisPool = new JedisPool(new JedisPoolConfig(), ip, Integer.parseInt(reader.readLine()), 10000, "CS6650cs6650");
        } catch (IOException e) {
            e.printStackTrace();
        }
        File confFile = new File(this.getClass()
                .getClassLoader().getResource("rabbitmq.conf").getFile());
        try {
            Scanner cin = new Scanner(confFile);
            String ip = cin.nextLine();
            conFactory.setHost(ip);
            conFactory.setPort(Integer.parseInt(cin.nextLine()));
            conFactory.setUsername(cin.nextLine());
            conFactory.setPassword(cin.nextLine());
            //jedisPool = new JedisPool(new JedisPoolConfig(), "172.31.5.13", 6379, 1000, "CS6650cs6650");
            Channel channel = conFactory.newConnection().createChannel();
            channel.queueDeclare(Constant.QUEUE_NAME, false, false, false, null);
            channelPool = new LinkedBlockingQueue<>();
            for(int i=0; i<NUM_CHANNEL; i++) channelPool.add(channel);
            breaker = new EventCountCircuitBreaker(500, 5, TimeUnit.SECONDS, 300);
            System.out.println("Connecting...");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType(Constant.CONTENT_TYPE);
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new Message(Constant.MISS_PARAM)));
            return;
        }
        String[] urlSplit = urlPath.split(Constant.SPLIT);
        if (!isUrlValid(urlSplit)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new Message(Constant.INVALID_PATH)));
        }else{
            try(Jedis jedis = jedisPool.getResource()) {
                if (urlSplit.length==3) {
                    String[] verticals = jedis.hget(urlSplit[1], "vertical").split("\\|");
                    String[] seasons = jedis.hget(urlSplit[1], "seasonID").split("\\|");
                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                    for (int i = 1; i < verticals.length; i++) {
                        map.put(seasons[i], map.getOrDefault(seasons[i], 0) + Integer.parseInt(verticals[i]));
                    }
                    JsonObject msg = new JsonObject();
                    JsonObject subMsg = new JsonObject();
                    for (String key : map.keySet()) {
                        subMsg.add(key, new JsonPrimitive(map.get(key)));
                    }
                    msg.add("resort", subMsg);
                    res.getWriter().write(gson.toJson(msg));
                } else {
                    String[] vertical = jedis.hget(urlSplit[7], "vertical").split("\\|");
                    String[] seasons = jedis.hget(urlSplit[7], "seasonID").split("\\|");
                    String[] days = jedis.hget(urlSplit[7], "dayID").split("\\|");
                    int total_vertical = 0;
                    for (int i = 1; i < vertical.length; i++) {
                        if (days[i].equals("\"" + urlSplit[5] + "\"") && seasons[i].equals("\"" + urlSplit[3] + "\"")){
                            total_vertical += Integer.parseInt(vertical[i]);
                        }
                    }
                    res.getWriter().write(String.valueOf(total_vertical));
                }
            }catch(NullPointerException e){
                res.getWriter().write("no data found!");
            }
            res.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        if(req.getPathInfo() == null) return;
        String[] urlPath = req.getPathInfo().split(Constant.SPLIT);
        if (urlPath.length != 8 || !isUrlValid(urlPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new Message(Constant.INVALID_PATH)));
            return;
        }
        LiftRide liftRide = gson.fromJson(req.getReader(), LiftRide.class);
        int skierID = Integer.parseInt(urlPath[7]);
        JsonObject msg = new JsonObject();
        msg.add("time", new JsonPrimitive(liftRide.getTime()));
        msg.add("liftID", new JsonPrimitive(liftRide.getLiftID()));
        msg.add("waitTime", new JsonPrimitive(liftRide.getWaitTime()));
        msg.add("skierID", new JsonPrimitive(skierID));
        msg.add("dayID", new JsonPrimitive(urlPath[5]));
        msg.add("seasonID", new JsonPrimitive(urlPath[3]));
        msg.add("resortID", new JsonPrimitive(urlPath[1]));
        msg.add("vertical", new JsonPrimitive(liftRide.getLiftID() * 10));
        Channel channel = null;
        try {
            channel = channelPool.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(channel != null) {
            while(!breaker.checkState());
            channel.basicPublish("", Constant.QUEUE_NAME, null, msg.toString().getBytes());
            res.setStatus(HttpServletResponse.SC_CREATED);
            System.out.println("Sent " + msg + " to rabbitmq");
            res.getWriter().write("Sent " + msg + " to rabbitmq");
            channelPool.add(channel);
            breaker.incrementAndCheckState();
        } else{
            res.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        if(urlPath.length == 3){
            return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].contains(Constant.VERTICAL);
        }else if(urlPath.length == 8){
            return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].equals(Constant.SEASONS) &&
                    urlPath[3].chars().allMatch(Character::isDigit) && urlPath[4].equals(Constant.DAYS + "s") &&
                    urlPath[5].chars().allMatch(Character::isDigit) && urlPath[6].equals(Constant.SKIERS) &&
                    urlPath[7].chars().allMatch(Character::isDigit) && Integer.parseInt(urlPath[5]) >= 1 &&
                    Integer.parseInt(urlPath[5])<=Constant.DAY_COUNTS;
        }
        return false;
    }
}
