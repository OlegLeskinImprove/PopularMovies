package leskin.udacity.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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
import leskin.udacity.popularmovies.db.FavoriteMovies;
import leskin.udacity.popularmovies.db.FavoriteMoviesProvider;
import leskin.udacity.popularmovies.event.MovieClickEvent;
import leskin.udacity.popularmovies.event.MoviesWasLoadedEvent;
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
    private int clickedPosition = 0;
    private String orderType = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.movies_list_key))) {
            listMovies = savedInstanceState.getParcelableArrayList(getString(R.string.movies_list_key));
            clickedPosition = savedInstanceState.getInt(getString(R.string.movie_position_key), 0);
            orderType = savedInstanceState.getString(getString(R.string.movie_order_type_key), "");
        }
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!listMovies.isEmpty())
            outState.putParcelableArrayList(getString(R.string.movies_list_key), listMovies);
        outState.putInt(getString(R.string.movie_position_key), clickedPosition);
        outState.putString(getString(R.string.movie_order_type_key), orderType);

        super.onSaveInstanceState(outState);
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
        EventBus.getDefault().register(this);

        if (!orderType.equals(getOrderTypeFromPref()) || listMovies.isEmpty()) {
            listMovies.clear();
            getMovies(1);
        } else {
            fillView(true);
            scrollToClickedPosition();
            moviesWasLoaded(listMovies.get(clickedPosition));
        }
    }

    @Override
    public void onStop() {
        orderType = getOrderTypeFromPref();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void movieItemClick(MovieClickEvent event) {
        clickedPosition = event.getPosition();
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
        adapter = new MovieAdapter(getActivity(), listMovies);
        moviesRecyclerView.setAdapter(adapter);
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
            if (loadFromNetwork()) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Urls.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                APIService service = retrofit.create(APIService.class);
                orderType = getOrderTypeFromPref();
                Map<String, String> queryParams = getQueryParams(page, orderType);

                Call<ResponseBody> call = service.getMovies(queryParams);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        listMovies.addAll(parseResponse(response.body()));
                        fillView(page == 1);
                        if (!listMovies.isEmpty() && page == 1)
                            moviesWasLoaded(listMovies.get(0));
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        hideProgress();
                    }
                });
            } else {
                loadFromFavorites();
            }

        } catch (Exception e) {
            Log.e(TAG, "getMovies: " + e.toString());
        }
    }

    private void moviesWasLoaded(Movie movie) {
        MoviesWasLoadedEvent event = new MoviesWasLoadedEvent();
        event.setFirstMovie(movie);
        EventBus.getDefault().post(event);
    }

    private boolean loadFromNetwork() {
        return !getOrderTypeFromPref().equals(getResources().getStringArray(R.array.pref_units_value)[2]);
    }

    private Map<String, String> getQueryParams(int page, String sortType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("api_key", BuildConfig.MOVIE_DB_API_KEY);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("sort_by", sortType);
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

    private void loadFromFavorites() {
        List<Movie> favoriteMovies = new ArrayList<>();
        Cursor cursor = getActivity().getContentResolver().query(FavoriteMoviesProvider.FavoriteMovies.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndex(FavoriteMovies.ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(FavoriteMovies.TITLE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(FavoriteMovies.OVERVIEW)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex(FavoriteMovies.POSTER_PATH)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(FavoriteMovies.RELEASE_DATE)));
                movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(FavoriteMovies.VOTE_AVERAGE)));
                favoriteMovies.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();

        listMovies.clear();
        listMovies.addAll(favoriteMovies);
        fillView(true);
    }

    private void fillView(boolean reload) {
        hideProgress();
        if (reload)
            adapter.notifyDataSetChanged();
        else
            adapter.notifyItemInserted(adapter.getItemCount());
    }

    private void scrollToClickedPosition() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(clickedPosition);
            }
        }, 500);
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

}
