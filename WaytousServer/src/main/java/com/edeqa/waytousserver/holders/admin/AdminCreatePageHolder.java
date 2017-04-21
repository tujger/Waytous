package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;

import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 1/23/2017.
 */

@SuppressWarnings("unused")
public class AdminCreatePageHolder implements PageHolder {

    private final static String HOLDER_TYPE = "create";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminCreatePageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query,HttpExchange exchange) {
        this.html = html;
        html.clear();

        return html;
    }

}
