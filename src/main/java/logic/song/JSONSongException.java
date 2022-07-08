package logic.song;

public class JSONSongException extends RuntimeException {
	
	public JSONSongException (Throwable e) throws RuntimeException {
		super(e);
	}
	
	public JSONSongException (String msg) throws RuntimeException {
		super(msg);
	}
	
	public JSONSongException (String msg, Throwable e) throws RuntimeException {
		super(msg, e);
	}
	
	public JSONSongException (String msg, String json) throws RuntimeException {
		super(msg + "\n" + json + "\n");
	}
	
	public JSONSongException (String msg, String json, Throwable e) throws RuntimeException {
		super(msg + "\n" + json + "\n", e);
	}
	
}
