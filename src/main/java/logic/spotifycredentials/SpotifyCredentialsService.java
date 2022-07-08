package logic.spotifycredentials;

import renderer.approvalbrowser.RedirectURIListener;

public interface SpotifyCredentialsService {
	public void addRedirectURIListener(RedirectURIListener ruril);
	public String[] getQueryValues();
	public void close();
}
