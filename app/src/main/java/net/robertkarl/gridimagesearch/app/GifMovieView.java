package net.robertkarl.gridimagesearch.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.view.View;

import java.io.InputStream;

/**
 * Draw a gif onscreen
 * Courtesy of http://droid-blog.net/2011/10/14/tutorial-how-to-use-animated-gifs-in-android-part-1/
 */
public class GifMovieView extends View {

    private Movie mMovie;
    private long mMoviestart;

    /**
     *
     * @param stream open stream for a gif file from assets
     */
    public GifMovieView(Context context, InputStream stream) {
        super(context);
        mMovie = Movie.decodeStream(stream);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now = SystemClock.uptimeMillis();

        if (mMoviestart == 0) {
            mMoviestart = now;
        }

        final int relTime = (int)((now - mMoviestart) % mMovie.duration());
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        this.invalidate();
    }

}
