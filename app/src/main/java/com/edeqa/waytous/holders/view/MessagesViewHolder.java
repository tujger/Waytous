package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.ShareSender;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.helpers.SystemMessage;
import com.edeqa.waytous.helpers.UserMessage;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable1;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_FAB;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.SmoothInterpolated.CURRENT_VALUE;
import static com.edeqa.waytous.helpers.UserMessage.TYPE_PRIVATE;
import static com.edeqa.waytous.holders.property.MessagesHolder.NEW_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.PRIVATE_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.SEND_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.USER_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.WELCOME_MESSAGE;
import static com.edeqa.waytous.holders.property.NotificationHolder.HIDE_CUSTOM_NOTIFICATION;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;


/**
 * Created 11/27/16.
 */
@SuppressWarnings("WeakerAccess")
public class MessagesViewHolder extends AbstractViewHolder {

    public static final String SHOW_MESSAGES = "show_messages"; //NON-NLS
    public static final String SETUP_WELCOME_MESSAGE = "setup_welcome_message"; //NON-NLS

    private static final String PREFERENCE_HIDE_SYSTEM_MESSAGES = "messages_hide_system_messages"; //NON-NLS
    private static final String PREFERENCE_FONT_SIZE = "messages_font_size"; //NON-NLS
    private static final String PREFERENCE_NOT_TRANSPARENT = "messages_not_transparent"; //NON-NLS


    private UserMessage.UserMessagesAdapter adapter;
    private SmoothInterpolated action;
    private Toolbar toolbar;
    private ColorDrawable drawable;
    private RecyclerView list;
    private AlertDialog dialog;

    private String filterMessage;
    private Integer fontSize;
    private boolean hideSystemMessages;
    private boolean donotscroll;
    private boolean notTransparentWindow;

    public MessagesViewHolder(final MainActivity context) {
        super(context);
        this.dialog = new AlertDialog.Builder(context).create();
        filterMessage = "";

        context.findViewById(R.id.toolbar).setOnTouchListener(new View.OnTouchListener() {
            float x1,x2;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getY();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        x2 = event.getY();
                        float deltaX = x2 - x1;
                        if(deltaX > 10) {
                            showMessages();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public MessagesView create(MyUser myUser) {
        if (myUser == null) return null;
        return new MessagesView(myUser);
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case NEW_MESSAGE:
                MyUser to = null;
                if(object instanceof Integer) {
                    to = State.getInstance().getUsers().getUsers().get(object);
                } else if (object instanceof MyUser) {
                    to = (MyUser) object;
                }
                newMessage(to,false,"");
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1977);
                break;
            case SHOW_MESSAGES:
                State.getInstance().fire(HIDE_CUSTOM_NOTIFICATION);
                showMessages();
                break;
//            case TOKEN_CHANGED:
//                if(adapter != null){
//                    adapter.notifyDataSetChanged();
//                }
//                break;
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_communication, R.string.chat, R.string.chat, R.drawable.ic_chat_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().fire(SHOW_MESSAGES);
                                return false;
                            }
                        });
                break;
/*
            case PREPARE_DRAWER:
                adder = (DrawerViewHolder.ItemsHolder) object;
                UserMessage.getDb().removeRestriction("search");
                int count = UserMessage.getCount();
                adder.findItem(R.string.chat).setVisible(count > 0);
                break;
*/
            case PREPARE_FAB:
                final FabViewHolder fab = (FabViewHolder) object;
                if(State.getInstance().tracking_active()) {
                    fab.add(R.string.new_message,R.drawable.ic_chat_black_24dp).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fab.close(true);
                            State.getInstance().fire(NEW_MESSAGE);
                        }
                    });
                }
                break;
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.set_welcome_message, Menu.NONE, R.string.set_welcome_message).setVisible(false).setOnMenuItemClickListener(onMenuItemSetWelcomeMessageClickListener);
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.set_welcome_message).setVisible(State.getInstance().tracking_active() && State.getInstance().getMe().getProperties().getNumber() == 0);
                break;
            case SETUP_WELCOME_MESSAGE:
                onMenuItemSetWelcomeMessageClickListener.onMenuItemClick(null);
                break;
        }
        return true;
    }

    private void newMessage(final MyUser toUser, final boolean privateMessage, String text) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (toUser == null) {
            dialog.setTitle(context.getString(R.string.send_message));
        } else {
            dialog.setTitle(context.getString(privateMessage ? R.string.private_message_to_s : R.string.reply_to_s, toUser.getProperties().getDisplayName()));
        }

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_new_message, null);

        final EditText etMessage = (EditText) content.findViewById(R.id.et_message);
        etMessage.setText(text);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (etMessage.getText().toString().length() > 0) {
                    if(State.getInstance().tracking_active()) {
                        if (privateMessage && toUser != null) {
                            SystemMessage mm = new SystemMessage(context)
                                    .setFromUser(State.getInstance().getMe())
                                    .setText(etMessage.getText().toString())
                                    .setDelivery(Utils.getUnique())
                                    .setToUser(toUser)
                                    .setType(TYPE_PRIVATE);
                            toUser.fire(SEND_MESSAGE, mm);

//                            UserMessage m = new UserMessage(context);
//                            m.setFrom(State.getInstance().getMe());
//                            m.setBody(etMessage.getText().toString());
//                            m.setDelivery(Utils.getUnique());
//                            m.setTo(toUser);
//                            m.setType(TYPE_PRIVATE);
//                            m.save(null);
//
//                            toUser.fire(SEND_MESSAGE, m);
                        } else {
                            SystemMessage mm = new SystemMessage(context)
                                    .setFromUser(State.getInstance().getMe())
                                    .setText(etMessage.getText().toString())
                                    .setDelivery(Utils.getUnique());

                            State.getInstance().fire(SEND_MESSAGE, mm);

//                            UserMessage m = new UserMessage(context);
//                            m.setFrom(State.getInstance().getMe());
//                            m.setBody(etMessage.getText().toString());
//                            m.setDelivery(Utils.getUnique());
//                            m.save(null);
//
//                            State.getInstance().fire(SEND_MESSAGE, m);
                        }
                        reloadCursor();
                    } else {
                        new SystemMessage(context).setText(context.getString(R.string.cannot_send_message_because_of_network_is_unavailable)).showSnack();
                    }
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.log(MessagesViewHolder.this, "newMessage:", "Cancel"); //NON-NLS
            }
        });
        if(toUser != null && !privateMessage) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.private_string), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newMessage(toUser, true, etMessage.getText().toString());
                }
            });
        }
        if(privateMessage) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.not_private), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newMessage(toUser, false, etMessage.getText().toString());
                }
            });
        }
        dialog.setView(content);
        dialog.show();
    }

    @SuppressWarnings("unchecked")
    public void showMessages() {
        State.getInstance().fire(HIDE_CUSTOM_NOTIFICATION);

        dialog = new AlertDialog.Builder(context).create();

        final View content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);

        final LinearLayout layoutFooter = setupFooter(content);

        context.getLayoutInflater().inflate(R.layout.dialog_items, null);

        list = (RecyclerView) content.findViewById(R.id.list_items);

        adapter = new UserMessage.UserMessagesAdapter(context, list);
        adapter.setEmptyView(content.findViewById(R.id.tv_placeholder));

        hideSystemMessages = State.getInstance().getBooleanPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
        notTransparentWindow = State.getInstance().getBooleanPreference(PREFERENCE_NOT_TRANSPARENT, false);
        fontSize = State.getInstance().getIntegerPreference(PREFERENCE_FONT_SIZE, 12);

        dialog.setCustomTitle(setupToolbar());

        if(hideSystemMessages) {
            UserMessage.getDb().addRestriction("user", "type_ = ? or type_ = ?", new String[]{""+UserMessage.TYPE_MESSAGE,""+ TYPE_PRIVATE});
        }
        context.getSupportLoaderManager().initLoader(2, null, adapter);

        adapter.setFontSize(fontSize);
        adapter.setOnRightSwipeListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                UserMessage.getDb().deleteByPosition(position);
                adapter.notifyItemRemoved(position);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        donotscroll = true;
                        reloadCursor();
                    }
                }, 500);
            }
        });


        adapter.setOnItemClickListener(new Runnable1<UserMessage>() {
            @Override
            public void call(UserMessage message) {
                reloadCursor();
            }
        });

        adapter.setOnItemShareListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                UserMessage item = UserMessage.getItemByCursor(UserMessage.getDb().getByPosition(position));
                Utils.log(MessagesViewHolder.this, "showMessages:", "item="+item);

                new ShareSender(context).send(context.getString(R.string.share_the_message), item.getFrom(), item.getFrom() + ":\n" + item.getBody());
            }
        });
        adapter.setOnItemReplyListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                UserMessage item = UserMessage.getItemByCursor(UserMessage.getDb().getByPosition(position));

                MyUser to = State.getInstance().getUsers().findUserByName(item.getFrom());
                if(to != null) {
                    ((EditText) layoutFooter.findViewById(R.id.et_message_send)).setText("> " + item.getBody());
                } else {
                    ((EditText) layoutFooter.findViewById(R.id.et_message_send)).setText("> " + item.getFrom() + ":\n> " + item.getBody());
                }
            }
        });
        adapter.setOnItemDeleteListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                UserMessage.getDb().deleteByPosition(position);
                adapter.notifyItemRemoved(position);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        donotscroll = true;
                        reloadCursor();
                    }
                }, 500);
            }
        });

        adapter.setOnItemTouchListener(onTouchListener);

        adapter.setOnCursorReloadListener(new Runnable1<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                if(toolbar != null) {
                    toolbar.setTitle(context.getString(R.string.chat_d, cursor.getCount()) + (filterMessage != null && filterMessage.length() > 0 ? " ["+filterMessage+"]" : ""));
                    if(!donotscroll) list.scrollToPosition(cursor.getCount() - 1);
                    donotscroll = false;
                }
            }
        });

        dialog.setView(content);

        drawable = new ColorDrawable(Color.WHITE);
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(drawable);
        }

        dialog.show();

        Utils.resizeDialog(context, dialog, Utils.MATCH_SCREEN, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onTouchListener.call(motionEvent);
                return false;
            }
        });

        makeDialogTransparent();

        setFilterAndReload(filterMessage);

    }

    private AppBarLayout setupToolbar() {

        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        toolbar = (Toolbar) layoutToolbar.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE,mMode);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog = null;
            }
        });

        toolbar.inflateMenu(R.menu.dialog_messages_menu);
        final MenuItem searchItem = toolbar.getMenu().findItem(R.id.search_message);
        searchItem.getIcon().setColorFilter(Color.WHITE,mMode);

        final Runnable1<String> setFilter = new Runnable1<String>() {
            @Override
            public void call(String text) {
                setFilterAndReload(text);
            }
        };
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchItem.collapseActionView();
                filterMessage = query;
                setFilter.call(filterMessage);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                filterMessage = s;
                setFilter.call(filterMessage);
                return false;
            }
        });

        prepareToolbarMenu();

        toolbar.setOnMenuItemClickListener(onDialogMenuItemClickListener);

        return layoutToolbar;
    }

    private LinearLayout setupFooter(View content) {
        final LinearLayout layoutFooter = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_message_send, null);
        ViewGroup placeFooter = (ViewGroup) content.findViewById(R.id.layout_footer);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutFooter.setLayoutParams(params);

        placeFooter.addView(layoutFooter);
        if(State.getInstance().tracking_active()) {
            placeFooter.setVisibility(View.VISIBLE);
        } else {
            placeFooter.setVisibility(View.GONE);
        }

        final Runnable1<EditText> sender = new Runnable1<EditText>() {
            @Override
            public void call(EditText et) {
                if (et.getText().toString().length() > 0) {
                    if(State.getInstance().tracking_active()) {
                        SystemMessage mm = new SystemMessage(context)
                                .setFromUser(State.getInstance().getMe())
                                .setText(et.getText().toString())
                                .setDelivery(Utils.getUnique());
                        State.getInstance().fire(SEND_MESSAGE, mm);


//                        UserMessage m = new UserMessage(context);
//                        m.setFrom(State.getInstance().getMe());
//                        m.setBody(et.getText().toString());
//                        m.setDelivery(Utils.getUnique());
//                        m.save(null);
//
//                        State.getInstance().fire(SEND_MESSAGE, m);

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                        reloadCursor();
                    } else {
                        new SystemMessage(context).setText(context.getString(R.string.cannot_send_message_because_of_network_is_unavailable)).showSnack();
                    }
                }
                et.setText("");
            }
        };

        layoutFooter.findViewById(R.id.ib_message_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sender.call((EditText)layoutFooter.findViewById(R.id.et_message_send));
            }
        });
        layoutFooter.findViewById(R.id.ib_message_send).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sender.call((EditText)layoutFooter.findViewById(R.id.et_message_send));
                dialog.dismiss();
                dialog = null;
                return true;
            }
        });

        layoutFooter.setVisibility(View.VISIBLE);
        return layoutFooter;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void setFilterAndReload(String filter) {
        if(filter != null && filter.length() > 0) {
            UserMessage.getDb().addRestriction("search","from_ LIKE ? OR to_ LIKE ? OR body_ LIKE ?", new String[]{"%"+filter+"%", "%"+filter+"%", "%"+filter+"%"});
        } else {
            UserMessage.getDb().removeRestriction("search");
        }
        Utils.log(MessagesViewHolder.this, "setFilterAndReload:", "Counter="+adapter.getItemCount());
        reloadCursor();
    }

    private void makeDialogTransparent() {
        if(notTransparentWindow) {
            drawable.setAlpha(255);

        } else {
            new SmoothInterpolated(new Runnable1<Float[]>() {
                @Override
                public void call(final Float[] value) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            if(drawable != null)
                                drawable.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
                        }
                    });
                }
            }).execute();
        }
    }

    private void reloadCursor(){
        if(context.getSupportLoaderManager().getLoader(2) != null) {
            context.getSupportLoaderManager().getLoader(2).forceLoad();
        }
    }

    private void prepareToolbarMenu() {
        toolbar.getMenu().findItem(R.id.hide_system_messages).setVisible(!hideSystemMessages);
        toolbar.getMenu().findItem(R.id.show_system_messages).setVisible(hideSystemMessages);
        toolbar.getMenu().findItem(R.id.smaller_font).setVisible(fontSize >= 12);
        toolbar.getMenu().findItem(R.id.bigger_font).setVisible(fontSize <= 24);
        toolbar.getMenu().findItem(R.id.transparent).setVisible(notTransparentWindow);
        toolbar.getMenu().findItem(R.id.not_transparent).setVisible(!notTransparentWindow);
    }

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        //noinspection HardCodedStringLiteral
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_messages").setViewId(R.string.new_message).setTitle("Here you can").setDescription("Write and send message to the group or private message to anybody."));
//        rules.put(new IntroRule().setEvent(PREPARE_OPTIONS_MENU).setId("menu_set_welcome").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setViewId(R.string.set_welcome_message).setTitle("Here you can").setDescription("Set welcome message to this group."));

        return rules;
    }

    private class MessagesView extends AbstractView {

        MessagesView(MyUser myUser) {
            super(MessagesViewHolder.this.context, myUser);
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() != null && !myUser.isUser()) return true;
            switch (event) {
                case USER_MESSAGE:
                    if(dialog != null && dialog.isShowing()) {
                        reloadCursor();
                    } else {
                        UserMessage m = (UserMessage) object;

                        //noinspection unchecked
                        new SystemMessage(context).setText(myUser.getProperties().getDisplayName() + ": " + m.getBody()).setDuration(10000).setAction(context.getString(R.string.reply),new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                newMessage(myUser, false,"");
                            }
                        }).setOnClickListener(new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).showSnack();

                    }
                    break;
                case PRIVATE_MESSAGE:
                    if(dialog != null && dialog.isShowing()) {
                        reloadCursor();
                        return false;
                    } else {
                        String text = (String) object;

                        //noinspection unchecked
                        new SystemMessage(context).setText("(private) " + myUser.getProperties().getDisplayName() + ": " + text).setDuration(10000).setAction(context.getString(R.string.reply),new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                newMessage(myUser, true, "");
                            }
                        }).setOnClickListener(new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).showSnack();
                    }
                    break;
                case NEW_MESSAGE:
                    newMessage(myUser, false, "");
                    break;
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(myUser != State.getInstance().getMe()) {
                        menu.add(0, R.string.private_message, Menu.NONE, R.string.private_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                newMessage(myUser, true, "");
                                return false;
                            }
                        }).setIcon(R.drawable.ic_chat_black_24dp);
                    }
                    break;
            }
            return true;
        }
    }

    private Runnable1<MotionEvent> onTouchListener = new Runnable1<MotionEvent>() {
        @Override
        public void call(MotionEvent motionEvent) {
            if(action != null) action.cancel();
            if(!notTransparentWindow) {
                switch (motionEvent.getAction()) {
                    case 0:
                        drawable.setAlpha(255);
                        break;
                    case 1:
                        action = new SmoothInterpolated(new Runnable1<Float[]>() {
                            @Override
                            public void call(final Float[] value) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        if(drawable != null)
                                            drawable.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
                                    }
                                });
                            }
                        }).setDuration(320);
                        action.execute();
                        break;
                }
            }
        }
    };

    private MenuItem.OnMenuItemClickListener onMenuItemSetWelcomeMessageClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(context.getString(R.string.set_welcome_message));

            View view = context.getLayoutInflater().inflate(R.layout.dialog_welcome_message, null);

            final EditText etMessage = (EditText) view.findViewById(R.id.et_welcome_message);
            final CheckBox cbSaveAsDefault = (CheckBox) view.findViewById(R.id.cb_save_as_default);

            etMessage.setText(State.getInstance().getStringPreference(WELCOME_MESSAGE, ""));

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(State.getInstance().tracking_active() && etMessage.getText().toString().length()>0) {
                        State.getInstance().getTracking().put(REQUEST_WELCOME_MESSAGE, etMessage.getText().toString()).send(REQUEST_WELCOME_MESSAGE);
                        if(cbSaveAsDefault.isChecked()) {
                            State.getInstance().setPreference(WELCOME_MESSAGE, etMessage.getText().toString());
                        }
                    }
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Utils.log(MessagesViewHolder.this, "onMenuItemSetWelcomeMessageClickListener:", "Cancel"); //NON-NLS
                }
            });

            dialog.setView(view);

            dialog.show();

            return false;
        }
    };

    private Toolbar.OnMenuItemClickListener onDialogMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.hide_system_messages:
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, true);
                    hideSystemMessages = true;
                    UserMessage.getDb().addRestriction("user", "type_ = ? or type_ = ?", new String[]{""+UserMessage.TYPE_MESSAGE,""+ TYPE_PRIVATE});
                    reloadCursor();
                    break;
                case R.id.show_system_messages:
                    UserMessage.getDb().removeRestriction("user");
                    hideSystemMessages = false;
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
                    reloadCursor();
                    break;
                case R.id.transparent:
                    State.getInstance().setPreference(PREFERENCE_NOT_TRANSPARENT, false);
                    notTransparentWindow = false;
                    makeDialogTransparent();
                    break;
                case R.id.not_transparent:
                    State.getInstance().setPreference(PREFERENCE_NOT_TRANSPARENT, true);
                    notTransparentWindow = true;
                    makeDialogTransparent();
                    break;
                case R.id.smaller_font:
                    fontSize -= 2;
                    State.getInstance().setPreference(PREFERENCE_FONT_SIZE, fontSize);
                    adapter.setFontSize(fontSize);
                    adapter.notifyDataSetChanged();
                    toolbar.post(new Runnable() { public void run() { toolbar.showOverflowMenu(); } });
                    break;
                case R.id.bigger_font:
                    fontSize += 2;
                    State.getInstance().setPreference(PREFERENCE_FONT_SIZE, fontSize);
                    adapter.setFontSize(fontSize);
                    adapter.notifyDataSetChanged();
                    toolbar.post(new Runnable() { public void run() { toolbar.showOverflowMenu(); } });
                    break;
                case R.id.clear_messages:
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setTitle(context.getString(R.string.clear_all_messages));
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UserMessage.clear();
//                            if(adapter != null) adapter.notifyDataSetChanged();
                            reloadCursor();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                        }
                    });
                    dialog.show();
                    break;
            }
            prepareToolbarMenu();
            return false;
        }
    };

}