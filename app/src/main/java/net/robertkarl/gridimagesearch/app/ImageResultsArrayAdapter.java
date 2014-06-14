package net.robertkarl.gridimagesearch.app;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class ImageResultsArrayAdapter extends ArrayAdapter<ImageResult> {

    public ImageResultsArrayAdapter(Context context, List<ImageResult> images) {
        super(context, android.R.layout.simple_list_item_1, images);
    }

}

