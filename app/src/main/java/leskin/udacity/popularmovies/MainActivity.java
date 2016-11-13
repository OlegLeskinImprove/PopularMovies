package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import leskin.udacity.popularmovies.event.MovieClickEvent;
import leskin.udacity.popularmovies.event.MoviesWasLoadedEvent;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
public class MainActivity extends AppCompatActivity{
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            ((MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movies)).setCountColumnsMovies(3);
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void movieItemClick(MovieClickEvent event){
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, DetailMovieFragment.newInstance(event.getMovie()), DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            DetailMovieActivity.launch(this, event.getMovie());
        }
    }

    @Subscribe
    public void moviesWasLoaded(MoviesWasLoadedEvent event){
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, DetailMovieFragment.newInstance(event.getFirstMovie()), DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

}
