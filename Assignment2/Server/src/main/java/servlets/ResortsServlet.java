package servlets;//about package name, you can see https://stackoverflow.com/questions/65703840/tomcat-casting-servlets-to-javax-servlet-servlet-instead-of-jakarta-servlet-http and https://xingez.me/tomcat10-issue/
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import constants.Message;
import constants.SkierNum;
import io.swagger.client.model.*;

import constants.Constant;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author jeromy zhang
 * servlets.ResortsServlet extends http servlet
 * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.16#/
 */
@WebServlet(name = "ResortsServlet", value = "ResortsServlet")
public class ResortsServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private ResortsList resortsList = new ResortsList().addResortsItem(new ResortsListResorts().resortID(Constant.ID).resortName(Constant.HELLO));
    private SeasonsList seasonsList = new SeasonsList().addSeasonsItem(Constant.SPRING).addSeasonsItem(Constant.SUMMER);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType(Constant.CONTENT_TYPE);
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty() || urlPath.equals(Constant.SPLIT)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(gson.toJson(resortsList));
            return;
        }

        String[] urlParts = urlPath.split(Constant.SPLIT);
        if(!isUrlValid(urlParts)){
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new Message(Constant.INVALID_PATH)));
        }else{
            res.setStatus(HttpServletResponse.SC_OK);
            if(urlParts.length == Constant.THREE){
                res.getWriter().write(gson.toJson(seasonsList));
            }else{
                res.getWriter().write(gson.toJson(new SkierNum(Constant.SKIER_NUM_TIME, Constant.SKIER_NUM_SKIERS)));
            }
        }
    }

    private boolean isUrlValid(String[] urlPath){
        if(urlPath.length == 3) {
            return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].equals(Constant.SEASONS);
        }else if(urlPath.length == 7){
            return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].equals(Constant.SEASONS) &&
            (urlPath[3].equals(Constant.SPRING) || urlPath[3].toLowerCase().equals(Constant.SUMMER.toLowerCase())) && urlPath[4].equals(Constant.DAYS) &&
                    urlPath[5].chars().allMatch(Character::isDigit) && Integer.parseInt(urlPath[5]) >= 1 &&
                    Integer.parseInt(urlPath[5])<=Constant.DAY_COUNTS && urlPath[6].equals(Constant.SKIERS);
        }
        return false;
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
        res.getWriter().write(gson.toJson(seasonsList.addSeasonsItem(body.get(Constant.YEAR).toString())));
    }
}