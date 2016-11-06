package leskin.udacity.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
public class PopularMoviesApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
