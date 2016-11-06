package leskin.udacity.popularmovies.db;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
@ContentProvider(authority = FavoriteMoviesProvider.AUTHORITY, database = FavoriteMoviesDatabase.class)
public class FavoriteMoviesProvider {

    public static final String AUTHORITY = "leskin.udacity.popularmovies.db.NotesProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String MOVIES = "favorite_movies";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = FavoriteMoviesDatabase.FAVORITE_MOVIES)
    public static class FavoriteMovies {

        @ContentUri(
                path = "favorite_movies",
                type = "vnd.android.cursor.dir/favorite",
                defaultSort = leskin.udacity.popularmovies.db.FavoriteMovies.TITLE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.MOVIES);

    }

}
