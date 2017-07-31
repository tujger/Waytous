package com.edeqa.waytous.helpers;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edeqa.eventbus.EventBus;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.holders.PropertiesHolder;
import com.edeqa.waytous.interfaces.Entity;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created 9/18/16.
 */
public class MyUser {

    private static final int SIMPLIFY_AFTER_EACH = 100;
    private final EventBus<AbstractView> viewBus;
    private final EventBus<AbstractProperty> propertyBus;

    volatile private LinkedHashMap<String,Entity> entities;
    private ArrayList<Location> locations;
    private Location location;
    private AtomicBoolean continueFiring = new AtomicBoolean();
    private long counter;
    private boolean user;

    public MyUser(){
        locations = new ArrayList<>();
        entities = new LinkedHashMap<>();

        propertyBus = new EventBus<>(String.valueOf(this.hashCode()));
        viewBus = new EventBus<>(String.valueOf(this.hashCode()));
//        propertyBus.setRunner(State.getInstance().getAndroidRunner());
        viewBus.setRunner(State.getInstance().getAndroidRunner());

        counter = 0;
        createProperties();
        user = false;

    }

        /*CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude())).radius(location.getAccuracy())
                .fillColor(Color.CYAN).strokeColor(Color.BLUE).strokeWidth(2f);
        circle = map.addCircle(circleOptions);*/

    public MyUser addLocation(Location location) {
        locations.add(location);
        setLocation(location);
        if((++counter) % SIMPLIFY_AFTER_EACH == 0 && counter > 1) { // simplifying locations
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<LatLng> positions = new ArrayList<>();

                        long previous = locations.size() - SIMPLIFY_AFTER_EACH - 1;
                        if(previous < 0) previous = 0;

                        for(long i = previous; i<locations.size()-1;i++) {
                            try {
                                positions.add(Utils.latLng(locations.get((int) i)));
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        positions = PolyUtil.simplify(positions, 10);

                        Iterator<Location> iterLocs = locations.iterator();
                        Iterator<LatLng> iterPozs = positions.iterator();

                        int sizeBefore = locations.size();
                        LatLng pos = iterPozs.next();
                        int i = 0;
                        while(i++ < previous) {
                            iterLocs.next();
                        }
                        while (iterLocs.hasNext()) {
                            Location loc = iterLocs.next();
                            if (loc != null && loc.getLatitude() == pos.latitude && loc.getLongitude() == pos.longitude) {
                                if (iterPozs.hasNext()) {
                                    pos = iterPozs.next();
                                } else {
                                    break;
                                }
                            } else {
                                iterLocs.remove();
                            }
                        }
                        Log.i("MyUser","Simplified locations {user:"+getProperties().getDisplayName()+", counter:"+counter+", size before:"+sizeBefore+", size after:"+locations.size()+"}");
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    onChangeLocation();
                }
            }).start();
        } else {
            onChangeLocation();
        }
        return this;
    }

    public Location getLocation(){
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<Location> getLocations(){
        return locations;
    }

    private void createProperties(){
        Iterator<String> iter = State.getInstance().getUserHolders().keySet().iterator();

        while(iter.hasNext()) {
            String type = iter.next();
            if(entities.containsKey(type)) continue;

            AbstractPropertyHolder holder = (AbstractPropertyHolder) State.getInstance().getUserHolders().get(type);
            if(!(holder instanceof AbstractViewHolder)) {
                AbstractProperty property = holder.create(this);
                if (property != null) {
                    entities.put(holder.getType(), property);
                    propertyBus.register(property);
                }
            }
        }

/*
        for(AbstractPropertyHolder holder: State.getInstance().getUserPropertyBus().getHolders()){
            System.out.println("NEXT:"+holder.getType());
            if(entities.containsKey(holder.getType())) continue;
            AbstractProperty property = holder.create(this);
            if(property != null){
                entities.put(holder.getType(),property);
                State.getInstance().getUserPropertyBus().register(property);
            }
        }
*/
/*
        for(Map.Entry<String, EntityHolder> entry: State.getInstance().getUserEntityHolders().entrySet()){
            if(entities.containsKey(entry.getKey())) continue;
            Entity property = entry.getValue().create(this);
            if(property != null){
                entities.put(entry.getKey(),property);
            }
        }
*/
    }

    public void createViews(){
        System.out.println("CREATEVIEWS:"+getProperties().getNumber());
        if(getProperties().isActive()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {

                    /*Iterator<Map.Entry<String, Entity>> iter = entities.entrySet().iterator();

                    while(iter.hasNext()) {
                        Map.Entry<String, Entity> entry = iter.next();
                        if(entry.getValue() instanceof AbstractView) {
                            entry.getValue().remove();
                            iter.remove();
                        }
                    }*/

                    Iterator<Map.Entry<String, AbstractPropertyHolder>> iterator = State.getInstance().getUserHolders().entrySet().iterator();

                    while(iterator.hasNext()) {
                        Map.Entry<String, AbstractPropertyHolder> entry = iterator.next();
                        String type = entry.getKey();
                        if(entities.containsKey(type)) continue;

                        AbstractPropertyHolder holder = State.getInstance().getUserHolders().get(type);

                        if(holder == null) continue;
                        if(holder instanceof AbstractViewHolder) {
                            if(entities.containsKey(type) && entities.get(type) != null){
                                entities.get(type).remove();
                            }
                            AbstractView property = ((AbstractViewHolder) holder).create(MyUser.this);
                            if (property != null) {
                                System.out.println("ADDVIEW:"+getProperties().getNumber()+":"+getProperties().getDisplayName()+":"+property);
                                entities.put(holder.getType(), property);
                                propertyBus.register(property);
                            }
                        }
                    }
                    System.out.println("VIEWSCREATED:"+MyUser.this);

                }
            });
        }

    }

    public void fire(final String EVENT){
        fire(EVENT, null);
    }

    public void fire(final String EVENT, final Object object){
        propertyBus.post(EVENT, object);
        viewBus.post(EVENT, object);


/*        continueFiring.set(true);
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(entry.getValue() instanceof AbstractProperty){
                try {
                    if(!continueFiring.get()) break;
                    continueFiring.set(entry.getValue().onEvent(EVENT, object));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,Entity> entry: entities.entrySet()){
                    if(entry.getValue() instanceof AbstractView){
                        try {
                            if(!continueFiring.get()) break;
                            continueFiring.set(entry.getValue().onEvent(EVENT, object));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });*/
    }

    public void onChangeLocation(){
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(entry.getValue() instanceof AbstractProperty
                    && entry.getValue().dependsOnLocation()
                    && getLocation() != null){
                try {
                    entry.getValue().onChangeLocation(getLocation());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                    if (getProperties().isActive() && entry.getValue() == null) {
                        AbstractViewHolder holder = State.getInstance().getUserViewHolders().get(entry.getKey());
                        if(holder != null) {
                            entry.setValue(holder.create(MyUser.this));
                        }
                    }
                    if (entry.getValue() instanceof AbstractView
                            && entry.getValue().dependsOnLocation()
                            && getLocation() != null) {
                        try {
                            entry.getValue().onChangeLocation(getLocation());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void removeViews(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Iterator<Map.Entry<String,Entity>> iter = entities.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,Entity> entry = iter.next();
                    if(entry.getValue() instanceof AbstractView){
                        entry.getValue().remove();
                        iter.remove();
                    }
                }
            }
        });
    }

    public PropertiesHolder.Properties getProperties(){
        return (PropertiesHolder.Properties) entities.get(PropertiesHolder.TYPE);
    }

    public Entity getEntity(String TYPE) {
        if(entities.containsKey(TYPE)){
            return entities.get(TYPE);
        }
        return null;
    }

    public boolean isUser(){
        return user;
        /*return location != null
                && (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                || location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)
                || location.getProvider().equals("LocationStore")
                || location.getProvider().equals("fused")
                || location.getProvider().equals("touch"));*/
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "entities=" + entities +
                ", location=" + location +
                ", user=" + user +
                "} " + (getProperties() != null ? getProperties().toString() : "");
    }
}
