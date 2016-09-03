package leskin.udacity.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import leskin.udacity.popularmovies.DetailMovieActivity;
import leskin.udacity.popularmovies.R;
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.network.Urls;


/**
 * Created by Oleg Leskin on 23.08.2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private ArrayList<Movie> list;

    public MovieAdapter(Context context, ArrayList<Movie> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position) {
        Glide.with(context).load(Urls.POSTER_URL + list.get(position).getPosterPath()).into(holder.posterImg);
        holder.posterImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailMovieActivity.launch(context, getItem(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private Movie getItem(int position) {
        return list.get(position);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_poster)
        ImageView posterImg;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
