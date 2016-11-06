package leskin.udacity.popularmovies.db;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Oleg Leskin on 06.11.2016.
 */
public interface FavoriteMovies {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(DataType.Type.INTEGER)
    String ID = "id";

    @DataType(DataType.Type.TEXT)
    String POSTER_PATH = "poster_path";

    @DataType(DataType.Type.TEXT)
    String OVERVIEW = "overview";

    @DataType(DataType.Type.TEXT)
    String RELEASE_DATE = "release_date";

    @DataType(DataType.Type.TEXT)
    String TITLE = "title";

    @DataType(DataType.Type.REAL)
    String VOTE_AVERAGE = "vote_average";
}
