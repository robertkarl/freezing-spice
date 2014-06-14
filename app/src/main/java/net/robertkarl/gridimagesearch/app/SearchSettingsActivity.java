package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

/**
 * Activity allowing user to set image color, type, size filters.
 */
public class SearchSettingsActivity extends Activity {

    ArrayList <Spinner>mSettingsSpinners = new ArrayList<Spinner>();
    ArrayList <SpinnerAdapter>mSpinnerAdapters = new ArrayList<SpinnerAdapter>();

    private SearchSettingsModel mSearchSettings;

    private EditText etSiteFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_settings);

        mSearchSettings = (SearchSettingsModel)getIntent()
                .getSerializableExtra(SearchActivity.SEARCH_SETTINGS_EXTRA);

        /// Note: changing the order of these calls will cause fields to be saved in the wrong places
        appendAndSetupSpinner(R.id.imageSizeSpinner, R.array.image_sizes);
        appendAndSetupSpinner(R.id.imageColorSpinner, R.array.image_colors);
        appendAndSetupSpinner(R.id.imageTypeSpinner, R.array.image_types);


        setupSiteFilterEdit();
    }

    private void setupSiteFilterEdit() {
        etSiteFilter = (EditText)findViewById(R.id.etSiteFilter);
        etSiteFilter.setText(mSearchSettings.siteFilter);
        etSiteFilter.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    /// Just consume it.
                    return true;
                }
                mSearchSettings.siteFilter = etSiteFilter.getText().toString();
                return false;
            }
        });
        etSiteFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mSearchSettings.siteFilter = etSiteFilter.getText().toString();
                }
            }
        });
    }

    private void appendAndSetupSpinner(int spinnerID, int preferenceStrings) {
        final Spinner spinner = (Spinner)findViewById(spinnerID);
        mSettingsSpinners.add(spinner);
        SpinnerAdapter adapter = ArrayAdapter.createFromResource(this,
                preferenceStrings, android.R.layout.simple_spinner_item);
        mSpinnerAdapters.add(adapter);
        spinner.setAdapter(adapter);

        String savedPreferenceName;
        int spinnerIndex = mSettingsSpinners.indexOf(spinner);
        switch (spinnerIndex) {
            case 0:
                savedPreferenceName = mSearchSettings.imageSize;
                break;
            case 1:
                savedPreferenceName = mSearchSettings.imageColor;
                break;
            case 2:
                savedPreferenceName = mSearchSettings.imageType;
                break;
            default:
                savedPreferenceName = null;
        }

        int startIndex = indexForItem(adapter, savedPreferenceName);
        spinner.setSelection(startIndex);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int spinnerIndex = mSpinnerAdapters.indexOf(parent.getAdapter());
                String settingValue = parent.getAdapter().getItem(position).toString();
                switch (spinnerIndex) {
                    case 0:
                        mSearchSettings.imageSize = settingValue;
                        break;
                    case 1:
                        mSearchSettings.imageColor = settingValue;
                        break;
                    case 2:
                        mSearchSettings.imageType = settingValue;
                        break;
                    default:
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /// Return the name of the spinner item for the given adapter
    /// at the given index.
    private int indexForItem(Adapter adapter, String item) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(item)) {
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
        finish();
    }

}
