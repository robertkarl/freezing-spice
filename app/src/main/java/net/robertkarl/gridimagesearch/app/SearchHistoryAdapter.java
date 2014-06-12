package net.robertkarl.gridimagesearch.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.loopj.android.image.SmartImageView;

import net.robertkarl.gridimagesearch.app.swipetodismiss.SwipeDismissTouchListener;

import java.util.List;

public class SearchHistoryAdapter extends ArrayAdapter<SearchHistoryModel> {


    public SearchHistoryAdapter(Context context, List<SearchHistoryModel> searches) {
        super(context, R.layout.history_item_row, searches);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SearchHistoryModel search = getItem(position);
        View tvSearchRow;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            tvSearchRow = inflater.inflate(R.layout.history_item_row, parent, false);
        }
        else {
            tvSearchRow = convertView;
        }

        tvSearchRow.setOnTouchListener(new SwipeDismissTouchListener(tvSearchRow, null, new SwipeDismissTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(Object token) {
                return true;
            }

            @Override
            public void onDismiss(View view, Object token) {
                SearchHistoryAdapter.this.remove(search);
                Log.d("DEBUG", String.format("Dismissing item. %s", token.toString()));
                SearchHistoryAdapter.this.notifyDataSetChanged();
            }

        }));

        TextView queryTextView = (TextView)tvSearchRow.findViewById(R.id.tvHistoryRowQuery);
        queryTextView.setText(String.format("\"%s\"", search.query));


        SmartImageView imageView = (SmartImageView)tvSearchRow.findViewById(R.id.historyRowSearchPreview);
        imageView.setImageUrl(search.thumbnailURL);

        return tvSearchRow;
    }

}
