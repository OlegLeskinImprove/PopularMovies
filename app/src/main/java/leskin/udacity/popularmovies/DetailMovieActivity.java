package leskin.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import leskin.udacity.popularmovies.model.Movie;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
public class DetailMovieActivity extends AppCompatActivity {
    private final static String EXTRA_MOVIE = "movie";

    public static void launch(Context context, Movie movie) {
        Intent intent = new Intent(context, DetailMovieActivity.class);
        intent.putExtra(EXTRA_MOVIE, movie);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        if (savedInstanceState == null) {
            if (getIntent().hasExtra(EXTRA_MOVIE)) {
                DetailMovieFragment detailMovieFragment = DetailMovieFragment.newInstance((Movie) getIntent().getParcelableExtra(EXTRA_MOVIE));
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, detailMovieFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
