package leskin.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.network.Urls;

/**
 * Created by Oleg Leskin on 28.08.2016.
 */
public class DetailMovieActivity extends AppCompatActivity {
    private final static String TAG_MOVIE = "movie";

    @BindView(R.id.text_header)
    TextView headerText;

    @BindView(R.id.text_year)
    TextView yearText;

    @BindView(R.id.text_vote_average)
    TextView voteAverageText;

    @BindView(R.id.text_description)
    TextView descriptionText;

    @BindView(R.id.img_poster)
    ImageView posterImg;

    private Movie movie;

    public static void launch(Context context, Movie movie) {
        Intent intent = new Intent(context, DetailMovieActivity.class);
        intent.putExtra(TAG_MOVIE, movie);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);
        getExtra();
        fillView();
    }

    private void getExtra() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable(TAG_MOVIE) != null) {
            this.movie = (Movie) getIntent().getExtras().getSerializable(TAG_MOVIE);
        }
    }

    private void fillView() {
        try {
            Glide.with(this).load(Urls.POSTER_URL + movie.getPosterPath()).into(posterImg);
            headerText.setText(movie.getTitle());
            yearText.setText(getParsedYear(movie.getReleaseDate()));
            setVoteAverageText();
            descriptionText.setText(movie.getOverview());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String getParsedYear(String date) {
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        try {
            Date sourceDate = sourceFormat.parse(date);
            return yearFormat.format(sourceDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @SuppressWarnings("deprecation")
    private void setVoteAverageText() {
        String voteAverage = String.format(getString(R.string.movie_details_average_rating), movie.getVoteAverage());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            voteAverageText.setText(Html.fromHtml(voteAverage, Html.FROM_HTML_MODE_LEGACY));
        } else {
            voteAverageText.setText(Html.fromHtml(voteAverage));
        }
    }
}
