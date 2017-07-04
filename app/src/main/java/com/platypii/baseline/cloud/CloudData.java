package com.platypii.baseline.cloud;

import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing online track info
 */
public class CloudData {
    private static final String TAG = "CloudData";

    public final String track_id;
    public final long date;
    public final String date_string;
    public final String trackUrl;
    public final String trackKml;
    public final String location;

    private CloudData(String track_id, long date, String date_string, String trackUrl, String trackKml, String location) {
        this.track_id = track_id;
        this.date = date;
        this.date_string = date_string;
        this.trackUrl = trackUrl;
        this.trackKml = trackKml;
        this.location = location;
    }

    static CloudData fromJson(JSONObject json) throws JSONException {
        final String track_id = json.getString("track_id");
        final long date = json.getLong("date");
        final String date_string = json.optString("date_string");
        final String trackUrl = json.optString("trackUrl");
        final String trackKml = json.optString("trackKml");
        final String location = json.optString("location");
        return new CloudData(track_id, date, date_string, trackUrl, trackKml, location);
    }

    JSONObject toJson() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("track_id", track_id);
            obj.put("date", date);
            obj.put("date_string", date_string);
            obj.put("trackUrl", trackUrl);
            obj.put("trackKml", trackKml);
            obj.put("location", location);
            return obj;
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            return null;
        }
    }

}
