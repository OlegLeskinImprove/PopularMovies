package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.network.APIService;
import leskin.udacity.popularmovies.network.SortType;
import leskin.udacity.popularmovies.network.Urls;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MoviesActivity extends AppCompatActivity {
    private static final String TAG = "MoviesActivity";

    @BindView(R.id.list_movies)
    RecyclerView moviesRecyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private MovieAdapter adapter;
    private List<Movie> listMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onResume() {
        getMovies(SortType.POPULAR);
        super.onResume();
    }

    private void init() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setHasFixedSize(true);
        moviesRecyclerView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(this, listMovies);
        moviesRecyclerView.setAdapter(adapter);
    }

    private void getMovies(SortType sortType) {
        try {
            showProgress();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Urls.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            APIService service = retrofit.create(APIService.class);
            Map<String, String> queryParams = getQueryParams();
            Log.d(TAG, "getMovies: " + queryParams.toString());
            Call<ResponseBody> call = sortType == SortType.POPULAR ? service.getPopularMovies(queryParams) : service.getTopRatedMovies(queryParams);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    listMovies.clear();
                    listMovies.addAll(parseResponse(response.body()));
                    fillView();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.toString(), t);
                    hideProgress();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getMovies: " + e.toString());
        }
    }

    private Map<String, String> getQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("api_key", Config.MOVIE_DB_API_KEY);
        queryParams.put("page", "1");
        return queryParams;
    }

    private List<Movie> parseResponse(ResponseBody response) {
        List<Movie> list = new ArrayList<>();
        try {
            String jsonStr = response.string();
            Type listType = new TypeToken<List<Movie>>() {
            }.getType();
            JSONObject json = new JSONObject(jsonStr);
            list = new Gson().fromJson(json.get("results").toString(), listType);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void fillView() {
        hideProgress();
        adapter.notifyDataSetChanged();
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }
}
