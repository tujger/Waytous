package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 1/20/2017.
 */

public class AdminMainHolder implements PageHolder {

    public static final String HOLDER_TYPE = "main";

    @SuppressWarnings("unused")
    private final MyHttpAdminHandler server;
    private HtmlGenerator html;
    private String part;
    private ArrayList<String> request;

    public AdminMainHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(HttpExchange parts) {
        return false;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query,HttpExchange exchange) {
        this.html = html;
        html.clear();

        html.getHead().add(TITLE).with("Admin");

        JSONObject o = new JSONObject();
        o.put("page", part);
        o.put("version", SERVER_BUILD);
        o.put("HTTP_PORT", SENSITIVE.getHttpPort());
        o.put("HTTPS_PORT", SENSITIVE.getHttpsPort());
        o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
        o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
        o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
        o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
        o.put("firebase_config", SENSITIVE.getFirebaseConfig());

        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

        return html;
    }

}