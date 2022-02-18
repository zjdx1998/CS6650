package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import constants.Constant;
import constants.Message;
import io.swagger.client.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkiersServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private SkierVertical skierVertical = new SkierVertical().
            addResortsItem(new SkierVerticalResorts().seasonID(Constant.SUMMER).totalVert(Constant.THREE))
            .addResortsItem(new SkierVerticalResorts().seasonID(Constant.SPRING).totalVert(Constant.SEVEN));

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
            res.setStatus(HttpServletResponse.SC_OK);
            if(urlSplit.length == 3) res.getWriter().write(gson.toJson(skierVertical));
            else res.getWriter().write(gson.toJson(Constant.ID));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        if(req.getPathInfo() == null) return;
        String[] urlPath = req.getPathInfo().split(Constant.SPLIT);
        if (urlPath.length != 8) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new Message(Constant.INVALID_PATH)));
            return;
        }
        JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

        for (String param : Constant.SKIER_POST) {
            if (body.get(param) == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(gson.toJson(new Message(Constant.MISS_PARAM)));
                return;
            }
        }
        res.setStatus(HttpServletResponse.SC_CREATED);
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
