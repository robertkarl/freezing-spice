package net.robertkarl.gridimagesearch.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.loopj.android.image.SmartImageTask;
import com.loopj.android.image.SmartImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Display a returned search result in a fullscreen view.
 */
public class FullScreenImageActivity extends Activity {

    ImageResult imageResult;
    private SmartImageView mFullScreenImage;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageResult = (ImageResult)getIntent().getSerializableExtra(SearchActivity.FULLSCREEN_IMAGE_KEY);
        mFullScreenImage = (SmartImageView)findViewById(R.id.ivFullScreen);

        mFullScreenImage.setImageUrl(imageResult.getFullURL(), new SmartImageTask.OnCompleteListener() {
            @Override
            public void onComplete() {
                setupShareIntent();
            }
        });

    }

    void setupShareIntent() {
        ImageView ivImage = (ImageView) findViewById(R.id.ivFullScreen);
        Uri bmpUri = getLocalBitmapUri(ivImage);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
        mShareActionProvider.setShareIntent(shareIntent);
    }

    /**
     * @return the URI path to the Bitmap displayed in specified ImageView
     */
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_details, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider)item.getActionProvider();
        return true;
    }
}
