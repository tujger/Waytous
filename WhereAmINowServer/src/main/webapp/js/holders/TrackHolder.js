/**
 * Created 3/9/17.
 */
EVENTS.SHOW_TRACK = "show_track";
EVENTS.HIDE_TRACK = "hide_track";

function TrackHolder(main) {

    var type = "track";
    var view;
    var drawerItemShow;
    var drawerItemHide;


    function start() {
        view = {};
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemShow = object.add(DRAWER.SECTION_VIEWS,EVENTS.SHOW_TRACK, u.lang.show_tracks,"title",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SHOW_TRACK);
                        drawerPopulate();
                    });
                });
                drawerItemHide = object.add(DRAWER.SECTION_VIEWS,EVENTS.HIDE_TRACK, u.lang.hide_tracks,"format_strikethrough",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_TRACK);
                        drawerPopulate();
                    });
                });
                drawerPopulate();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user.type == "user" && user.location && !user.views.track.show) {
                    object.add(MENU.SECTION_VIEWS,EVENTS.SHOW_TRACK, u.lang.show_track,"title",function(){
                        user.fire(EVENTS.SHOW_TRACK);
                        drawerPopulate();
                    });
                } else if(user.views.track.show) {
                    object.add(MENU.SECTION_VIEWS,EVENTS.HIDE_TRACK,u.lang.hide_track,"format_strikethrough",function(){
                        user.fire(EVENTS.HIDE_TRACK);
                        drawerPopulate();
                    });
                }
                break;
            case EVENTS.SHOW_TRACK:
                if(this && this.views && this.views.track) {
                    this.views.track.show = true;
                    u.saveForGroup("track:show:" + this.number, true);
                    show.call(this);
                }
                break;
            case EVENTS.HIDE_TRACK:
                removeView(this);
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        var view = {
            user:myUser,
            show:u.loadForGroup("track:show:" + myUser.number)
        };
        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
    }

    function removeView(user){
        if(!user) return;
        user.views.track.show = false;
        u.saveForGroup("track:show:" + user.number);
        if(user.views && user.views.track && user.views.track.track) {
            user.views.track.track.setMap(null);
            user.views.track.track = null;
        }
    }

    function drawerPopulate() {
        setTimeout(function(){
            drawerItemHide.hide();
            drawerItemShow.hide();
            if(main.tracking && main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE) {
                main.users.forAllUsers(function (number, user) {
                    if(user.views.track) {
                        if (user.views.track.show) {
                            drawerItemHide.show();
                        } else {
                            drawerItemShow.show();
                        }
                    }
                })
            }
        },0);
    }

    function show() {
        // if(!this.views.track) {
        //     this.views.track = createView(this);
        // }
        if(!this || !this.views || !this.views.track || !this.views.track.show) return;

        if(this.locations && this.locations.length > 1) {
            if(!this.views.track.track) {
                var points = [];
                for(var i in this.locations) {
                    points.push(u.latLng(this.locations[i]));
                }
                this.views.track.track = new google.maps.Polyline({
                    path: points,
                    geodesic: true,
                    strokeColor: this.properties.color,
                    strokeOpacity: 0.6,
                    strokeWeight: 8,
                    map: main.map
                });
                // this.views.track.track.setMap(main.map);
            } else {
                this.views.track.track.getPath().push(u.latLng(this.location));
            }

        }

    }

    function onChangeLocation(location) {
        show.call(this);
        // console.log("SAMPLEONCHANGELOCATION",this,location);
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