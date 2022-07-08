package logic.spotifyapi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.jayway.jsonpath.JsonPath;

import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import logic.device.Device;
import logic.playerlogic.PlayerLogic;
import logic.song.JSONSongException;
import logic.song.Song;
import logic.spotifycredentials.SpotifyCredentials;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/** 
 * A class that uses a SpotifyCredentialsService to be granted an access token to Spotify
 * servers by using either the Client Authorization flow (doesn't access a Spotify account 
 * but can still search for a song) or the Authorization Code + PKCE flow (connects to a 
 * Spotify user account and can make all requests) in its constructor call. This access
 * token is used to make all requests, such as start/resume the Spotify player, pause, 
 * transfer playback to a certain device, or search for a song. 
 * 
 * <p>The request responses are JSON's, which are further parsed using <i>JsonPath</i>
 * for selected data and returned as <i>Song</i> or <i>Device</i> objects with the data
 * as fields.
 * 
 * @author Paul Meddaugh
 * @since 3-13-22
 */
public class SpotifyAPI {
	
	/** The SpotifyCredentials instance can use either the <i>Client Authorization</i>
	 * flow or <i>Authorization Code + PKCE</i> flow to get an access token.
	 * <p>An <i>Authorization Code + PKCE</i> flow requires a refreshed token every 6 
	 * minutes. 
	 * 
	 * @see SpotifyCredentials */
	private SpotifyCredentials sc = new SpotifyCredentials();
	
	/** 
	 * Empty constructor that doesn't get authorization to the Spotify Web API.
	 */
	public SpotifyAPI() {}
	
	/**
	 * Gets an access token for Spotify Web API requests by using either the Client 
	 * Authorization flow (doesn't access a Spotify account but can play previews) 
	 * or the Authorization Code + PKCE flow (connects to Spotify user accounts).
	 * 
	 * @param authCodeFlow If <i>true</i>, follows the Authorization Code + PKCE
	 * flow. If <i>false</i>, follows Client Authorization flow.
	 */
	public SpotifyAPI(boolean authCodeFlow) {
		setSpotifyCredentials(authCodeFlow, null);
	}
	
	/**
	 * Gets an access token for Spotify Web API requests by using the Authorization 
	 * Code + PKCE flow (connects to Spotify user accounts) to make requests. The 
	 * Authorization Code flow first asks the Spotify user to grant permissions and
	 * doesn't finish the flow, so it takes a parameter of Runnable interface 
	 * <b>init</b> to call its run() method when the flow completes.
	 * 
	 * @param init A Runnable interface to call after the user grants or denies
	 * permissions.
	 */
	public SpotifyAPI(Runnable init) {
		setSpotifyCredentials(true, init);
	}
	
	/**
	 * Gets an access token for Spotify Web API requests by using either the Client 
	 * Authorization flow (doesn't access a Spotify account but can play previews)
	 * the Authorization Code + PKCE flow (connects to Spotify user accounts). The 
	 * Authorization Code flow first asks the Spotify user to grant permissions and
	 * doesn't finish the flow, so it takes a parameter of Runnable interface 
	 * <b>init</b> to call its run() method when the flow completes.
	 * 
	 * @param authCodeFlow If <i>true</i>, follows the Authorization Code + PKCE
	 * flow. If <i>false</i>, follows Client Authorization flow.
	 * @param init A Runnable interface to call after the user grants or denies
	 * permissions.
	 */
	public SpotifyAPI(boolean authCodeFlow, Runnable init) {
		setSpotifyCredentials(authCodeFlow, init);
	}

	private void setSpotifyCredentials(SpotifyCredentials sc) {
		this.sc = sc;
	}
	
	/**
	 * Gets authorization to the Spotify Web API by using either the Client 
	 * Authorization flow (doesn't access a Spotify account but can play previews)
	 * the Authorization Code + PKCE flow (connects to Spotify user accounts) for 
	 * making requests. The Authorization Code flow first asks the Spotify user to 
	 * grant permissions and doesn't finish the flow, so it takes a parameter of 
	 * Runnable interface <b>init</b> to call its run() method when the flow completes.
	 * 
	 * @param authCodeFlow If <i>true</i>, follows the Authorization Code + PKCE
	 * flow. If <i>false</i>, follows Client Authorization flow.
	 * @param init A Runnable interface to call after the user grants or denies
	 * permissions.
	 */
	public void setSpotifyCredentials(boolean authCodeFlow, Runnable init) {
		try {
			if (!authCodeFlow) {
				setSpotifyCredentials(new SpotifyCredentials(false));
			} else {
				setSpotifyCredentials(new SpotifyCredentials(init));
			}
		} catch (UnirestException ue) {
			throw new SpotifyAPIException("Could not get connection.", ue);
		}
	}
	
	/** 
	 * Returns the current access token, and throws a RuntimeException if none has been 
	 * authorized. This token will need to be refreshed every 6 minutes if using the 
	 * Authorization Code + PKCE flow. 
	 * 
	 * @return The access token that was granted through authorization.
	 * */
	public String getAccessToken () {
		return sc.getAccessToken();
	}
	
	public boolean authorizationSuccessful () {
		return sc.wasSuccessful();
	}
	
	private void checkAuthSuccess () {
		if (!authorizationSuccessful()) {
			throw new SpotifyAPIException("Authorization was unsuccessful.");
		}
	}
	
	/** 
	 * <i>Only applicable to the Authorization Code + PKCE flow</i>. Requests and returns
	 * a refreshed access token, as access tokens will expire in 6 minutes.
	 * 
	 * @return A refreshed access token.
	 */
	public String requestRefreshToken() {
		checkAuthSuccess();
		
		return sc.getRefreshToken();
	}
	
	/** 
	 * Returns a Map that has select values of this device on Spotify, with values 
	 * being the name, id, if active currently, and if private session.
	 * 
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * get-a-users-available-devices
	 * @return A Map object with keys of "name," "id," "active," and "privateSession"
	 * and values for "name" and "id" as Strings and "active" and "privateSession" 
	 * as booleans of this device on Spotify.
	 */
	public String getDevices () {
		
		String returnJson = "";
		
	    final String url = "https://api.spotify.com/v1/me/player/devices";
	    returnJson = getSpotifyAPIRequest(url).toString();

		return returnJson;
	}
	
	/** 
	 * Transfers the playback, active or not, to the specified Device <b>d</b>, with
	 * the additional parameter <b>play</b> that indicates whether to begin playback
	 * or not.
	 * 
	 * @param play Can begin the playback transfered if <i>true</i>.
	 * @param d The <i>Device</i> object to transfer the playback to.
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * transfer-a-users-playback
	 * @return A String of the JSON response.
	 */
	public String transferPlayback (boolean play, String deviceId) {
		
		String returnJson = "";
		
		if (deviceId == null) {
			throw new IllegalArgumentException("String \"deviceID\" must not be null");
		}
		
		String url = "https://api.spotify.com/v1/me/player";
		
		// The JSON to send with PUT request 
		JSONObject jObj = new JSONObject();
		// {
			JSONArray devices = new JSONArray();
			devices.add(deviceId);
			jObj.put("device_ids", devices); 
			
			jObj.put("play", play);
		// }
		
		returnJson = putSpotifyAPIRequest(url, jObj).toPrettyString();
		
		return returnJson;
	}
	
	/** 
	 * Gets a search request of the <b>search</b> parameter that responds with a JSON 
	 * containing a list of songs size <b>limit</b> between 1 and 50. This song list
	 * is put into an Array of <i>Song</i> objects, which is what returns from this 
	 * method.
	 * 
	 * @param search The keywords of the query.
	 * @param limit The number of songs to return.
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/search
	 * @return A <i>Song</i> array of size <b>limit</b> returned from the search results.
	 */
	public String searchForSongs(String search, int limit) {
		
		// Initialize to return not null if error
		String returnJson = "";
		
		if (search == null) {
			throw new NullPointerException("The search query cannot be null.");
		} else if (limit < 1 || limit > 50) {
			throw new IllegalArgumentException("Parameter 'limit' must be between 1 and 50");
		}
		
	    final String endPoint = "https://api.spotify.com/v1/search";
	    
        final String q = search.replaceAll(" ", "+"); // (UTF-8 format of 'search')
        final String type = "track";
        final String lim = Integer.toString(limit);
        
	    StringBuilder query = new StringBuilder()
	    	.append("?q=").append(q)
	    	.append("&type=").append(type)
	    	.append("&limit=").append(lim);
	    
		// GET request
	    returnJson = getSpotifyAPIRequest(endPoint + query).toPrettyString();

		return returnJson;
	}
	
	/** Returns the current Spotify player state, regardless of device, in a <i>Song</i> 
	 * object. Therefore, it contains information about the current track on Spotify
	 * and if playing. Its albumCover is not automatically set to load for performance, 
	 * as it requires another GET request.
	 * 
	 * <p>The JSON returns much more information about the actions the player is taking 
	 * and on the active device than is initialized to the song object shown in the 
	 * API note below.
	 * 
	 * @param loadAlbumCover if true, additionally loads the album cover of the
	 * song on Spotify's playback state.
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/get-
	 * information-about-the-users-current-playback
	 * @return A <i>Song</i> object with the current Spotify player's data.
	 */
	public String getPlaybackState(boolean loadAlbumCover) {
		
		String returnJson = "";
		
		String url = "https://api.spotify.com/v1/me/player";
		returnJson = getSpotifyAPIRequest(url).toPrettyString();

		return returnJson;
	}
	
	/** 
	 * Uses the "player/currently-playing" endPoint to get a JSON containing the
	 * currently playing song's information, using the base <i>JSONPath</i> expression,
	 * "$.item".
	 * 
	 * @return A <i>Song</i> object containing the currently playing song's information.
	 */
	public String getCurrentSong () {
		
		String returnJson = "";
		
		String url = "https://api.spotify.com/v1/me/player/currently-playing";
		returnJson = getSpotifyAPIRequest(url).toPrettyString();
		
		return returnJson;
	}
	
	/** 
	 * Uses the "player/recently-playing" endPoint to get a JSON containing an
	 * array of <i>Song</i> objects between 1 and 50, and uses the base <i>JSONPath</i> 
	 * expression, "$.items".
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * get-the-users-currently-playing-track
	 * @param limit An int of the number of <i>Song</i> objects to return between 1 and 50.
	 * @return An array of <i>Song</i> objects containing the Spotify Player's 
	 * recently played song information, in order of most recently played to
	 * less recently played.
	 */
	public String getRecentlyPlayed (final int limit) {
		
		// Initialize to return empty string if error instead of null
		String returnJson = "";
		
		if (limit < 1 || limit > 50) {
			throw new IllegalArgumentException("Parameter 'limit' must be between 1 and 50");
		}
		
		final String endPoint = "https://api.spotify.com/v1/me/player/recently-played";
		final String query = "?limit=" + limit;
		
		returnJson = getSpotifyAPIRequest(endPoint + query).toPrettyString();

		return returnJson;
	}
	
	/** 
	 * Sends a PUT request to play the parameter <b>song</b> from the <i>Device</i> 
	 * object <b>d</b> parameter, and if no device is specified (null), plays from the 
	 * active device.
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * start-a-users-playback
	 * @param song A <i>Song</i> object of the song to play.
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The PUT JSON response as a String.
	 */
	public String play(Song song, Device d) {
		
		String response = "";
		
		if (song == null) {
			throw new IllegalArgumentException("Song parameter cannot be null");
		}
			
	    final String endPoint = "https://api.spotify.com/v1/me/player/play";
	    final String query = (d == null) ? "" : "?device=" + d.getId();
	    
	    // JSON to send with PUT
	    JSONObject jObj = new JSONObject();
	    // {
		    jObj.put("context_uri", 
		    		(song.getContextURI() != null) ? song.getContextURI() : song.getAlbumURI());
		    
		    JSONObject offset = new JSONObject();
		    offset.put("position", song.getTrackNumber() - 1);
		    jObj.put("offset", offset);
		    
	    	jObj.put("position_ms", song.getProgress());
  		// }
	    
	    response = putSpotifyAPIRequest(endPoint + query, jObj).toPrettyString();
		
		return response;
	}
	
	/** 
	 * Sends a PUT request to pause from the <i>Device</i> object <b>d</b> parameter, 
	 * and if no device is specified (null), pauses from the active device.
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * pause-a-users-playback
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The PUT JSON response as a String.
	 */
	public String pauseFrom(Device d) {
		
		String response = "";
			
	    final String endPoint = "https://api.spotify.com/v1/me/player/pause";
	    final String query = (d == null) ? "" : "?device=" + d.getId();
	    
	    response = putSpotifyAPIRequest(endPoint + query, null).toPrettyString();
		
		return response;
	}
	
	/** 
	 * Sends a POST request to skip to the next track from the <i>Device</i> object
	 * parameter, and if no device is specified (null), pauses from the active device.
	 * 
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * skip-users-playback-to-next-track
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The POST JSON response as a String.
	 */
	public String nextFrom(Device d) {
		
		String response = "";
		
	    final String endPoint = "https://api.spotify.com/v1/me/player/next";
	    final String query = (d == null) ? "" : "?device=" + d.getId();
	    
	    response = postSpotifyAPIRequest(endPoint + query).toPrettyString();
		
		return response;
	}
	
	/** 
	 * Sends a POST request to skip to the next track from the <i>Device</i> object
	 * parameter, and if no device is specified (null), pauses from the active device.
	 * 
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * skip-users-playback-to-next-track
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The POST JSON response as a String.
	 */
	public String previousFrom(Device d) {
		
		String response = "";
		
	    final String endPoint = "https://api.spotify.com/v1/me/player/previous";
	    final String query = (d == null) ? "" : "?device=" + d.getId();
	    
	    response = postSpotifyAPIRequest(endPoint + query).toPrettyString();
		
		return response;
	}
	
	/** 
	 * Sends a POST request to add a song to the queue targeting the <i>Device</i> 
	 * object parameter, and if no device is specified (null), targets the active device.
	 * 
	 * @apiNote Spotify documentation for end point:
	 * https://developer.spotify.com/documentation/web-api/reference
	 * /#/operations/add-to-queue
	 * @param uri The Spotify URI of the song to add to the queue.
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The POST JSON response as a String.
	 */
	public String addToQueue(String uri, Device d) {
		
		String response = "";
		
	    final String endPoint = "https://api.spotify.com/v1/me/player/queue";
	    final String device = (d == null) ? "" : "&device=" + d.getId();
	    final String query = "?uri=" + uri + device;
	    
	    response = postSpotifyAPIRequest(endPoint + query).toPrettyString();
		
		return response;
	}
	
	/**
	 * 
	 * 
	 * @param url The endPoint and query (if applicable) to make the request.
	 * @param jObj The JSONObject to put as the body of the request (can be null).
	 * @return A <i>JsonNode</i> of the response.
	 */
	public JsonNode makeSpotifyAPIRequest (String type, String url, JSONObject jObj) {
		
		JsonNode returnObj = null;
		
		checkAuthSuccess();
		
		if (type == null) {
			throw new NullPointerException("String \"type\" cannot be null.");
		} else if (url == null) {
			throw new NullPointerException("String \"url\" is null.");
	 	}
		
	    // Requests to Spotify API
		try {
			if (type.equalsIgnoreCase("GET")) {
				returnObj = Unirest.get(url)
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + sc.getAccessToken())
						.asJson()
						.getBody();
			} else if (type.equalsIgnoreCase("PUT")) {
				returnObj = Unirest.put(url)
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + sc.getAccessToken())
						.body(jObj)
						.asJson()
						.getBody();
			} else if (type.equalsIgnoreCase("POST")) {
				returnObj = Unirest.post(url)
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + sc.getAccessToken())
						.asJson()
						.getBody();
			}
			
		} catch (UnirestException e) {
			throw new SpotifyAPIException("Could not connect to the Internet", e);
		}

		return returnObj;
	}
	
	/** 
	 * Makes a GET request to the Spotify Web API at the parameter <b>url</b> 
	 * with accessToken and content-type headers, and returns the response
	 * as a Unirest library object <i>JsonNode</i> which can be converted to a
	 * String, JSONObject, or down to the desired object.
	 * 
	 * @param url The endPoint and query (if applicable) to make the request.
	 * @return A <i>JsonNode</i> of the response.
	 */
	public JsonNode getSpotifyAPIRequest (String url) {
		
		JsonNode returnObj = makeSpotifyAPIRequest("GET", url, null);
		return returnObj;
		
	}
	
	/** 
	 * Makes a PUT request to the Spotify Web API at the parameter <b>url</b> 
	 * with accessToken and content-type headers and a body of a JSONObject.
	 * It returns the response as a Unirest library object <i>JsonNode</i> which can be 
	 * converted to a String, JSONObject, or down to the desired object.
	 * 
	 * @param url The endPoint and query (if applicable) to make the request.
	 * @param jObj The JSONObject to put as the body of the request (can be null).
	 * @return A <i>JsonNode</i> of the response.
	 */
	public JsonNode putSpotifyAPIRequest (String url, JSONObject jObj) {
		
		JsonNode returnObj = makeSpotifyAPIRequest("PUT", url, jObj);
		return returnObj;
		
	}
	
	/** 
	 * Makes a PUT request to the Spotify Web API at the parameter <b>url</b> 
	 * with accessToken and content-type headers and a body of a JSONObject.
	 * It returns the response as a Unirest library object <i>JsonNode</i> which can be 
	 * converted to a String, JSONObject, or down to the desired object.
	 * 
	 * @param url The endPoint and query (if applicable) to make the request.
	 * @return A <i>JsonNode</i> of the response.
	 */
	public JsonNode postSpotifyAPIRequest (String url) {
		
		JsonNode returnObj = makeSpotifyAPIRequest("POST", url, null);
		return returnObj;
		
	}
	
	/** 
	 * Loads the albumCover of a song object from its albumURL if it hasn't already
	 * been loaded. 
	 * 
	 * @param song The <i>Song</i> object to load the album cover of. 
	 */
	public BufferedImage loadAlbumCover(String url) {
		
		BufferedImage albumCover = null;

		if (url == null) {
			throw new IllegalArgumentException("\"url\" parameter must not be null");
		}
			
		// Gets image URL as byte array
		byte[] imageBytes = Unirest.get(url)
			.asBytes()
			.getBody();
		
		try {
			albumCover = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return albumCover;
	}
}
