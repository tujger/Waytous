/**
 * Created 2/16/17.
 */
function GpsHolder(main) {

    var type = "gps";
    var geoTrackFilter = new GeoTrackFilter();

    function start() {

        /*var latLong;
        $.getJSON("https://ipinfo.io", function(ipinfo){
            console.log("Found location ["+ipinfo.loc+"] by ipinfo.io");
            latLong = ipinfo.loc.split(",");
        });*/

        navigator.geolocation.getCurrentPosition(function(location){
            locationUpdateListener(location);
            navigator.geolocation.watchPosition(locationUpdateListener, function(error){
                console.error(error);
            }, {
                enableHighAccuracy: true,
                maximumAge: 1000,
                timeout: 30000
            });
        },function(error){
            var message;
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    message = "You have denied geolocation.";
                    break;
                case error.PERMISSION_DENIED_TIMEOUT:
                    message = "User took too long to grant/deny geolocation permission.";
                    break;
                case error.POSITION_UNAVAILABLE:
                    message = "Geolocation is unavailable.";
                    break;
                case error.TIMEOUT:
                    message = "The request to geolocation timed out.";
                    break;
                default:
                    message = "An unknown error occurred while requesting geolocation.";
                    break;
            }

            var icon;
            var alert = u.dialog({
                className: "alert-dialog",
                items: [
                    { type: HTML.DIV, label: message + " Please resolve this problem and try again. Note that geolocation is required for working this service properly." },
                ],
                positive: {
                    label: "OK",
                    onclick: function(){
                        icon.classList.remove("hidden");
                        alert.onclose();
                    }
                },
                negative: {
                    onclick: function(){
                        icon.classList.remove("hidden");
                        alert.onclose();
                    }
                },
                help: function() {
                    main.fire(EVENTS.SHOW_HELP, {module: main.holders.gps, article: 1});
                 }
            }).onopen();

            icon = u.create(HTML.BUTTON, {className:"alert-icon hidden", type: HTML.BUTTON, innerHTML:"warning", onclick: function(){
                icon.classList.add("hidden");
                alert.classList.remove("hidden");
            }}, main.right);
        }, {
            enableHighAccuracy: true,
            maximumAge: 60000,
            timeout: 30000
        });
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.TRACKING_ACTIVE:
                if(main.me.location) {
                    var message = u.locationToJson(main.me.location);
                    main.tracking.sendMessage(REQUEST.TRACKING, message);
                }
                //
                // navigator.geolocation.getCurrentPosition(function(location){
                //     locationUpdateListener(location);
                // });
                break;
            default:
                break;
        }
        return true;
    }

    function locationUpdateListener(position) {

        if(!position) return;
        // position = geoTrackFilter.normalizeLocation(position);
        var last = main.me.location;
        if(last && last.coords && last.coords.latitude == position.coords.latitude && last.coords.longitude == position.coords.longitude) {
            return;
        }
        console.log("POSITION",position.coords.latitude, position.coords.longitude, position);

        var message = u.locationToJson(position);
        if(main.tracking && main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE) main.tracking.sendMessage(REQUEST.TRACKING, message);
        main.me.addLocation(position);
    }

    function GeoTrackFilter() {
        return {
            current:0,
            earthRadius: 6371009,
            lastTimeStep: null,
            kalmanFilter: null,

            normalizeLocation: function(position) {
                console.log(this.current,position);
                this.current ++;

                filter.update(position.coords.latitude, position.coords.longitude, position.timestamp);
                var latLng = filter.getLatLng();
                position.coords.latitude = latLng[0];
                position.coords.longitude = latLng[1];
                position.coords.heading = filter.getBearing();
                position.coords.speed = filter.getSpeed(position.coords.altitude)

                return position;
            }
        }
    }

    var help = {
        title: "Geolocation",
        1: {
            title: "Allow geolocation",
            body: "Instruction how to allow geolocation"
        }
    };

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        help:help,
    }
}