package leskin.udacity.popularmovies.network;

/**
 * Created by Oleg Leskin on 23.08.2016.
 */
public enum SortType {
    POPULAR("popular"), TOP_RATED("top_rated");

    private final String sortType;

    SortType(String sortType) {
        this.sortType = sortType;
    }

    @Override
    public String toString() {
        return sortType;
    }
}
