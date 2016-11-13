package leskin.udacity.popularmovies.event;

import leskin.udacity.popularmovies.model.Movie;

/**
 * Created by Oleg Leskin on 13.11.2016.
 */
public class MoviesWasLoadedEvent {
    private Movie firstMovie;

    public Movie getFirstMovie() {
        return firstMovie;
    }

    public void setFirstMovie(Movie firstMovie) {
        this.firstMovie = firstMovie;
    }
}
