/**
 * Created 2/8/17.
 */
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
    var drawerLayout;
    var menu;

    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        drawer = new Drawer();
//        target.addEventListener("touchmove", preventPullToRefresh);

        drawerLayout = u.create(HTML.DIV, {className:"drawer", tabindex: 1, onblur: function(){
            drawerLayout.classList.remove("drawer-open");
            return true;
        }}, main.layout, "first");

        var actionbar = u.create(HTML.DIV, {className:"actionbar"}, main.right);
        u.create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: function(){
            try {
                drawerLayout.classList.add("drawer-open");
                menu.scrollTop = 0;
                drawerLayout.focus();
            } catch(e) {
                console.error(e);
            }
        },onfocus:function(){}}, actionbar);
        var label = u.create(HTML.DIV, {className:"actionbar-label"}, actionbar);
        title = u.create(HTML.DIV, {className:"actionbar-label-title", innerHTML:main.appName}, label);
        subtitle = u.create(HTML.DIV, {className:"actionbar-label-subtitle hidden"}, label);

        var header = u.create(HTML.DIV, { className:"drawer-header" }, drawerLayout);
        headerName = u.create(HTML.DIV, {className:"drawer-header-name"}, header);
        headerTitle = u.create(HTML.DIV, {className:"drawer-header-title", innerHTML:main.appName}, header);
        u.create(HTML.DIV, {className:"drawer-header-subtitle", innerHTML:"Be always on the same way\nwith your friends"}, header);

        menu = u.create(HTML.DIV, {className:"menu"}, drawerLayout);
        sections = [];
        for(var i=0;i<10;i++){
            sections[i] = u.create(HTML.DIV, {className:"hidden" + (i==9 ? "" : " divider")}, menu);
        }

        main.fire(EVENTS.CREATE_DRAWER, drawer);

        var th = u.create(HTML.DIV, { className:"drawer-footer"}, drawerLayout);
        u.create(HTML.DIV, main.appName + " &copy;2017 WTG", th);
        u.create(HTML.DIV, "Build " + data.version, th);

    };

    function Drawer() {
        var items = {};

        function add(section,id,name,icon,callback) {
            items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create(HTML.DIV, {className:"menu-item"}, sections[section]);
            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"menu-item-icon", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                u.create(HTML.DIV, {
                    onclick: function (event) {
                        setTimeout(function () {
                            drawerLayout.blur();
                            callback(event);
                        }, 100);
                    },
                    className: "menu-item-label",
                    innerHTML: name
                }, th);
            }
            sections[section].classList.remove("hidden");
            return th;
        }
        function getDrawer(){
            console.log("GETDRAWER:",items);
        }

        return {
            add:add,
            getDrawer:getDrawer,
        }
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
                title.innerHTML = "Connecting...";
                headerTitle.innerHTML = "Connecting...";
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    headerName.innerHTML = main.me.properties.getDisplayName();
                }
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
    }

    /*function preventPullToRefresh(e){
        var scrolly = target.pageYOffset || target.scrollTop || 0;
        var direction = e.changedTouches[0].pageY > last_y ? 1 : -1;
        if(direction>0 && scrolly===0){
           e.preventDefault();
        }
        last_y = e.changedTouches[0].pageY;
    }*/

    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
    }
}

