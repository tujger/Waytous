/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/8/17.
 */
EVENTS.UPDATE_ACTIONBAR_SUBTITLE = "update_actionbar_subtitle";

function DrawerHolder(main) {

    var drawer;
    var title;
    var subtitle;
    var backButtonAction;
    var actionbar;


    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        var dialogAbout = utils.dialogAbout(main.right);

        var sections = {};
        sections[DRAWER.SECTION_MAP] = u.lang.map;

        drawer = new u.drawer({
            title: main.appName,
            subtitle: u.lang.be_always_on_the_same_way,
            logo: {
                src:"/images/logo.svg",
                onclick: function(){
                    dialogAbout.open();
                }
            },
            ontogglesize: function() {
                main.fire(EVENTS.CAMERA_UPDATE);
            },
            onprimaryclick: function(){
                main.me.fire(EVENTS.SELECT_SINGLE_USER);
            },
            footer: {
                className: "drawer-footer-label",
                content: u.create(HTML.DIV).place(HTML.SPAN, {className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017 Edeqa", onclick: function(e){
                    dialogAbout.open();
                    e.preventDefault();
                    e.stopPropagation;
                    return false;
                }}).place(HTML.SPAN, "\nBuild " + data.version)
            },
            sections: sections,
            collapsible: [DRAWER.SECTION_MAP, DRAWER.SECTION_VIEWS]
        }, document.body);

        actionbar = u.actionBar({
            title: main.appName,
            onbuttonclick: function(){
                 try {
                     drawer.open();
                 } catch(e) {
                     console.error(e);
                 }
             }
        }, main.right);

        setTimeout(function(){
            main.fire(EVENTS.CREATE_DRAWER, drawer);
        },0);

        window.history.pushState(null, document.title, location.href);
        backButtonAction = function (event) {
           window.history.pushState(null, document.title, location.href);
           drawer.toggle();
        }

    };

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ADDRESS:
                if(main.users.getCountSelected() == 1 && this.properties.selected) {
                    actionbar.subtitle.innerHTML = object;
                    actionbar.subtitle.show();
                } else {
                    actionbar.subtitle.hide();
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                break;
            case EVENTS.TRACKING_DISABLED:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                window.removeEventListener("popstate", backButtonAction);
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                u.lang.updateNode(actionbar.titleNode, u.lang.connecting);
                u.lang.updateNode(drawer.headerTitle, u.lang.connecting);
                window.addEventListener("popstate", backButtonAction);
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    drawer.headerPrimary.innerHTML = main.me.properties.getDisplayName();
                }
                break;
            case EVENTS.SELECT_USER:
//            case EVENTS.SELECT_SINGLE_USER:
                onChangeLocation.call(this, this.location)
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
    }

    function onChangeLocation(location) {
        if(this && this.properties && this.properties.selected && main.users.getCountSelected() == 1) {
            actionbar.subtitle.show();
            this.fire(EVENTS.UPDATE_ACTIONBAR_SUBTITLE, actionbar.subtitle);
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:main",
                    title: u.lang.main,
                    items: [
                        {
                            id:"drawer:collapsed",
                            itemClassName: "media-hidden",
                            type: HTML.CHECKBOX,
                            label: u.lang.collapsed_drawer,
                            checked: u.load("drawer:collapsed"),
                            onaccept: function(e, event) {
                                drawer.toggleSize(this.checked);
                            },
                        }
                    ]
                }
            ]
        }
    }

    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
        options:options,
    }
}
