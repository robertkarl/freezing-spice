package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SearchSettingsActivity extends Activity {

    private Spinner imageSizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_settings);

        setupSizeSpinner();

    }

    private void setupSizeSpinner() {
        imageSizeSpinner = (Spinner)findViewById(R.id.imageSizeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.image_sizes, android.R.layout.simple_spinner_item);
        imageSizeSpinner.setAdapter(adapter);
    }

}
