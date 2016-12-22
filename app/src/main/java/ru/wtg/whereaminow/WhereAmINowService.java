package ru.wtg.whereaminow;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.service_helpers.MyTracking;

public class WhereAmINowService extends Service {

    private ServiceBinder binder = new ServiceBinder();
    private State state;

    private int id;

    public WhereAmINowService() {
//        new GlobalExceptionHandler(WhereAmINowService.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        state = State.getInstance();
        state.setService(this);

//        if(state.getToken() != null) {
//            Intent intentService = new Intent(state,WhereAmINowService.class);
//            intentService.putExtra("mode", "join");
//            intentService.putExtra("token", state.getToken());
//            intentService.putExtra("host", data.getHost());
//            startService(intentService);
//            state.fire(TRACKING_JOIN);
//            onStartCommand()
//        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        id = startId;
        String mode = "initial";
        if(intent != null && intent.hasExtra("mode")) mode = intent.getStringExtra("mode");

        if("start".equals(mode)){
            try {
                state.setTracking(new MyTracking());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
            state.getTracking().start();
        } else if("join".equals(mode)){
            if(state.tracking()) {
                state.getTracking().stop();
            }
            try {
                assert intent != null;
                state.setTracking(new MyTracking(intent.getStringExtra("host")));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
            String token = intent.getStringExtra("token");
            state.getTracking().join(token);
        } else if("stop".equals(mode) && state.tracking()){
            state.getTracking().stop();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class ServiceBinder extends Binder {
        WhereAmINowService getService() {
            return WhereAmINowService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        state.setService(null);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public int getId(){
        return id;
    }


}
