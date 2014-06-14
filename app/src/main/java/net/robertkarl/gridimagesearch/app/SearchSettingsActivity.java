package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SearchSettingsActivity extends Activity {

    private Spinner imageSizeSpinner;
    private ArrayAdapter<CharSequence> imageSizeAdapter;
    private SearchSettingsModel mSearchSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_settings);

        mSearchSettings = (SearchSettingsModel)getIntent().getSerializableExtra(SearchActivity.SEARCH_SETTINGS_EXTRA);

        setupSizeSpinner();
    }

    private void setupSizeSpinner() {
        imageSizeSpinner = (Spinner)findViewById(R.id.imageSizeSpinner);
        imageSizeAdapter = ArrayAdapter.createFromResource(this,
                R.array.image_sizes, android.R.layout.simple_spinner_item);
        imageSizeSpinner.setAdapter(imageSizeAdapter);

        int startIndex = indexForItem(mSearchSettings.imageSize);
        imageSizeSpinner.setSelection(startIndex);

        imageSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSearchSettings.imageSize = imageSizeAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    int indexForItem(String item) {
        for (int i = 0; i < imageSizeAdapter.getCount(); i++) {
            if (imageSizeAdapter.getItem(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBackPressed() {
        Intent answer = new Intent();
        answer.putExtra(SearchActivity.SEARCH_SETTINGS_EXTRA, mSearchSettings);
        setResult(RESULT_OK, answer);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
