package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.abstracts.AbstractProperty;
import ru.wtg.whereaminow.abstracts.AbstractPropertyHolder;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SystemMessage;
import ru.wtg.whereaminow.helpers.UserMessage;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NUMBER;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_JOINED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_MESSAGE;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_PRIVATE;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_DISMISSED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_JOINED;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SHOW_MESSAGES;
import static ru.wtg.whereaminow.holders.NotificationHolder.SHOW_CUSTOM_NOTIFICATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;

/**
 * Created 11/27/16.
 */
public class MessagesHolder extends AbstractPropertyHolder {

    public static final String TYPE = REQUEST_MESSAGE;

    public static final String NEW_MESSAGE = "new_message";
    public static final String SEND_MESSAGE = "send_message";
    public static final String PRIVATE_MESSAGE = "private";
    public static final String USER_MESSAGE = "user_message";
    public static final String WELCOME_MESSAGE = "welcome_message";
    private final Context context;
    private final android.support.v4.app.NotificationCompat.Builder notification;

    private ArrayList<UserMessage> messages = new ArrayList<>();
    private boolean showNotifications = true;
    private Notification result;

    public MessagesHolder(Context context) {
        this.context = context;
        UserMessage.init(context);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification = new NotificationCompat.Builder(context)
                .setVisibility(VISIBILITY_PUBLIC)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification_message)
                .setAutoCancel(true)
                .setContentTitle("...")
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setVibrate(new long[]{0L, 0L});
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public Messages create(MyUser myUser) {
        if (myUser == null) return null;
        return new Messages(myUser);
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

    @Override
    public boolean isEraseable() {
        return false;
    }


    @Override
    public void perform(final JSONObject o) throws JSONException {
        if(o.has(REQUEST_DELIVERY_CONFIRMATION)) {
            System.out.println("DELIVERED:"+o);
            try {
                UserMessage m = UserMessage.getItemByFieldValue("delivery", o.getString(REQUEST_DELIVERY_CONFIRMATION));

                System.out.println("MESSAGEFOUND:" + m);
                if (m != null) {
                    m.setDelivery("delivered");
                    m.save(null);
                }
            }catch(Exception e){
                e.printStackTrace();
            }


        } else if (o.has(USER_MESSAGE)) {
            int number = o.getInt(USER_NUMBER);
            final String text = o.getString(USER_MESSAGE);
            if(o.has("key")){
                String key = o.getString("key");
                if(UserMessage.getItemByFieldValue("key", key) != null) return;
            }

            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser myUser) {
                    UserMessage m = new UserMessage(context);
                    m.setBody(text);
                    try {
                        m.setKey(o.getString("key"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    m.setFrom(myUser);
                    if(o.has(PRIVATE_MESSAGE)){
                        m.setType(TYPE_PRIVATE);
                        m.setTo(State.getInstance().getMe());
                    } else {
                        m.setType(TYPE_MESSAGE);
                    }
                    myUser.fire(USER_MESSAGE, m);
                }
            });
        }
    }

    @Override
    public boolean onEvent(String event, Object object) {
        final MyUser user;
        switch(event){
            case SEND_MESSAGE:
                SystemMessage mm = (SystemMessage) object;
                UserMessage m;
                if(mm != null) {
                    m = new UserMessage(mm);
                    m.save(mm.getCallback());

                    messages.add(m);
                    State.getInstance().getTracking()
                            .put(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, m.getBody())
                            .put(REQUEST_PUSH, true)
                            .put(REQUEST_DELIVERY_CONFIRMATION, m.getDelivery())
                            .send(REQUEST_MESSAGE);
                }
                break;
            case USER_JOINED:
                user = (MyUser) object;
                if(!user.isUser()) return true;
                m = new UserMessage(context);
                m.setBody(context.getString(R.string.user_has_joined_the_group));
                m.setFrom(user);
                m.setType(TYPE_USER_JOINED);
                m.save(null);
                messages.add(m);
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(!user.isUser()) return true;
                m = new UserMessage(context);
                m.setBody(context.getString(R.string.user_left_the_group));
                m.setFrom(user);
                m.setType(TYPE_USER_DISMISSED);
                m.save(null);
                messages.add(m);
                break;
            case WELCOME_MESSAGE:
                String text = (String) object;
                m = new UserMessage(context);
                m.setBody(text);
                m.setFrom(State.getInstance().getUsers().getUsers().get(0));
                m.setType(TYPE_JOINED);
                m.save(null);
                messages.add(m);
                break;
            case ACTIVITY_RESUME:
                showNotifications = false;
                break;
            case ACTIVITY_PAUSE:
                showNotifications = true;
                break;
        }
        return true;
    }

    private class Messages extends AbstractProperty {

        Messages(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(!myUser.isUser()) return true;
            switch (event){
                case USER_MESSAGE:
                    UserMessage m = (UserMessage) object;
                    if(m != null) {
                        m.save(null);
                        messages.add(m);

                        Intent viewIntent = new Intent(context, MainActivity.class);
                        viewIntent.putExtra("action", "fire");
                        viewIntent.putExtra("fire", SHOW_MESSAGES);

                        Intent replyIntent = new Intent(context, MainActivity.class);
                        replyIntent.putExtra("action", "fire");
                        replyIntent.putExtra("fire", NEW_MESSAGE);
                        replyIntent.putExtra("number", myUser.getProperties().getNumber());

                        PendingIntent pendingViewIntent = PendingIntent.getActivity(context, 1978, viewIntent, 0);
                        PendingIntent pendingReplyIntent = PendingIntent.getActivity(context, 1979, replyIntent, 0);

                        notification.mActions.clear();
                        notification.addAction(R.drawable.ic_notification_message, context.getString(R.string.view), pendingViewIntent);
                        notification.addAction(R.drawable.ic_notification_reply, context.getString(R.string.reply), pendingReplyIntent);

                        notification.setContentTitle(myUser.getProperties().getDisplayName())
                                .setContentText(m.getBody())
                                .setWhen(new Date().getTime());

                        if(showNotifications) {
                            notification.setDefaults(DEFAULT_ALL);
                        }

                        State.getInstance().fire(SHOW_CUSTOM_NOTIFICATION, notification.build());

                    }

                    break;
                case SEND_MESSAGE:
                    SystemMessage mm = (SystemMessage) object;
                    if(mm != null) {
                        m = new UserMessage(mm);
                        m.save(mm.getCallback());

                        messages.add(m);
                        State.getInstance().getTracking()
                                .put(RESPONSE_PRIVATE, mm.getToUser().getProperties().getNumber())
                                .put(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, m.getBody())
                                .put(REQUEST_PUSH, true)
                                .put(REQUEST_DELIVERY_CONFIRMATION, m.getDelivery())
                                .send(REQUEST_MESSAGE);
                    }
                    break;
                case CHANGE_NUMBER:
                    if(State.getInstance().tracking_active() && myUser.getProperties().getNumber() == 0) {
                        String text = State.getInstance().getStringPreference(WELCOME_MESSAGE, "");
                        if(text.length()>0) {
                            State.getInstance().getTracking().put(REQUEST_WELCOME_MESSAGE, text).send(REQUEST_WELCOME_MESSAGE);
                        }
                    }
                    break;
            }
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }
    }
}
