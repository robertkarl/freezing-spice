package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.smoothie.AsyncListView;

import java.util.ArrayList;

public class SearchActivity extends Activity {

    public static String SEARCH_SETTINGS_EXTRA = "net.robertkarl.searchSettings";
    private EditText etQuery;
    private Button btnSearch;
    private GridView gvResults;

    private SearchSettingsModel mSearchSettings;

    public static String FULLSCREEN_IMAGE_KEY = "fullImageURL";

    ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
    ImageResultsArrayAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupSubviews();

        AsyncListView listView;

        imageAdapter = new ImageResultsArrayAdapter(this, imageResults);
        gvResults.setAdapter(imageAdapter);

        mSearchSettings = new SearchSettingsModel();
        mSearchSettings.imageSize = "small";

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                ImageResult result = imageAdapter.getItem(position);
                i.putExtra(FULLSCREEN_IMAGE_KEY, result);
                startActivity(i);
            }
        });
    }

    private void setupSubviews() {
        etQuery = (EditText)findViewById(R.id.etQuery);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        gvResults = (GridView)findViewById(R.id.gvResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SearchSettingsActivity.class);
            i.putExtra(SearchActivity.SEARCH_SETTINGS_EXTRA, mSearchSettings);
            startActivityForResult(i, 420);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchClicked(View v) {
        String query = etQuery.getText().toString();
        Toast.makeText(this,String.format("Searching for %s", query), Toast.LENGTH_LONG).show();

        AsyncHttpClient client = new AsyncHttpClient();
        String URL = String.format("http://ajax.googleapis.com/ajax/services/search/images?imgsz=%s&rsz=8&start=%d&v=1.0&q=%s", mSearchSettings.imageSize, 0, query);
        client.get(URL,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        JSONArray imageJsonResults = null;
                        try {
                            imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
                            imageResults.clear();
                            imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));

                            Log.d("DEBUG", imageResults.toString());
                        }
                        catch (JSONException e) {
                            // ignore
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mSearchSettings = (SearchSettingsModel)data.getSerializableExtra(SEARCH_SETTINGS_EXTRA);
            Log.i("DEBUG", "New image size is " +mSearchSettings.imageSize);
        }
        else if (resultCode == RESULT_CANCELED) {
            int x = 0;
            x++;
            Log.i("DEBUG", "Cancelled loading image size.");
        }
    }
}
