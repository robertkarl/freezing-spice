package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.os.Bundle;

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

}
