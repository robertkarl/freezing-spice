package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.image.SmartImageView;


public class FullScreenImageActivity extends Activity {

    ImageResult imageResult;
    private SmartImageView ivFullScren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageResult = (ImageResult)getIntent().getSerializableExtra(SearchActivity.FULLSCREEN_IMAGE_KEY);
        ivFullScren = (SmartImageView)findViewById(R.id.ivFullScreen);

        ivFullScren.setImageUrl(imageResult.getFullURL());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.full_screen_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
