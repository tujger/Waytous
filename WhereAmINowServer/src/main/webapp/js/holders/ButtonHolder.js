/**
 * Created 2/11/17.
 */
EVENTS.HIDE_MENU_SUBTITLE = "hide_menu_subtitle";
EVENTS.SHOW_MENU_SUBTITLE = "show_menu_subtitle";
EVENTS.UPDATE_MENU_SUBTITLE = "update_menu_subtitle";

MENU = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_VIEWS: 3,
    SECTION_NAVIGATION: 4,
    SECTION_EDIT: 5,
    SECTION_MAP: 8,
    SECTION_LAST: 9
}

function ButtonHolder(main) {

    var type = "button";
    var buttons;
    var contextMenu;
    var sections;
    var contextMenuLayout;
    var delayDismiss;
    var startTime;

    function start() {
        buttons = u.dialog({
            id: "button",
            title: {
                label: "Users",
                className: "user-buttons-title",
                button: {
                    icon: "view_headline",
                    className: "user-buttons-title-button",
                    onclick: function() {
                        var mininized = u.load("button:minimized");
                        u.save("button:minimized", !mininized);
                        main.users.forAllUsers(function(number,user){
                            user.views.button.subtitle[u.load("button:minimized") ? "hide" : "show"]();
                            updateSubtitle.call(user);
                        });
                    }
                }
            },
            className: "user-buttons",
            tabindex: 1,
            resizeable: true,
            items: [],
            itemsClassName: "user-buttons-items",
        });

        contextMenuLayout = u.create(HTML.DIV, {className:"user-context-menu shadow hidden", tabindex: 2, onblur: function(){
                contextMenuLayout.hide();
            }, onmouseleave: function(){
                contextMenuLayout.hide();
            }, onmouseenter: function(){
                clearTimeout(delayDismiss);
            }
        }, main.right);
        contextMenu = new ContextMenu();
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.TRACKING_ACTIVE:
                buttons.open();
                break;
            case EVENTS.TRACKING_DISABLED:
                buttons.close();
                break;
            case EVENTS.SELECT_USER:
                this.views.button.button.scrollIntoView();
                this.views.button.button.classList.add("user-button-selected");
                break;
            case EVENTS.UNSELECT_USER:
                this.views.button.button.classList.remove("user-button-selected");
                break;
            case EVENTS.CHANGE_NAME:
                this.views.button.title.innerHTML = this.properties.getDisplayName();
                break;
            case EVENTS.CHANGE_NUMBER:
                this.views.button.button.dataset.number = parseInt(object);
                break;
            case EVENTS.MAKE_ACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.show();
                u.lang.updateNode(buttons.titleLayout, u.lang.users_d.format(main.users.getCountActive()))

//                buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";
                if(main.users.getCountActive() > 1) {
                    buttons.open();
                } else if(!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED) {
                    buttons.close();
                }
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.hide();
                u.lang.updateNode(buttons.titleLayout, u.lang.users_d.format(main.users.getCountActive()))
//                buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";
                if(main.users.getCountActive() < 2 && (!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED)) {
                    buttons.close();
                }
                break;
//            case EVENTS.UPDATE_ADDRESS:
//                var subtitle = this.views.button.subtitle;
//                if(object) {
//                    subtitle.innerHTML = object;
//                    if(!u.load("button:minimized")) {
//                        subtitle.show();
//                        updateSubtitle.call(this);
//                    }
//                } else {
//                    subtitle.hide();
//                }
//                break;
            case EVENTS.SHOW_BADGE:
                if(object == EVENTS.INCREASE_BADGE) {
                    var value = parseInt(this.views.button.badge.innerHTML);
                    value = value || 0;
                    this.views.button.badge.innerHTML = ""+(++value);
                    this.views.button.button.scrollIntoView();
                } else {
                    this.views.button.badge.innerHTML = object || "";
                }
                if(this.views.button.badge.innerHTML) {
                    this.views.button.badge.show();
                }
                break;
            case EVENTS.HIDE_BADGE:
                this.views.button.badge.hide();
                this.views.button.badge.innerHTML = "";
                break;
            case EVENTS.MOUSE_OVER:
                this.views.button.button.classList.add("user-button-hover");
                break;
            case EVENTS.MOUSE_OUT:
                this.views.button.button.classList.remove("user-button-hover");
                break;
            case EVENTS.CHANGE_COLOR:
                if(!object && object.constructor === String) {
                    var color = object || "#0000FF";
                    color = u.getRGBAColor(color, 0.4)
                    this.views.button.button.style.backgroundColor = color;
                } else if(object && object.constructor === Number) {
                    console.log("TODO NUMERIC")
                }
                break;
            default:
                break;
        }
        return true;
    }

    var clicked = false;
    function createView(user){

//    if(buttons.itemsLayout.children.length ==1 && user != main.me){
//    debugger;
//    }

        if(!user || !user.properties) return;
        var color = user.color || user.properties.color || "#0000FF";
        color = u.getRGBAColor(color, 0.4);

        var task;
        var onlyTouch,clicked;
        var b = u.create(HTML.DIV, {
            className:"user-button" +(user.properties.active ? "" : " hidden"),
            dataNumber:user.number,
            style:{backgroundColor:color},
            onmousedown: function(){
                onlyTouch = true;
                startTime = new Date().getTime();
                task = setTimeout(function(){
                    openContextMenu(user);
                }, 500);
                // console.log(user);
            },
            onmousemove: function(){
                onlyTouch = false;
            },
            onmouseup: function(){
                if(!onlyTouch) return;
                var delay = new Date().getTime() - startTime;
                if(delay < 500) {
                    if(clicked) {
                        user.fire(EVENTS.CAMERA_ZOOM);
                        contextMenuLayout.hide();
                        clicked = false;
                    } else {
                        user.fire(EVENTS.SELECT_SINGLE_USER);
                        openContextMenu(user);
                        clicked = true;
                        setTimeout(function(){
                            clicked = false;
                        }, 500);
                    }
                }
                clearTimeout(task);
            },
            onmouseenter: function(e) {
                user.fire(EVENTS.MOUSE_OVER,e);
            },
            onmouseleave: function(e) {
                user.fire(EVENTS.MOUSE_OUT,e);
            }
        });
        var icon = (user && user.origin && user.origin.buttonIcon) || "person";
        u.create(HTML.DIV, {className:"user-button-icon", innerHTML:icon}, b);
        var badge = u.create(HTML.DIV, {className:"user-button-badge hidden"}, b);
//        console.log(user)
        var div = u.create(HTML.DIV, {className:"user-button-label"}, b);
        var title = u.create(HTML.DIV, {className:"user-button-title",innerHTML:user.properties.getDisplayName()}, div);
        var subtitle = u.create(HTML.DIV, {className:"user-button-subtitle hidden",innerHTML:""}, div);

        if(!u.load("button:minimized")) {
            subtitle.show();
            updateSubtitle.call(user);
        }

        buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";

        var added = false;
        for(var i =0; i < buttons.itemsLayout.children.length; i++) {
            var number = parseInt(buttons.itemsLayout.children[i].dataset.number);
            if(number != main.me.number && number >= user.number) {
                buttons.itemsLayout.insertBefore(b, buttons.itemsLayout.children[i]);
                added = true;
                break;
            }
        }
        if(!added) {
            buttons.itemsLayout.appendChild(b);
        }

        return {
            button: b,
            title: title,
            subtitle: subtitle,
            badge:badge,
        };
    }

    function removeView(user){
        user.views.button.button.hide();
    }

    function openContextMenu(user) {
        u.clear(contextMenuLayout);
        sections = [];
        for(var i = 0; i < 10; i ++) {
            sections[i] = u.create(HTML.DIV, {className:"user-context-menu-section hidden"}, contextMenuLayout);
        }
        user.fire(EVENTS.CREATE_CONTEXT_MENU, contextMenu);

        setTimeout(function(){
            var size = user.views.button.button.getBoundingClientRect();
            contextMenuLayout.show();
            contextMenuLayout.style.top = Math.floor(size.top) + "px";
            if(size.left - main.right.offsetLeft - contextMenuLayout.offsetWidth -10 > 0) {
                contextMenuLayout.style.left = Math.floor(size.left - contextMenuLayout.offsetWidth -10) + "px";
            } else {
                contextMenuLayout.style.left = Math.floor(size.right + 10) + "px";
            }
            if(main.right.offsetTop + main.right.offsetHeight < contextMenuLayout.offsetTop + contextMenuLayout.offsetHeight) {
                contextMenuLayout.style.top = (main.right.offsetTop + main.right.offsetHeight - contextMenuLayout.offsetHeight - 5) + "px";
            }

            clearTimeout(delayDismiss);
            delayDismiss = setTimeout(function(){
                contextMenuLayout.hide();
            },2000);
        },0);
    }

    function ContextMenu() {

        function add(section,id,name,icon,callback) {
            var th = u.create(HTML.DIV, {
                className:"user-context-menu-item",
                onclick: function() {
                    setTimeout(function(){
                        contextMenuLayout.hide();
                        contextMenuLayout.blur();
                        callback();
                    }, 0);
                },
            }, sections[section]);
            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"user-context-menu-item-icon", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            u.create(HTML.DIV, { className:"user-context-menu-item-title", innerHTML: name}, th);
            sections[section].show();
            return th;
        }
        function getContextMenu(){
            console.log("GETCONTEXTMENU:",items);
        }

        return {
            add:add,
            getContextMenu:getContextMenu,
        }
    }

    function onChangeLocation(location) {
        updateSubtitle.call(this);
    }

    function updateSubtitle() {
        if(this.location && this.views.button && !this.views.button.subtitle.classList.contains("hidden")) {
            this.fire(EVENTS.UPDATE_MENU_SUBTITLE, this.views.button.subtitle);
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
        onChangeLocation:onChangeLocation,
    }
}