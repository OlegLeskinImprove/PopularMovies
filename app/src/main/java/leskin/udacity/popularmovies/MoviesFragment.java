package leskin.udacity.popularmovies;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class MoviesFragment extends Fragment {
    private static final String TAG = "MoviesFragment";

    @BindView(R.id.list_movies)
    RecyclerView moviesRecyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private MovieAdapter adapter;
    private GridLayoutManager layoutManager;
    private ArrayList<Movie> listMovies = new ArrayList<>();
    private int countColumnsMovies = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        listMovies.clear();
        getMovies(1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            SettingsActivity.launch(getActivity());
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }


    private void init() {
        layoutManager = new GridLayoutManager(getActivity(), getCountColumnsMovies());
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setItemViewCacheSize(30);
        adapter = new MovieAdapter(getActivity(), listMovies, ((MoviesCallback) getActivity()));
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

            Call<ResponseBody> call = service.getMovies(queryParams);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    listMovies.addAll(parseResponse(response.body()));
                    fillView(page == 1);
                    if (!listMovies.isEmpty() && page == 1)
                        ((MoviesCallback) getActivity()).moviesWasLoaded(listMovies.get(0));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
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
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_order_key),
                getString(R.string.pref_order_default_value));
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    public int getCountColumnsMovies() {
        return countColumnsMovies;
    }

    public void setCountColumnsMovies(int countColumnsMovies) {
        this.countColumnsMovies = countColumnsMovies;
    }


    public interface MoviesCallback {
        void movieItemClick(Movie movie);

        void moviesWasLoaded(Movie firstMovie);
    }

}
