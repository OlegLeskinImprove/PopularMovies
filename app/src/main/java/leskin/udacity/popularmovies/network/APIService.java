package leskin.udacity.popularmovies.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Oleg Leskin on 23.08.2016.
 */
public interface APIService {

    @GET("3/movie/top_rated")
    Call<ResponseBody> getTopRatedMovies(@QueryMap Map<String, String> options);

    @GET("3/movie/popular")
    Call<ResponseBody> getPopularMovies(@QueryMap Map<String, String> options);
}
