package com.edeqa.waytous.holders;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Callable1;

import org.json.JSONObject;

import java.util.Date;


import static com.edeqa.waytous.State.EVENTS.SELECT_USER;
import static com.edeqa.waytous.State.EVENTS.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
@SuppressWarnings("WeakerAccess")
public class AddressViewHolder extends AbstractViewHolder<AddressViewHolder.AddressView> {
    
    private static final String TYPE = "address";
    private Callable1<String> callback;

    public AddressViewHolder(final MainActivity context) {
        super(context);
        setCallback(new Callable1<String>() {
            @Override
            public void call(String text) {
                if(context.getSupportActionBar() != null) {
                    context.getSupportActionBar().setSubtitle(text);
                }
            }
        });
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AddressView create(MyUser myUser) {
        if (myUser == null) return null;
        return new AddressView(myUser);
    }

    public AddressViewHolder setCallback(Callable1<String> callback) {
        this.callback = callback;
        return this;
    }

    private void setTitle(final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                callback.call(text);
            }
        });
    }

    class AddressView extends AbstractView {
        private long lastRequestTimestamp;

        AddressView(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            resolveAddress(location);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                case UNSELECT_USER:
                    if(State.getInstance().getUsers().getCountAllSelected() > 1){
                        callback.call(null);
                        return true;
                    } else {
                        callback.call("...");
                        onChangeLocation(myUser.getLocation());
                    }
                    break;
            }
            return true;
        }

        private void resolveAddress(final Location location) {
            if(!myUser.getProperties().isSelected() || location == null || State.getInstance().getUsers().getCountAllSelected() > 1){
                return;
            }
            long currentTimestamp = new Date().getTime();
            if(currentTimestamp - lastRequestTimestamp < 5000) return;
            lastRequestTimestamp = currentTimestamp;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String req = context.getString(R.string.address_request_template, location.getLatitude(), location.getLongitude());

                    try {
                        System.out.println(myUser.getProperties().getNumber() +":"+ req);
                        final String res = Utils.getUrl(req);
                        System.out.println(res);
                            JSONObject address = new JSONObject(res);
                            setTitle(address.getString("display_name"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTitle(null);
                    }
                }
            }).start();
        }
    }

}
