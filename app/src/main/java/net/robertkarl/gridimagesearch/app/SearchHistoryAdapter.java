package net.robertkarl.gridimagesearch.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SearchHistoryAdapter extends ArrayAdapter<SearchHistoryModel> {


    public SearchHistoryAdapter(Context context, List<SearchHistoryModel> searches) {
        super(context, android.R.layout.simple_list_item_1, searches);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SearchHistoryModel search = getItem(position);
        TextView tvSearchRow;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            tvSearchRow = (TextView)inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        else {
            tvSearchRow = (TextView)convertView;
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

        tvSearchRow.setText(search.query);
        return tvSearchRow;
    }

}
