package logic.song;

public class JSONSongException extends RuntimeException {
	
	public JSONSongException (String msg) throws RuntimeException {
		super(msg);
	}
	
	public JSONSongException (String msg, Exception e) throws RuntimeException {
		super(msg, e);
	}
	
	public JSONSongException (String msg, String json) throws RuntimeException {
		super(msg + "\n" + json);
	}
	
	public JSONSongException (String msg, String json, Exception e) throws RuntimeException {
		super(msg + "\n" + json, e);
	}
	
}
