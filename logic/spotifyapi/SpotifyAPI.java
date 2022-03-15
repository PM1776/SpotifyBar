package logic.spotifyapi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import logic.device.Device;
import logic.playerlogic.PlayerLogic;
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
	
	/** The SpotifyCredentials instance that uses either the <i>Client Authorization</i>
	 * flow or <i>Authorization Code + PKCE</i> flow to get an access token.
	 * <p>An <i>Authorization Code + PKCE</i> flow requires a refreshed token every 6 
	 * minutes. 
	 * 
	 * @see SpotifyCredentials */
	private SpotifyCredentials sc;
	
	static {
		// adds the JSONPath expressions for initializing fields of Song objects from JSONs
		Map<String, String> jsonPathsMap = new HashMap<>();
		jsonPathsMap.put("name", ".name");
		jsonPathsMap.put("artists", ".artists[*]");
		jsonPathsMap.put("albumName", ".album.name");
		jsonPathsMap.put("albumURL", ".album.images[0].url");
		jsonPathsMap.put("previewURL", ".preview_url");
		jsonPathsMap.put("ID", ".id");
		jsonPathsMap.put("duration", ".duration_ms");
		jsonPathsMap.put("progress", "$.progress_ms");
		jsonPathsMap.put("timestamp", "$.timestamp");
		jsonPathsMap.put("playing", "$.is_playing");
		jsonPathsMap.put("contextURI", "$.context.uri");
		jsonPathsMap.put("albumURI", ".album.uri");
		jsonPathsMap.put("trackNumber", ".track_number");
		Song.putJSONFieldPaths(jsonPathsMap);
		
		// adds default fields to set when calling Song.initializeFromJSON()
		String[] defaultJsonFields = {"name", "artists", "albumURL", "previewURL", 
				"albumURI", "trackNumber"};
		Song.setDefaultJSONFields(defaultJsonFields);
	}
	/**
	 * Gets an access token for Spotify Web API requests by defaulting to the Client 
	 * Authorization Flow (doesn't access a spotify account and searches for songs
	 * to play their previews, but that is its end in capability in this class)
	 */
	public SpotifyAPI() {
		setSpotifyCredentials(new SpotifyCredentials());
	}
	
	/**
	 * Gets an access token for Spotify Web API requests by using the Authorization 
	 * Code + PKCE flow (connects to Spotify user accounts) to make requests. The 
	 * Authorization Code flow first asks the Spotify user to grant permissions and
	 * doesn't finish the flow, so it takes a parameter of Runnable interface 
	 * <b>init</b> to call its run() method when the flow completes.
	 * 
	 * @param init A Runnable interface to call when the Authorization Code flow 
	 * completes, as a Spotify user must first approve of access to their account.
	 */
	public SpotifyAPI(Runnable init) {
		setSpotifyCredentials(new SpotifyCredentials(init));
	}

	private void setSpotifyCredentials(SpotifyCredentials sc) {
		this.sc = sc;
	}
	
	/** 
	 * Returns the current access token, and throws a RuntimeException if none has been 
	 * authorized. This token will need to be refreshed every 6 minutes if using the 
	 * Authorization Code + PKCE flow for access. 
	 * 
	 * @return The access token granted through authorization.
	 * */
	public String getAccessToken () {
		return sc.getAccessToken();
	}
	
	/** 
	 * <i>Only applicable to the Authorization Code + PKCE flow</i>. Requests and returns
	 * a refreshed access token, as access tokens will expire in 6 minutes.
	 * 
	 * @return A refreshed access token.
	 */
	public String requestRefreshToken() {
		return sc.getRefreshToken();
	}
	
	/** 
	 * Returns a Device object that has select properties of this device
	 * concerning Spotify as its fields, with properties being the name, id, and 
	 * if active currently.
	 * 
	 * @apiNote URL for the Spotify end point documentation:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * get-a-users-available-devices
	 * @return A Device object with fields for the name, id, and active properties of 
	 * this Spotify device.
	 */
	public Device getDevice () {
		
		Device thisDevice = null;
		
	    final String url = "https://api.spotify.com/v1/me/player/devices";
	    
	    // GET request as a JSONObject
	    kong.unirest.json.JSONObject returnJson = getSpotifyAPIRequest(url).getObject();
	    
		try {
			// gets name of computer, which is the Spotify device name property
			String hostname = "Unknown";
		    InetAddress addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
			
		    if (!hostname.equals("Unknown")) {
		    	
		    	kong.unirest.json.JSONArray devices;
		    	devices = (kong.unirest.json.JSONArray) returnJson.getJSONArray("devices");
			    
		    	// loops through Spotify devices in JSONArray in returned JSONObject
		    	for (int i = 0; i < devices.length(); i++) {
		    		
			    	kong.unirest.json.JSONObject deviceObj = (kong.unirest.json.JSONObject) 
			    			devices.get(i);
					
					String deviceName = deviceObj.getString("name");
					
					if (deviceName.equals(hostname)) {
						String id = deviceObj.getString("id");
						boolean active = deviceObj.getBoolean("is_active");
						
						thisDevice = new Device(id, active, deviceName);
					}
				}
		    }
			
		} catch (UnknownHostException ex) {
		    System.out.println("Hostname can not be resolved");
		}

		return thisDevice;
	}
	
	/** 
	 * Transfers the playback, active or not, to the specified Device <b>d</b>, with
	 * the additional parameter <b>play</b> that indicates whether to begin playback
	 * or not.
	 * 
	 * @param play Can begin the playback transfered if <i>true</i>.
	 * @param d The <i>Device</i> object to transfer the playback to.
	 * @apiNote URL for the Spotify end point documentation:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/
	 * transfer-a-users-playback
	 * @return A String of the JSON response.
	 */
	public String transferPlayback (boolean play, Device d) {
		
		String url = "https://api.spotify.com/v1/me/player";
		
		// create JSONObject to send as the body of the PUT request
		JSONArray devices = new JSONArray();
		devices.add(d.getId());
		
		boolean p = play;
		
		JSONObject jObj = new JSONObject();
		jObj.put("device_ids", devices);
		jObj.put("play", p);
		
		// PUT request
		String returnJson = putSpotifyAPIRequest(url, jObj).toPrettyString();
		
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
	 * @apiNote URL for the Spotify end point documentation:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/search
	 * @return A <i>Song</i> array of size <b>limit</b> returned from the search results.
	 */
	public Song[] searchForSongs(String search, int limit) {
		
		// Initialize Song array to have an array object to return if error
		Song[] songs = new Song[limit];
		
		if (search == null) {
			throw new NullPointerException("The search query cannot be null.");
		} else if (limit < 1 || limit > 50) {
			throw new IllegalArgumentException("Parameter 'limit' must be between 1 and 50");
		}
		
	    final String endPoint = "https://api.spotify.com/v1/search";
	    
        final String q = search.replaceAll(" ", "+"); // (UTF-8 format of search)
        final String type = "track";
        final String lim = Integer.toString(limit);
        
	    StringBuilder query = new StringBuilder()
	    	.append("?q=").append(q)
	    	.append("&type=").append(type)
	    	.append("&limit=").append(lim);
	    
		// GET request
	   	String returnJson = getSpotifyAPIRequest(endPoint + query).toPrettyString();
		
		songs = Song.initializeFromJSON(returnJson, "$.tracks.items");
		
		// initializeFromJSON() puts a null after the last song added
		if (songs[0] == null) {
			JOptionPane.showMessageDialog(null, "No songs found", "Nothin'", 0);
		}

		return songs;
	}
	
	/** Returns the current Spotify player state, regardless of device, in a <i>Song</i> 
	 * object. Therefore, it contains information about the current track
	 * and if playing. The JSON returns much more information about the actions
	 * the player is taking and on the active device, however.
	 * 
	 * @apiNote URL for the Spotify end point documentation:
	 * https://developer.spotify.com/documentation/web-api/reference/#/operations/get-
	 * information-about-the-users-current-playback
	 * @return A <i>Song</i> object with the current Spotify player's data.
	 */
	public Song getPlaybackState() {
		
		String url = "https://api.spotify.com/v1/me/player";
		String returnJson = getSpotifyAPIRequest(url).toPrettyString();
		
		Song song = null;
			
		String[] extendedFields = {"duration", "progress", "timestamp", 
				"playing", "ID"};
		song = Song.initializeFromJSON(returnJson, "$.item", extendedFields)[0];

		return song;
	}
	
	/** 
	 * Uses the "player/currently-playing" endPoint to get a JSON containing the
	 * currently playing song's information, using the base <i>JSONPath</i> expression,
	 * "$.item".
	 * 
	 * @return A <i>Song</i> object containing the currently playing song's information.
	 */
	public Song getCurrentSong () {
		
		String url = "https://api.spotify.com/v1/me/player/currently-playing";
		String returnJson = getSpotifyAPIRequest(url).toPrettyString();
		
		Song song = Song.initializeFromJSON(returnJson, "$.item")[0];
		
		return song;
	}
	
	/** 
	 * Uses the "player/recently-playing" endPoint to get a JSON containing an
	 * array of <i>Song</i> objects between 1 and 50, and uses the base <i>JSONPath</i> 
	 * expression, "$.items".
	 * 
	 * @param limit An int of the number of <i>Song</i> objects to return between 1 and 50.
	 * @return An array of <i>Song</i> objects containing the Spotify Player's 
	 * recently played song information, in order of most recently played to
	 * less recently played.
	 */
	public Song[] getRecentlyPlayed (final int limit) {
		
		// Initialize Song array to have an array object to return if error
		Song[] songs = new Song[limit];
		
		if (limit < 1 || limit > 50) {
			throw new IllegalArgumentException("Parameter 'limit' must be between 1 and 50");
		}
		
		final String endPoint = "https://api.spotify.com/v1/me/player/recently-played";
		final String query = "?limit=" + limit;
		
		String returnJson = getSpotifyAPIRequest(endPoint + query).toPrettyString();
		
		Map<String, String> initialJsonPaths = Song.getJsonPathsMap();
		
		// reconfigures jsonPathsMap to the JSON context for initializing the songs[]
		Map<String, String> jsonPaths = new HashMap<>();
		jsonPaths.put("name", ".track.name");
		jsonPaths.put("artists", ".track.artists[*]");
		jsonPaths.put("albumURL", ".track.album.images[0].url");
		jsonPaths.put("previewURL", ".track.preview_url");
		jsonPaths.put("contextURI", ".context.uri");
		jsonPaths.put("duration", ".track.duration_ms");
		Song.putJSONFieldPaths(jsonPaths);
		
		// initializes Song object(s) from the specified field paths in the returned JSON
		String[] extendedFields = {"duration"};
		songs = Song.initializeFromJSON(returnJson, "$.items", extendedFields);
		
		Song.putJSONFieldPaths(initialJsonPaths);

		return songs;
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
		
		if (url == null) {
			throw new NullPointerException("URL String is null.");
	 	}
		
		JsonNode returnObj = null;
		
		if (sc.getAccessToken() != null) {
			
		    // GET request to Spotify
			try {
				returnObj = Unirest.get(url)
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + sc.getAccessToken())
						.asJson()
						.getBody();
				
			} catch (UnirestException e) {
				System.out.println(returnObj);
				e.printStackTrace();
			}
			
		} else {
			JOptionPane.showMessageDialog(null, "Cannot access Spotify Servers", "Problem with Connection", 0);
		}

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
		
		if (url == null) {
			throw new NullPointerException("URL String is null.");
	 	}
		
		// jObj null is okay
	
		JsonNode returnObj = null;
		
		if (sc.getAccessToken() != null) {
			
		    // PUT request to Spotify API
			try {
				returnObj = Unirest.put(url)
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + sc.getAccessToken())
						.body(jObj)
						.asJson()
						.getBody();
				
			} catch (UnirestException e) {
				e.printStackTrace();
			}
			
		} else {
			JOptionPane.showMessageDialog(null, "Cannot access Spotify Servers", "Problem with Connection", 0);
		}

		return returnObj;
	}
	
	/** 
	 * Loads the albumCover of a song object from its albumURL if it hasn't already
	 * been loaded. 
	 * 
	 * @param song The <i>Song</i> object to load the album cover of. 
	 */
	public static void loadAlbumCover(Song song) {
		
		// Continues if album cover not already loaded
		if ( !(song == null || 
				song.getAlbumURL() == null || 
				song.getAlbumCover() != null) ) {
			
			/*
			 *  (1) Gets stream of the album cover from the albumURL
			 *  and stores it in a BufferedImage
			 */
			
			Unirest.get(song.getAlbumURL())
				.thenConsume(r -> {
                try { 
                    BufferedImage albumCover = ImageIO.read(r.getContent());
			
					// and sets it as the song albumCover
					song.setAlbumCover(albumCover);
                } catch (IOException e) { 
                    e.printStackTrace();
                } 
            });
		}
	}
	
	/** 
	 * Sends a PUT request to play the parameter <b>song</b> from the <i>Device</i> 
	 * object <b>d</b> parameter, and if no device is specified, plays from the 
	 * active device.
	 * 
	 * @param song A <i>Song</i> object of the song to play.
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The PUT JSON response as a String.
	 */
	public String play(Song song, Device d) {
			
	    final String endPoint = "https://api.spotify.com/v1/me/player/play";
	    
	    // no query means use the active device
	    final String query = (d != null) ? "?device=" + d.getId() : "";
	    
	    // JSONObject to send with put
	    JSONObject jObj = new JSONObject();
	    jObj.put("context_uri", song.getAlbumURI());
	    JSONObject offset = new JSONObject();
	    offset.put("position", song.getTrackNumber() - 1);
	    jObj.put("offset", offset);
	    jObj.put("position_ms", song.getProgress());
  		
	    String response = putSpotifyAPIRequest(endPoint + query, jObj).toPrettyString();
		
		return response;
	}
	
	/** 
	 * Sends a PUT request to pause from the <i>Device</i> object <b>d</b> parameter, 
	 * and if no device is specified, pauses from the active device.
	 * 
	 * @param d The <i>Device</i> object of the device to play from (null = active device).
	 * @return The PUT JSON response as a String.
	 */
	public String pauseFrom(Device d) {
			
	    final String endPoint = "https://api.spotify.com/v1/me/player/pause";
	    // No device query means the active device
	    final String query = (d != null) ? "?device=" + d.getId() : "";
  		
	    String response = putSpotifyAPIRequest(endPoint + query, null).toPrettyString();
		
		return response;
	}
	
}
