package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.robertkarl.gridimagesearch.app.settings.SearchSettingsActivity;
import net.robertkarl.gridimagesearch.app.settings.SearchSettingsModel;
import net.robertkarl.gridimagesearch.app.swipetodismiss.SwipeDismissListViewTouchListener;
import net.robertkarl.gridimagesearch.app.util.Connectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends Activity {

    public static String SEARCH_SETTINGS_EXTRA = "net.robertkarl.searchSettings";
    private GridView gvResults;
    private SearchView mSearchView;
    private ActionBarDrawerToggle mDrawerToggle;

    private SearchSettingsModel mSearchSettings;
    private DrawerLayout mDrawerLayout;

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

        setupNavListeners();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


    }

    void setupNavListeners() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,
                R.string.drawer_close
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
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ListView sideNav = (ListView)findViewById(R.id.left_drawer);
        sideNav.setAdapter(searchHistoryAdapter);

        sideNav.setOnTouchListener(new SwipeDismissListViewTouchListener(sideNav, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for (int i : reverseSortedPositions) {
                    searchHistoryAdapter.remove(searchHistoryAdapter.getItem(i));
                }
                searchHistoryAdapter.notifyDataSetChanged();
            }
        }));

        sideNav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchHistoryModel historyItem = (SearchHistoryModel)parent.getAdapter().getItem(position);
                mSearchSettings = historyItem.searchSettings;
                onSearch(historyItem.query);
            }
        });

        mDrawerLayout.openDrawer(Gravity.LEFT);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }



    /**
     * @param visible true if the Error state should be shown
     */
    void setErrorStateVisibility(final boolean visible) {
        SearchActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View errorView = findViewById(R.id.errorStateLayout);
                if (visible) {
                    updateErrorStateText(getString(R.string.error_state_maintext),
                            getString(R.string.error_state_subtext));
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

    void updateErrorStateText(String mainText, String flavorText) {
        TextView mainError = (TextView)findViewById(R.id.tvErrorStateMainText);
        TextView flavaFlave = (TextView)findViewById(R.id.tvErrorStateFlavorText);
        flavaFlave.setText(flavorText);
        mainError.setText(mainText);
    }

    /**
     * @param visible true if the empty (no results) state should be shown
     */
    void setEmptyStateVisibility(final boolean visible) {
        SearchActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout errorView = (RelativeLayout)findViewById(R.id.errorStateLayout);
                if (visible) {
                    updateErrorStateText(getString(R.string.empty_state_maintext),
                            getString(R.string.empty_state_subtext));
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
                    Toast.makeText(SearchActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                    mDrawerLayout.openDrawer(Gravity.LEFT);
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
        setupSearchBar();
        return true;
    }

    private void setupSearchBar() {
        mSearchView = (SearchView) findViewById(R.id.svImageQuery);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                onSearch(mSearchView.getQuery().toString());
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
                            onImagesReceived(ImageResult.fromJSONArray(imageJsonResults));
                        }
                        catch (JSONException e) {
                            // ignore
                            e.printStackTrace();
                            Log.d("DEBUG", e.toString());
                        }
                    }
                });
    }

    private void hideErrorStates() {
        setErrorStateVisibility(false);
        setEmptyStateVisibility(false);
    }

    private void showEmptyState() {
        setErrorStateVisibility(false);
        setEmptyStateVisibility(true);
    }

    private void onImagesReceived(ArrayList<? extends ImageResult> imageResults) {
        if (imageResults.isEmpty()) {
            showEmptyState();
        }
        else {
            hideErrorStates();
            imageAdapter.addAll(imageResults);
            adjustHistoryItemWithThumbnail(imageResults.get(imageResults.size() - 1).getThumbURL());
        }
    }

    void adjustHistoryItemWithThumbnail(String thumbnail) {
        if (getCurrentHistoryItem().thumbnailURL == null) {
            getCurrentHistoryItem().thumbnailURL = imageResults.get(0).getThumbURL();
            searchHistoryAdapter.notifyDataSetChanged();
        }
    }

    SearchHistoryModel getCurrentHistoryItem() {
        return searchHistoryAdapter.getItem(searchHistoryAdapter.getCount() - 1);
    }

    public void onSearch(String queryString) {
        Toast.makeText(this,String.format("Searching for %s", queryString), Toast.LENGTH_LONG).show();
        imageAdapter.clear();
        asyncAppendPageOfResults(0, queryString);

        SearchHistoryModel search = new SearchHistoryModel();
        search.query = queryString;
        search.searchSettings = mSearchSettings;
        searchHistoryAdapter.add(search);

        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mSearchSettings = (SearchSettingsModel)data.getSerializableExtra(SEARCH_SETTINGS_EXTRA);
        }
    }
}
