package servlets;//about package name, you can see https://stackoverflow.com/questions/65703840/tomcat-casting-servlets-to-javax-servlet-servlet-instead-of-jakarta-servlet-http and https://xingez.me/tomcat10-issue/
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import constants.Message;
import constants.SkierNum;
import io.swagger.client.model.*;

import constants.Constant;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author jeromy zhang
 * servlets.ResortsServlet extends http servlet
 * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.16#/
 */
@WebServlet(name = "ResortsServlet", value = "ResortsServlet")
public class ResortsServlet extends HttpServlet {
    private final Gson gson = new Gson();
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
            try(Jedis jedis = jedisPool.getResource()){

                String key = "day" + "\""+ urlSplit[5]+ "\"";
                String[] resorts = jedis.hget(key, "resortID").split("\\|");
                String[] seasons = jedis.hget(key, "seasonID").split("\\|");
                String[] skiers = jedis.hget(key, "skierID").split("\\|");
                HashSet<String> skierSet = new HashSet<String>();
                for (int i = 0; i < resorts.length; i++) {
                    if(resorts[i].equals("\"" + urlSplit[1] + "\"") && seasons[i].equals("\"" + urlSplit[3] + "\"")){
                        skierSet.add(skiers[i]);
                    }
                }
                res.getWriter().write(String.valueOf(skierSet.size()));
            }catch(NullPointerException e){
                res.getWriter().write("no data found!");
            }
            res.setStatus(HttpServletResponse.SC_OK);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType(Constant.CONTENT_TYPE);
        if(req.getPathInfo() == null) return;
        String[] urlPath = req.getPathInfo().split(Constant.SPLIT);
        // check we have a URL!
        if (urlPath.length != Constant.THREE || !isUrlValid(urlPath)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new Message(Constant.INVALID_PATH)));
            return;
        }
        JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

        if (body.get(Constant.YEAR) == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new Message(Constant.MISS_PARAM)));
            return;
        }

        res.setStatus(HttpServletResponse.SC_CREATED);
    }
    private boolean isUrlValid(String[] urlPath) {
        if(urlPath.length == 7){
            return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].equals(Constant.SEASONS) &&
                    urlPath[3].chars().allMatch(Character::isDigit) && urlPath[4].equals(Constant.DAYS) &&
                    urlPath[5].chars().allMatch(Character::isDigit) && urlPath[6].equals(Constant.SKIERS) &&
                    Integer.parseInt(urlPath[5]) >= 1 && Integer.parseInt(urlPath[5])<=Constant.DAY_COUNTS;
        }
        return false;
    }
}