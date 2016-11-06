package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import leskin.udacity.popularmovies.model.Movie;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
public class MainActivity extends AppCompatActivity implements MoviesFragment.MoviesCallback {
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
    public void movieItemClick(Movie movie) {
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, DetailMovieFragment.newInstance(movie), DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            DetailMovieActivity.launch(this, movie);
        }
    }

    @Override
    public void moviesWasLoaded(Movie firstMovie) {
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, DetailMovieFragment.newInstance(firstMovie), DETAILFRAGMENT_TAG)
                    .commit();
        }
    }
}
