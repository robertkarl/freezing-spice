package net.robertkarl.gridimagesearch.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImageResult {

    private String fullURL;
    private String thumbURL;

    public ImageResult(JSONObject json) {
        try {
            this.fullURL = json.getString("url");
            this.thumbURL = json.getString("tbUrl");
        }
        catch (JSONException e) {
            // ignore
        }
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public String toString() {
        return "<ImageResult \"" + thumbURL + "\">";
    }

    public String getFullURL() {
        return fullURL;
    }

    public static ArrayList<? extends ImageResult> fromJSONArray (JSONArray array) {
        ArrayList <ImageResult> results = new ArrayList<ImageResult>();
        for (int i = 0; i < array.length(); i++) {
            try {
                results.add(new ImageResult(array.getJSONObject(i)));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}
