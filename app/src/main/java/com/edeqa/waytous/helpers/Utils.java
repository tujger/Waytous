package com.edeqa.waytous.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;


/**
 * Created 10/8/16.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class Utils {


//    public static final int DIGEST_METHOD_MD2 = 2;
//    public static final int DIGEST_METHOD_MD5 = 5;
//    public static final int DIGEST_METHOD_SHA1 = 1;
//    public static final int DIGEST_METHOD_SHA256 = 256;
//    public static final int DIGEST_METHOD_SHA512 = 512;
    public static final int MATCH_SCREEN = -100;

//    public static String getEncryptedHash_old(String str) {
//        return getEncryptedHash(str, 5);
//    }

    @SuppressWarnings("WeakerAccess")
    public static float[] getColorMatrix(int color) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        return new float[] {
                1, 0, 0, 0, r,
                0, 1, 0, 0, g,
                0, 0, 1, 0, b,
                0, 0, 0, 1, 0
        };
    }

    public static Location jsonToLocation(JSONObject json) throws JSONException {
        Location loc = new Location(json.getString(USER_PROVIDER));
        loc.setLatitude(json.getDouble(USER_LATITUDE));
        loc.setLongitude(json.getDouble(USER_LONGITUDE));
        loc.setAltitude(json.has(USER_ALTITUDE) ? json.getDouble(USER_ALTITUDE) : 0);
        loc.setAccuracy(json.has(USER_ACCURACY) ? (float) json.getDouble(USER_ACCURACY) : 0);
        loc.setBearing(json.has(USER_BEARING) ? (float) json.getDouble(USER_BEARING): 0);
        loc.setSpeed(json.has(USER_SPEED) ? (float) json.getDouble(USER_SPEED): 0);
        loc.setTime(json.getLong(REQUEST_TIMESTAMP));
        return loc;
    }

    public static JSONObject locationToJson(Location location) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(USER_PROVIDER,location.getProvider());
        json.put(USER_LATITUDE,restrictPrecision(location.getLatitude(), 5));
        json.put(USER_LONGITUDE,restrictPrecision(location.getLongitude(), 5));
        json.put(USER_ALTITUDE,restrictPrecision(location.getAltitude(),5));
        json.put(USER_ACCURACY,restrictPrecision(location.getAccuracy(), 1));
        json.put(USER_BEARING,restrictPrecision(location.getBearing(),1));
        json.put(USER_SPEED,restrictPrecision(location.getSpeed(),1));
        json.put(REQUEST_TIMESTAMP,location.getTime());
        return json;
    }

    public static double restrictPrecision(double value, int floating) {
        long multi = (long) Math.pow(10, floating);
        long l = (long) (value * multi);
        return l * 1D /multi;
    }

    public static Drawable renderDrawable(Context context, int resource, int color){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        return renderDrawable(context,resource,color,width,height);
    }

    public static Drawable renderDrawable(Context context, int resource, int color, int width, int height){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);

        }
        drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(color)));
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return drawable;
    }

    public static Bitmap renderBitmap(Context context, int resource, int color){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        return renderBitmap(context,resource,color,width,height);
    }

    public static Bitmap renderBitmap(Context context, int resource, int color, int width, int height){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);

        }
        drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(color)));
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    public static Location normalizeLocation(GeoTrackFilter filter, Location location) {
        filter.update_velocity2d(location.getLatitude(),location.getLongitude(),location.getTime());
        double[] latlng = filter.get_lat_long();
        location.setLatitude(latlng[0]);
        location.setLongitude(latlng[1]);
        if(OPTIONS.isDebugMode()) location.setBearing((float) filter.get_bearing());
        location.setSpeed((float) filter.get_speed(location.getAltitude()));
        return location;

    }

    /** Read the object from Base64 string. */
    public static Object deserializeFromString( String s ) {
        Object o = null;
        try {
            byte [] data = Base64.decode( s, android.util.Base64.DEFAULT);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o  = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String serializeToString( Serializable o ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( o );
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static LatLng findPoint(List<LatLng> points, double fraction) {

        double length = 0;
        for(int i=1; i<points.size();i++) {
            length += SphericalUtil.computeDistanceBetween(points.get(i-1),points.get(i));
        }

        length = length * fraction;

        for(int i=1; i<points.size();i++) {
            double current = SphericalUtil.computeDistanceBetween(points.get(i-1),points.get(i));
            if(length - current < 0) {
                return SphericalUtil.interpolate(points.get(i-1),points.get(i), length / current);
            } else {
                length -= current;
            }
        }
        return SphericalUtil.interpolate(points.get(0),points.get(points.size()-1), fraction);
    }

    public static LatLng latLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static LatLngBounds reduce(LatLngBounds bounds, double fraction) {

        LatLng newNortheast = SphericalUtil.interpolate(bounds.northeast, bounds.southwest, (1+fraction)/2);
        LatLng newSouthwest = SphericalUtil.interpolate(bounds.southwest, bounds.northeast, (1+fraction)/2);

        return new LatLngBounds(newNortheast,newSouthwest);
    }

    public static void updateMarkerPosition(final GoogleMap map, final Marker marker, final List<LatLng> points) {
        if(marker == null || points == null || points.size() < 2) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    LatLng mePosition = points.get(0);
                    LatLng userPosition = points.get(points.size() - 1);
                        LatLng markerPosition = Utils.findPoint(points, .5);
                        LatLngBounds bounds = Utils.reduce(map.getProjection().getVisibleRegion().latLngBounds, .8);
                        if (!bounds.contains(markerPosition) && (bounds.contains(mePosition) || bounds.contains(userPosition))) {
                            if (!bounds.contains(markerPosition)) {
                                double fract = 0.5;
                                while (!bounds.contains(markerPosition)) {
                                    fract = fract + (bounds.contains(mePosition) ? -1 : +1) * .01;
                                    if (fract < 0 || fract > 1) break;
                                    markerPosition = Utils.findPoint(points, fract);
                                }
                            }
                        }
                        marker.setPosition(markerPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void resizeDialog(Activity activity, Dialog dialog, int width, int height) {
        if(width == MATCH_SCREEN || height == MATCH_SCREEN) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            if(width == MATCH_SCREEN) {
                width = displaymetrics.widthPixels;
            }
            if(height == MATCH_SCREEN) {
                int result = 0;
                try {
                    int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android"); //NON-NLS
                    if (resourceId > 0) {
                        result = activity.getResources().getDimensionPixelSize(resourceId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                height = displaymetrics.heightPixels - result;
            }
        }
        //noinspection ConstantConditions
        dialog.getWindow().setLayout(width, height);
    }

    @SuppressWarnings("SameParameterValue")
    public static int adaptedSize(Context context, int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, context.getResources().getDisplayMetrics());
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public static void log(Object... text) {
        StringBuilder str = new StringBuilder();
        String tag = "Utils";
        int count = 0;
        for (Object aText : text) {
            if(aText == null) {
                str.append("null ");
            } else if(aText instanceof Serializable) {
                str.append(aText.toString()).append(" ");
            } else if((count++) == 0) {
//                str += aText.getClass().getSimpleName() + ": ";
                tag = aText.getClass().getSimpleName();
            } else {
                str.append(aText.toString()).append(" ");
            }
        }
        Log.i(tag, str.toString());
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public static void err(Object... text) {
        StringBuilder str = new StringBuilder();
        String tag = "Utils";
        Throwable e = null;
        int count = 0;
        for (Object aText : text) {
            if(aText == null) {
                str.append("null ");
            } else if (aText instanceof Throwable) {
                str.append(aText).append(" ");
                e = (Throwable) aText;
            } else if(aText instanceof Serializable) {
                str.append(aText.toString()).append(" ");
            } else if((count++) == 0) {
                tag = aText.getClass().getSimpleName();
//                str += aText.getClass().getSimpleName() + ": ";
            } else {
                str.append(aText.toString()).append(" ");
            }
        }
        Log.e(tag, str.toString());
        if(e != null) e.printStackTrace();
    }


    public static String getWrappedHttpPort(){
        return OPTIONS.getHttpPortMasked() == 80 ? "" : ":" + OPTIONS.getHttpPortMasked();
    }

    public static String getWrappedHttpsPort(){
        return OPTIONS.getHttpsPortMasked() == 443 ? "" : ":" + OPTIONS.getHttpsPortMasked();
    }

    public static Bitmap getImageBitmap(String url) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage()); //NON-NLS
            e.printStackTrace();
        }
        return mIcon11;

//        Bitmap bm = null;
//        try {
//            URL aURL = new URL(url);
//            URLConnection conn = aURL.openConnection();
//            conn.connect();
//            InputStream is = conn.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//            bm = BitmapFactory.decodeStream(bis);
//            bis.close();
//            is.close();
//        } catch (IOException e) {
//            Log.e("Utils", "Error getting bitmap", e); //NON-NLS
//        }
//        return bm;
    }


}
