package leskin.udacity.popularmovies.db;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
@Database(version = FavoriteMoviesDatabase.VERSION)
public class FavoriteMoviesDatabase {

    public static final int VERSION = 1;

    @Table(FavoriteMovies.class) public static final String FAVORITE_MOVIES = "favorite_movies";

}
