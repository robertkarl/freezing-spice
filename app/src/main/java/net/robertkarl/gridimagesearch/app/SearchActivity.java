package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.robertkarl.gridimagesearch.app.settings.SearchSettingsActivity;
import net.robertkarl.gridimagesearch.app.settings.SearchSettingsModel;
import net.robertkarl.gridimagesearch.app.util.Connectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SearchActivity extends Activity {

    public static String SEARCH_SETTINGS_EXTRA = "net.robertkarl.searchSettings";
    private GridView gvResults;
    private SearchView mSearchView;
    private GifMovieView mGearsView;
    private ActionBarDrawerToggle mDrawerToggle;

    private SearchSettingsModel mSearchSettings;

    public static String FULLSCREEN_IMAGE_KEY = "fullImageURL";

    ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
    ImageResultsArrayAdapter imageAdapter;

    SearchHistoryAdapter searchHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_search);
        setupSubviews();

        setupSideNav();

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

        /// Don't check for connectivity in the emulator -- ping does not work there
        if (!Build.FINGERPRINT.startsWith("generic")) {
            checkBackForAConnection(0);
        }
    }

    private void setupSideNav() {
        searchHistoryAdapter = new SearchHistoryAdapter(this, new ArrayList<SearchHistoryModel>());
        SearchHistoryModel searchItem = new SearchHistoryModel();
        searchItem.query = "whatev";
        searchItem.searchSettings = new SearchSettingsModel();
        searchHistoryAdapter.add(searchItem);

        ListView sideNav = (ListView)findViewById(R.id.left_drawer);
        sideNav.setAdapter(searchHistoryAdapter);


        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(R.string.drawer_close);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.drawer_open);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }



    /**
     * Safe to call from any thread
     * @param visible true if the Error state should be shown
     */
    void setErrorStateVisibility(final boolean visible) {
        SearchActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout errorView = (LinearLayout)findViewById(R.id.llEmptyState);
                if (visible) {
                    errorView.setVisibility(View.VISIBLE);
                    gvResults.setVisibility(View.GONE);
                    addGearsViewIfNeeded();
                }
                else {
                    errorView.setVisibility(View.GONE);
                    gvResults.setVisibility(View.VISIBLE);
                    if (mGearsView != null) {
                        errorView.removeView(mGearsView);
                        mGearsView = null;
                    }
                }
            }
        });

    }

    void addGearsViewIfNeeded() {
        if (mGearsView == null) {
            mGearsView = newGearsGif();
            mGearsView.setLayoutParams(new LinearLayout.LayoutParams(344, 341));
            LinearLayout emptyStateContainer = (LinearLayout)findViewById(R.id.llEmptyState);
            emptyStateContainer.addView(mGearsView);
        }
    }

    GifMovieView newGearsGif() {
        InputStream stream;
        try {
            stream = getAssets().open("gears.gif");
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        GifMovieView gifView = new GifMovieView(this, stream);
        return gifView;
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
