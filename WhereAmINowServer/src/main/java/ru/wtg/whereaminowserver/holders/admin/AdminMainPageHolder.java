package ru.wtg.whereaminowserver.holders.admin;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminHandler;

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

/**
 * Created 1/20/2017.
 */

public class AdminMainPageHolder implements PageHolder {

    public static final String HOLDER_TYPE = "main";

    @SuppressWarnings("unused")
    private final MyHttpAdminHandler server;
    private HtmlGenerator html;
    private String part;
    private ArrayList<String> request;

    public AdminMainPageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
        this.html = html;
        html.clear();

        html.getHead().add(TITLE).with("Admin");

        JSONObject o = new JSONObject();
        o.put("page", part);
        o.put("request", request);
        o.put("version", SERVER_BUILD);

        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

        return html;
    }

    public void addPart(String s) {
        part = s;
    }
    public void addRequest(ArrayList<String> s) {
        request = s;
    }
}