package leskin.udacity.popularmovies.event;

import leskin.udacity.popularmovies.model.Movie;

/**
 * Created by Oleg Leskin on 13.11.2016.
 */
public class MovieClickEvent {
    private int position;
    private Movie movie;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
