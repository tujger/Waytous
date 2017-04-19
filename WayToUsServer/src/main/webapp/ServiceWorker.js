var CACHE_NAME = "${APP_NAME}-v${SERVER_BUILD}";
var urlsToCache = [
  "/",
  "/css/tracking.css",
  "/images/logo.png",
  "/images/marker.svg",
  "/js/helpers/Constants.js",
  "/js/helpers/MyUser.js",
  "/js/helpers/MyUsers.js",
  "/js/helpers/NoSleep.js",
  "/js/helpers/TrackingFB.js",
  "/js/helpers/Utils.js",
  "/js/holders/AddressHolder.js",
  "/js/holders/ButtonHolder.js",
  "/js/holders/CameraHolder.js",
  "/js/holders/DistanceHolder.js",
  "/js/holders/DrawerHolder.js",
  "/js/holders/FabHolder.js",
  "/js/holders/GpsHolder.js",
  "/js/holders/HelpHolder.js",
  "/js/holders/MapHolder.js",
  "/js/holders/MarkerHolder.js",
  "/js/holders/MessagesHolder.js",
  "/js/holders/NavigationHolder.js",
  "/js/holders/PlaceHolder.js",
  "/js/holders/PropertiesHolder.js",
  "/js/holders/SavedLocationHolder.js",
  "/js/holders/SocialHolder.js",
  "/js/holders/StreetViewHolder.js",
  "/js/holders/TrackHolder.js",
  "/js/holders/TrackingHolder.js",
  "/js/holders/WelcomeHolder.js",
  "/js/tracking/Main.js",
  "/locales/resources.en-us.json"
];

self.addEventListener("install", function(event) {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log("Opened cache");
        return cache.addAll(urlsToCache);
      })
  );
});

self.addEventListener("fetch", function(event) {
  event.respondWith(
    caches.match(event.request)
      .then(function(response) {
        if (response) {
            console.log("Return cached",event.request);
            return response;
        }

        var fetchRequest = event.request.clone();

        return fetch(fetchRequest).then(
          function(response) {
            if(!response || response.status !== 200 || response.type !== "basic") {
                console.log("Return cached",response);
                return response;
            }

            var responseToCache = response.clone();

            caches.open(CACHE_NAME).then(function(cache) {
                cache.put(event.request, responseToCache);
            });
            console.log("Return cached",response);
            return response;
          }
        );
      })
    );
});

self.addEventListener("activate", function(event) {
  var cacheWhitelist = [CACHE_NAME];
  event.waitUntil(
    caches.keys().then(function(cacheNames) {
      return Promise.all(
        cacheNames.map(function(cacheName) {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});