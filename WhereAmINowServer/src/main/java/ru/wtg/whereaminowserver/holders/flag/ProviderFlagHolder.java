package ru.wtg.whereaminowserver.holders.flag;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.FlagHolder;
import ru.wtg.whereaminowserver.servers.AbstractWainProcessor;
import ru.wtg.whereaminowserver.servers.MyWsServer;

/**
 * Created 1/19/17.
 */

public class ProviderFlagHolder implements FlagHolder {

    public static final String TYPE = "provider";

    public ProviderFlagHolder(AbstractWainProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("PROVIDERFLAGHOLDER:" + request);
        return true;
    }


}