package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import leskin.udacity.popularmovies.adapter.MovieAdapter;
import leskin.udacity.popularmovies.model.Movie;
import leskin.udacity.popularmovies.network.APIService;
import leskin.udacity.popularmovies.network.Urls;
import leskin.udacity.popularmovies.utils.EndlessScrollListener;
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
    private GridLayoutManager layoutManager;
    private ArrayList<Movie> listMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listMovies.clear();
        getMovies(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            SettingsActivity.launch(this);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }


    private void init() {
        layoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setItemViewCacheSize(30);
        adapter = new MovieAdapter(this, listMovies);
        moviesRecyclerView.setAdapter(adapter);
        moviesRecyclerView.setHasFixedSize(true);
        moviesRecyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getMovies(page);
            }
        });
    }

    private void getMovies(final int page) {
        try {
            showProgress();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Urls.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            APIService service = retrofit.create(APIService.class);
            Map<String, String> queryParams = getQueryParams(page);
            Log.d(TAG, "getMovies: " + queryParams.toString());

            Call<ResponseBody> call = service.getMovies(queryParams);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    listMovies.addAll(parseResponse(response.body()));
                    fillView(page == 1);
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

    private Map<String, String> getQueryParams(int page) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("api_key", Config.MOVIE_DB_API_KEY);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("sort_by", getOrderTypeFromPref());
        return queryParams;
    }

    private ArrayList<Movie> parseResponse(ResponseBody response) {
        ArrayList<Movie> list = new ArrayList<>();
        try {
            String jsonStr = response.string();
            Type listType = new TypeToken<List<Movie>>() {
            }.getType();
            JSONObject json = new JSONObject(jsonStr);
            list = new Gson().fromJson(json.get("results").toString(), listType);
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void fillView(boolean reload) {
        hideProgress();
        if (reload)
            adapter.notifyDataSetChanged();
        else
            adapter.notifyItemInserted(adapter.getItemCount());
    }

    private String getOrderTypeFromPref() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_order_key),
                getString(R.string.pref_order_default_value));
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }
}
