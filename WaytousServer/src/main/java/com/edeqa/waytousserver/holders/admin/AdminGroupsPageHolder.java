package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 1/20/2017.
 */

@SuppressWarnings("unused")
public class AdminGroupsPageHolder implements PageHolder {

    private static final String HOLDER_TYPE = "groups";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminGroupsPageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query,HttpExchange exchange) {
        this.html = html;
        html.clear();

        return html;
    }

    private boolean processQuery(Map<String, List<String>> query) {
        boolean processed = false;
        if(query.containsKey("action")){
            for(String x:query.get("action")){
                processAction(x,query);
                processed = true;
            }
        }
        return processed;
    }

    private void processAction(String action, Map<String, List<String>> query) {

        if("del".equals(action)){

            String token=null,id=null;

            if(query.containsKey("token")) token = query.get("token").get(0);
            if(query.containsKey("id")) id = query.get("id").get(0);

            server.getDataProcessor().removeUser(token,id);

        }

    }

}
