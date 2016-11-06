package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import leskin.udacity.popularmovies.adapter.MovieDetailsAdapter;
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.model.Review;
import leskin.udacity.popularmovies.model.Trailer;
import leskin.udacity.popularmovies.network.APIService;
import leskin.udacity.popularmovies.network.Urls;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Oleg Leskin on 28.08.2016.
 */
public class DetailMovieFragment extends Fragment {
    private static final String TAG = "DetailMovieFragment";
    private final static String EXTRA_MOVIE = "movie";

    @BindView(R.id.rv_movie_details)
    RecyclerView movieDetailsRv;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private Movie movie;

    private ArrayList<Trailer> listTrailers = new ArrayList<>();
    private ArrayList<Review> listReviews = new ArrayList<>();

    public static DetailMovieFragment newInstance(Movie movie) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MOVIE, movie);

        DetailMovieFragment detailMovieFragment = new DetailMovieFragment();
        detailMovieFragment.setArguments(bundle);
        return detailMovieFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getExtra();

        if (movie != null) {
            getMovieTrailers(movie.getId());
            getMovieReviews(movie.getId());
        }
    }

    private void getExtra() {
        if (getArguments() != null && getArguments().getParcelable(EXTRA_MOVIE) != null) {
            this.movie = getArguments().getParcelable(EXTRA_MOVIE);
        }
    }

    private void fillView() {
        try {
            MovieDetailsAdapter adapter = new MovieDetailsAdapter(getContext(), movie, listTrailers, listReviews);
            movieDetailsRv.setLayoutManager(new LinearLayoutManager(getContext()));
            movieDetailsRv.setAdapter(adapter);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void getMovieTrailers(Integer movieId) {
        showProgress();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);

        Map<String, String> queryParams = getQueryParams();
        Call<ResponseBody> call = service.getMovieTrailers(String.valueOf(movieId), queryParams);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideProgress();
                listTrailers.addAll(parseTrailers(response.body()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideProgress();
            }
        });
    }

    private void getMovieReviews(Integer movieId) {
        showProgress();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);

        Map<String, String> queryParams = getQueryParams();
        Call<ResponseBody> call = service.getMovieReviews(String.valueOf(movieId), queryParams);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listReviews.addAll(parseReviews(response.body()));
                fillView();
                hideProgress();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideProgress();
            }
        });
    }

    private Map<String, String> getQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("api_key", Config.MOVIE_DB_API_KEY);
        return queryParams;
    }

    private ArrayList<Trailer> parseTrailers(ResponseBody response) {
        ArrayList<Trailer> list = new ArrayList<>();
        try {
            String jsonStr = response.string();
            Type listType = new TypeToken<List<Trailer>>() {
            }.getType();
            JSONObject json = new JSONObject(jsonStr);
            list = new Gson().fromJson(json.get("results").toString(), listType);
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ArrayList<Review> parseReviews(ResponseBody response) {
        ArrayList<Review> list = new ArrayList<>();
        try {
            String jsonStr = response.string();
            Type listType = new TypeToken<List<Review>>() {
            }.getType();
            JSONObject json = new JSONObject(jsonStr);
            list = new Gson().fromJson(json.get("results").toString(), listType);
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }
}
