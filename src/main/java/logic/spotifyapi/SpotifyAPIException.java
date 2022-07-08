package logic.spotifyapi;

/** 
 * Exception wrapper for exceptions thrown in the SpotifyAPI class.
 * 
 * @author Paul
 */
public class SpotifyAPIException extends RuntimeException {
	
	public SpotifyAPIException (String message) {
		super(message);
	}
	
	public SpotifyAPIException (Throwable ex) {
		super(ex);
	}
	
	public SpotifyAPIException (String message, Throwable ex) {
		super(message, ex);
	}
}
