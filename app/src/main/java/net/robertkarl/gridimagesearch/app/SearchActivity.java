package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.robertkarl.gridimagesearch.app.util.Connectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends Activity {

    public static String SEARCH_SETTINGS_EXTRA = "net.robertkarl.searchSettings";
    private GridView gvResults;
    private SearchView mSearchView;

    private SearchSettingsModel mSearchSettings;

    public static String FULLSCREEN_IMAGE_KEY = "fullImageURL";

    ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
    ImageResultsArrayAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupSubviews();

        imageAdapter = new ImageResultsArrayAdapter(this, imageResults);
        gvResults.setAdapter(imageAdapter);

        mSearchSettings = new SearchSettingsModel();

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                ImageResult result = imageAdapter.getItem(position);
                i.putExtra(FULLSCREEN_IMAGE_KEY, result);
                startActivity(i);
            }
        });

        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                asyncAppendPageOfResults(page, mSearchView.getQuery().toString());
            }
        });
        checkBackForAConnection(0);
    }

    /**
     * Safe to call from any thread
     * @param visible true if the Error state should be shown
     */
    void setErrorStateVisibility(final boolean visible) {
        SearchActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View errorView = findViewById(R.id.tvEmptyState);
                if (visible) {
                    errorView.setVisibility(View.VISIBLE);
                    gvResults.setVisibility(View.GONE);
                }
                else {
                    errorView.setVisibility(View.GONE);
                    gvResults.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * Perform exponential backoff checking for internet connectivity
     * @param delay milliseconds later for initial check.
     */
    private void checkBackForAConnection(final int delay) {
        Log.d("DEBUG", "Checking connectivity");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Connectivity.pingGoogleSynchronous()) {
                    setErrorStateVisibility(false);
                }
                else {
                    Log.d("DEBUG", String.format("Checking server connection in %d millis", delay * 2));
                    checkBackForAConnection(delay == 0 ? 500 : delay * 2);
                    setErrorStateVisibility(true);
                }
            }
        }, delay);
    }

    private void setupSubviews() {
        gvResults = (GridView)findViewById(R.id.gvResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        setupSearchBar(menu);
        return true;
    }

    private void setupSearchBar(Menu menu) {
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                onSearchClicked();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

    private void asyncAppendPageOfResults(int resultsPage, String queryString) {

        AsyncHttpClient client = new AsyncHttpClient();
        boolean x = mSearchSettings.imageColor.equals(null);
        String URLBase = "http://ajax.googleapis.com/ajax/services/search/images";
        String URL = URLBase + String.format("?imgtype=%s&imgcolor=%s&imgsz=%s&rsz=8&start=%d&v=1.0&q=%s",
                mSearchSettings.imageType.equals("all") ? "" : mSearchSettings.imageType,
                mSearchSettings.imageColor.equals("all") ? "" : mSearchSettings.imageColor,
                mSearchSettings.imageSize.equals("all") ? "" : mSearchSettings.imageSize,
                resultsPage * 8,
                queryString);
        client.get(URL,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        JSONArray imageJsonResults = null;
                        try {
                            Log.d("DEBUG", response.getJSONObject("responseData").getJSONObject("cursor").toString());
                            imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
                            imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
                        }
                        catch (JSONException e) {
                            // ignore
                            e.printStackTrace();
                            Log.d("DEBUG", e.toString());
                        }
                    }
                });
    }

    public void onSearchClicked() {
        String queryString = mSearchView.getQuery().toString();
        Toast.makeText(this,String.format("Searching for %s", queryString), Toast.LENGTH_LONG).show();
        imageAdapter.clear();
        asyncAppendPageOfResults(0, queryString);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mSearchSettings = (SearchSettingsModel)data.getSerializableExtra(SEARCH_SETTINGS_EXTRA);
        }
    }
}
