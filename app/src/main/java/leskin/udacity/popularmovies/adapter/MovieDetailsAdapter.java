package leskin.udacity.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import leskin.udacity.popularmovies.R;
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.model.Review;
import leskin.udacity.popularmovies.model.Trailer;
import leskin.udacity.popularmovies.network.Urls;

/**
 * Created by Oleg Leskin on 30.10.2016.
 */
public class MovieDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_DETAILS = 0;
    private final int TYPE_TRAILER = 1;
    private final int TYPE_REVIEW = 2;

    private Movie movie;
    private List<Trailer> trailersList;
    private List<Review> reviewsList;
    private LayoutInflater layoutInflater;

    private Context context;

    public MovieDetailsAdapter(Context context, Movie movie, List<Trailer> trailersList, List<Review> reviewsList) {
        this.movie = movie;
        this.trailersList = trailersList;
        this.reviewsList = reviewsList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DETAILS:
                return new MovieDetailsViewHolder(layoutInflater.inflate(R.layout.list_item_movie_details, parent, false));

            case TYPE_TRAILER:
                return new MovieTrailerViewHolder(layoutInflater.inflate(R.layout.list_item_trailer, parent, false));

            case TYPE_REVIEW:
                return new MovieReviewViewHolder(layoutInflater.inflate(R.layout.list_item_review, parent, false));

        }

        throw new RuntimeException("There is no type that matches the type " + viewType +
                " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieDetailsViewHolder) {
            fillMovieDetails((MovieDetailsViewHolder) holder);
        } else if (holder instanceof MovieTrailerViewHolder) {
            fillTrailer((MovieTrailerViewHolder) holder, getTrailerPosition(position));
        } else if (holder instanceof MovieReviewViewHolder) {
            fillReview((MovieReviewViewHolder) holder, getReviewPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        return trailersList.size() + reviewsList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_DETAILS;

        if (!trailersList.isEmpty() && position <= trailersList.size())
            return TYPE_TRAILER;

        return TYPE_REVIEW;
    }

    private void fillMovieDetails(MovieDetailsViewHolder holder) {
        Glide.with(context).load(Urls.POSTER_URL + movie.getPosterPath()).into(holder.posterImg);
        holder.headerText.setText(movie.getTitle());
        holder.yearText.setText(getParsedYear(movie.getReleaseDate()));
        setVoteAverageText(holder);
        holder.descriptionText.setText(movie.getOverview());
    }

    @SuppressWarnings("deprecation")
    private void setVoteAverageText(MovieDetailsViewHolder holder) {
        String voteAverage = String.format(context.getString(R.string.movie_details_average_rating), movie.getVoteAverage());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.voteAverageText.setText(Html.fromHtml(voteAverage, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.voteAverageText.setText(Html.fromHtml(voteAverage));
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

    private void fillTrailer(MovieTrailerViewHolder holder, final int position) {
        if (position == 0) {
            holder.separator.setVisibility(View.VISIBLE);
            holder.trailersLblText.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.GONE);
            holder.trailersLblText.setVisibility(View.GONE);
        }

        holder.trailerText.setText(trailersList.get(position).getName());
        holder.trailerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailersList.get(position).getKey())));
            }
        });
    }

    private int getTrailerPosition(int position) {
        return position - 1;
    }

    private void fillReview(MovieReviewViewHolder holder, int position) {
        if (position == 0) {
            holder.separator.setVisibility(View.VISIBLE);
            holder.reviewsLblText.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.GONE);
            holder.reviewsLblText.setVisibility(View.GONE);
        }

        holder.authorText.setText(reviewsList.get(position).getAuthor());
        holder.reviewText.setText(reviewsList.get(position).getContent());
    }

    private int getReviewPosition(int position) {
        return position - trailersList.size() - 1;
    }


    class MovieDetailsViewHolder extends RecyclerView.ViewHolder {
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

        public MovieDetailsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MovieTrailerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view_separator)
        View separator;

        @BindView(R.id.text_trailers_lbl)
        TextView trailersLblText;

        @BindView(R.id.text_trailer)
        TextView trailerText;

        public MovieTrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MovieReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view_separator)
        View separator;

        @BindView(R.id.text_reviews_lbl)
        TextView reviewsLblText;

        @BindView(R.id.text_author)
        TextView authorText;

        @BindView(R.id.text_review)
        TextView reviewText;

        public MovieReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
