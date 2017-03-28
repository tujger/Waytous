/**
 * Created 2/8/17.
 */
EVENTS.UPDATE_ACTIONBAR_SUBTITLE = "update_actionbar_subtitle";

DRAWER = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_NAVIGATION: 3,
    SECTION_VIEWS: 4,
    SECTION_MAP: 8,
    SECTION_LAST: 9
};

function DrawerHolder(main) {


    var drawer;
    var headerName;
    var sections;
    var title;
    var headerTitle;
    var subtitle;
    var menu;
    var alphaDialog;

    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        drawer = new Drawer();
//        target.addEventListener("touchmove", preventPullToRefresh);
        main.layout.insertBefore(drawer,main.layout.firstChild);

        var actionbar = u.create(HTML.DIV, {className:"actionbar"}, main.right);
        u.create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: function(){
            try {
                drawer.open();
            } catch(e) {
                console.error(e);
            }
        },onfocus:function(){}}, actionbar);
        var label = u.create(HTML.DIV, {className:"actionbar-label"}, actionbar);
        title = u.create(HTML.DIV, {className:"actionbar-label-title", innerHTML:main.appName}, label);
        subtitle = u.create(HTML.DIV, {className:"actionbar-label-subtitle"}, label);

        setTimeout(function(){
            main.fire(EVENTS.CREATE_DRAWER, drawer);
        },0);

        /*window.onhashchange = function(e) {
            console.log("HASHCHAN",e)
            drawer.open();
        }*/

        window.history.pushState(null, document.title, location.href);
        window.addEventListener("popstate", function (event) {
            window.history.pushState(null, document.title, location.href);
            drawer.open();
        });


////// FIXME - remove when no alpha
        alphaDialog = alphaDialog || u.dialog({
            className: "alert-dialog",
            items: [
                { type: HTML.DIV, innerHTML:u.lang.alpha_1 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_2 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_3 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_4 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_5 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_6 },
                { type: HTML.DIV, innerHTML:u.lang.alpha_7 },
            ],
            positive: {
                label: u.lang.ok,
                onclick: function(){
                    alphaDialog.close();
                }
            },
        });
        main.alpha.addEventListener("click", function(){
            alphaDialog.open();
        });



    };

    function Drawer() {
        var layout = u.create(HTML.DIV, {
            className:"drawer",
            tabindex: -1,
            onblur: function(){
                 this.close();
                 return true;
            },
            open: function() {
                 this.classList.add("drawer-open");
                 this.scrollTop = 0;
                 this.menu.scrollTop = 0;
                 this.focus();
            },
            close: function(){
                 this.classList.remove("drawer-open");
            },
         });

         layout.header = u.create(HTML.DIV, { className:"drawer-header" }, layout);
         u.create(HTML.IMG, {
            className:"drawer-header-logo",
            src:"/images/logo.svg",
            onclick: function(){
                alphaDialog.open();
            }
         }, layout.header);
         headerName = u.create(HTML.DIV, {className:"drawer-header-name", onclick: function(){
            layout.blur();
            main.me.fire(EVENTS.SELECT_SINGLE_USER);
        }}, layout.header);
         headerTitle = u.create(HTML.DIV, {className:"drawer-header-title", innerHTML:main.appName}, layout.header);
         u.create(HTML.DIV, {className:"drawer-header-subtitle", innerHTML: u.lang.be_always_on_the_same_way }, layout.header);


        layout.items = {};


        layout.menu = u.create(HTML.DIV, {className:"drawer-menu"}, layout);
        sections = [];
        for(var i=0;i<10;i++){
            sections[i] = u.create(HTML.DIV, {className:"hidden" + (i==9 ? "" : " divider")}, layout.menu);
        }


        layout.add = function(section,id,name,icon,callback) {
            layout.items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create(HTML.DIV, {
                className:"drawer-menu-item",
                onclick: function (event) {
                    setTimeout(function () {
                        layout.blur();
                        callback(event);
                    }, 100);
                },
                hide: function() {
                    this.classList.add("hidden");
                    this.fixShowing();
                    return this;
                },
                show: function() {
                    this.classList.remove("hidden");
                    this.fixShowing();
                    return this;
                },
                enable: function() {
                    this.classList.remove("disabled");
                    return this;
                },
                disable: function() {
                    this.classList.add("disabled");
                    return this;
                },
                fixShowing: function() {
                    var parent = th.parentNode;
                    var shown = false;
                    for(var i in parent.childNodes) {
                        if(parent.childNodes.hasOwnProperty(i)) {
                            if(!parent.childNodes[i].classList.contains("hidden")) shown = true;
                        }
                    }
                    if(shown) parent.classList.remove("hidden");
                    else parent.classList.add("hidden");
                },
                increaseBadge: function() {
                    var val = parseInt(this.badge.innerHTML || "0");
                    val ++;
                    this.badge.innerHTML = val;
                    this.showBadge();
                },
                showBadge: function() {
                    this.badge.classList.remove("hidden");
                },
                hideBadge: function() {
                    this.badge.classList.add("hidden");
                    this.badge.innerHTML = "0";
                },

            }, sections[section]);

            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"drawer-menu-item-icon", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                u.create(HTML.DIV, {
                    className: "drawer-menu-item-label",
                    innerHTML: name
                }, th);
            }
            th.badge = u.create(HTML.DIV, { className:"drawer-menu-item-badge hidden", innerHTML: "0" }, th);
            sections[section].classList.remove("hidden");
            return th;
        }

        layout.footer = u.create(HTML.DIV, { className:"drawer-footer"}, layout);
        u.create(HTML.DIV, main.appName + " &copy;2017 WTG", layout.footer);
        u.create(HTML.DIV, "Build " + data.version, layout.footer);

        return layout;
    }

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ADDRESS:
                if(main.users.getCountSelected() == 1 && this.properties.selected) {
                    subtitle.innerHTML = object;
                    subtitle.classList.remove("hidden");
                } else {
                    subtitle.classList.add("hidden");
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
            case EVENTS.TRACKING_DISABLED:
                title.innerHTML = main.appName;
                headerTitle.innerHTML = main.appName;
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                u.lang.updateNode(title, u.lang.connecting);
                u.lang.updateNode(headerTitle, u.lang.connecting);
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    headerName.innerHTML = main.me.properties.getDisplayName();
                }
                break;
            case EVENTS.SELECT_USER:
            case EVENTS.SELECT_SINGLE_USER:
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
            subtitle.show();
            this.fire(EVENTS.UPDATE_ACTIONBAR_SUBTITLE, subtitle);
        }
    }

    /*function preventPullToRefresh(e){
        var scrolly = target.pageYOffset || target.scrollTop || 0;
        var direction = e.changedTouches[0].pageY > last_y ? 1 : -1;
        if(direction>0 && scrolly===0){
           e.preventDefault();
        }
        last_y = e.changedTouches[0].pageY;
    }*/

    var resources = {
        connecting: "Connecting...",
        be_always_on_the_same_way: "Be always on the same way\nwith your friends",

        alpha_1: "Thank you for using the",
        alpha_2: "ALPHA version of Waytogo.",
        alpha_3: "&nbsp;",
        alpha_4: "Please if you found some errors, weird behaviour, new great idea or just because - feel free to send us an e-mail:",
        alpha_5: "<a href=\"mailto:support@waytogo.us\">support@waytogo.us</a>.",
        alpha_6: "&nbsp;",
        alpha_7: "<a href=\"/\" target=\"_blank\">Go to the main page of project (new window).</a>",

    }

    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
        resources:resources,
        onChangeLocation:onChangeLocation,
    }
}

