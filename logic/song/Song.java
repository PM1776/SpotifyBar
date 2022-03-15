package logic.song;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

/** A class which can instantiate objects to hold a song's information received 
 * from the Spotify API, including name, artists, album name, album Cover, album
 * cover URL, preview URL, Spotify ID, and the duration of the song. 
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 * */
public class Song {
	
	/** The name of the song. */
	private String name;
	/** The artists of the song in a TreeSet. */
	private LinkedHashSet<String> artists = new LinkedHashSet<>();
	
	/** The name of the album. */
	private String albumName;
	/** The album cover as a BufferedImage. */
	private BufferedImage albumCover;
	/** The URI to begin playing the album. */
	private String albumURI;
	/** The URL to retrieve the album cover. */
	private String albumURL;
	
	/** The track number of the playlist/album. */
	private int trackNumber;
	
	/** The URL to retrieve the song preview. */
	private String previewURL;
	
	/** The ID of the track on Spotify servers. */
	private String ID;
	
	/** The duration of the song in seconds as a double. */
	private int duration;
	
	/** The progress into the song in seconds as a double. */
	private int progress;
	
	/** The last time in Unix milliseconds that the song data was updated. */
	private long timestamp;
	
	/** The playback state of the track on Spotify. */
	private boolean playing;
	
	/** The context URI of the track on Spotify. */
	private String contextURI;
	
	/** A {@code Map<String, String>} that contains the <i>JSONPath</i> expression 
	 * values to get each key field in the <i>Song</i> class from a JSON returned by 
	 * the Spotify Web API.
	 * 
	 * @see JsonPath
	 * */
	private static Map<String, String> jsonPaths = new HashMap<>();
	
	/** The default fields that initializeFromJSON() will parse using their expression
	 * values in jsonPaths without using initializeFromJSON()'s extendedFields parameter. */
	private static String[] defaultJsonFields;
	
	/** Creates a Song object to store a song's data. */
	public Song() {}
	
	public Song(String name, LinkedHashSet<String> artists, String albumURL, 
			String previewURL) {
		this.name = name;
		this.artists = artists;
		this.albumURL = albumURL;
		this.previewURL = previewURL;
	}
	
	public Song(String name, LinkedHashSet<String> artists, String albumURL, 
			String previewURL, String albumName, String ID) {
		
		this(name, artists, albumURL, previewURL);
		
		this.albumName = albumName;
		this.ID = ID;
	}
	
	/** Takes all the fields of class <i>Song</i> as parameters. Primarily used for 
	 * deep copying a Song object.
	 * 
	 * @param name The name of the song.
	 * @param artists A TreeSet of the artists.
	 * @param imageURL The URL to get the album cover.
	 * @param previewURL The URL to get the preview of the song.
	 * @param albumName Name of album.
	 * @param ID The ID of the track on spotify servers.
	 * @param duration The length of the track.
	 * @param albumCover A BufferedImage of the album cover.
	 */
	public Song(String name, LinkedHashSet<String> artists, String albumName,
			String albumURL, String albumURI, BufferedImage albumCover, String previewURL, 
			String ID, int duration, int progress, long timeStamp, boolean playing, 
			String contextURI, int trackNumber) {
		
		this(name, artists, albumURL, previewURL, albumName, ID);
		
		this.albumURI = albumURI;
		this.albumCover = albumCover;
		this.duration = duration;
		this.progress = progress;
		this.timestamp = timeStamp;
		this.playing = playing;
		this.contextURI = contextURI;
		this.trackNumber = trackNumber;
	}
	
	// Name
	public String getName() {
		return name;
	}
	public void setName (String name) {
		this.name = name;
	}
	
	// Artists
	public LinkedHashSet<String> getArtists() {
		if (!artists.isEmpty()) {
			return new LinkedHashSet<String>(artists);
		}
		return null;
	}
	public void setArtists(LinkedHashSet<String> artists) {
		this.artists = artists;
	}
	
	// Album name
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	
	// Album URL
	public String getAlbumURL() {
		return albumURL;
	}
	public void setAlbumURL (String url) {
		this.albumURL = url;
	}
	
	// Album cover
	public BufferedImage getAlbumCover() {
		if (albumCover != null) {
			BufferedImage bufImg = new BufferedImage(albumCover.getWidth(), albumCover.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			
			Graphics g = bufImg.getGraphics();
			g.drawImage(albumCover, 0, 0, null);
			g.dispose();
			
			return bufImg;
		} else {
			return null;
		}
	}
	public void setAlbumCover(BufferedImage bi) {
		this.albumCover = bi;
	}

	// Album URI
	public String getAlbumURI() {
		return albumURI;
	}
	public void setAlbumURI(String albumURI) {
		this.albumURI = albumURI;
	}
	
	// Preview URL
	public String getPreviewURL() {
		return previewURL;
	}
	public void setPreviewURL (String previewURL) {
		this.previewURL = previewURL;
	}
	
	// Duration
	public int getDuration () {
		return duration;
	}
	void setDuration (int d) {
		this.duration = d;
	}
	
	// Progress (in track)
	public int getProgress () {
		return progress;
	}
	public void setProgress (int p) {
		this.progress = p;
	}
	
	// ID
	public String getID () {
		return ID;
	}
	public void setID (String ID) {
		this.ID = ID;
	}
	
	// Timestamp
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	// Playing
	public boolean getPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	// ContextURI
	public String getContextURI () {
		return contextURI;
	}
	public void setContextURI (String contextURI) {
		this.contextURI = contextURI;
	}
	
	// jsonPathsMap
	public static HashMap<String, String> getJsonPathsMap() {
		return new HashMap<String, String>(jsonPaths);
	}
	/** Uses the Map sets to get the <i>JSONPath</i> expression values of the 
	 * associated field keys when using {@code Song.initializeFromJSON()} to initialize 
	 * an array of <i>Song</i> objects. This <i>JSONPath</i> appends to the 
	 * <b>baseJSONPath</b> parameter passed in to Song.initializeFromJSON(), and works
	 * with {@code Song.setDefaultJsonFields()} to determine the field keys to get
	 * their path values. These Map sets can additionally be referred for 
	 * <i>JSONPath</i> values with the <b>extendedFields</b> parameter in 
	 * Song.initializeFromJSON(). 
	 *  
	 * 
	 * @param m A Map with sets of field keys and their <i>JSONPath</i> expression
	 * values that are referenced to initialize fields with 
	 * {@code Song.initializeFromJson()}.
	 * 
	 * @see JsonPath
	 */
	public static void putJSONFieldPaths(Map<String, String> m) {
		jsonPaths.putAll(m);
	}

	// DefaultJSONFields
	public static String[] getDefaultJsonFields() {
		String[] deepCopy = new String[defaultJsonFields.length];
		for (int i = 0; i < defaultJsonFields.length; i++) {
			deepCopy[i] = defaultJsonFields[i];
		}
		return deepCopy;
	}
	public static void setDefaultJSONFields(String... defaultJSONFields) {
		Song.defaultJsonFields = defaultJSONFields;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	/** Sets a field of the <i>Song</i> object by passing the <b>field</b> name as a 
	 * String and the <b>value</b> to change it to.
	 * 
	 * @param field The field to set as a String.
	 * @param value The value to set to the field.
	 */
	private void set (String field, Object value) {
	        	
    	try {
    		Field f = this.getClass().getDeclaredField(field);
    		
			Method method = this.getClass().getDeclaredMethod(
					"set" + Character.toUpperCase(field.charAt(0)) + field.substring(1),
					f.getType() );
			
			if (f.getType().isPrimitive()) {
				method.invoke(this, value); //primitive classes don't cast from Wrappers
			} else {
				method.invoke(this, f.getType().cast(value));
			}
			
		} catch (NoSuchFieldException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassCastException cce) {
			cce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Returns another song object with all the data of this one. 
	 * 
	 * @return A deep copy of this Song instance.
	 */
	public Song getDeepCopy () {
		
		Song copy = new Song(this.getName(), this.getArtists(), this.getAlbumName(), 
				this.getAlbumURL(), this.getAlbumURI(), this.getAlbumCover(),
				this.getPreviewURL(), this.getID(), this.getDuration(), 
				this.getProgress(), this.getTimestamp(), this.getPlaying(),
				this.getContextURI(), this.getTrackNumber());
		
		return copy;
	}
	
	/** 
	 * Uses the <i>JSONPath</i> library to get select song data from a JSON String,
	 * parsing the name, artists, ID, albumURL, and previewURL from this <b>json</b>,
	 * and additionally putting the data into <i>Song</i> objects in the <b>songs</b>
	 * array in the order they are listed. Each data element is stored as a
	 * String, except artists (TreeSet).
	 * 
	 * @param songs The array of Song objects to store the data in.
	 * @param json The JSON to parse the data from as a String
	 * @throws Exception 
	 */
	public static Song[] initializeFromJSON (String json) throws Exception {
		return initializeFromJSON(json, "$");
	}
	
	/** 
	 * Uses the <i>JSONPath</i> library to get select song data from a JSON String,
	 * parsing the name, artists, ID, albumURL, and previewURL from this <b>json</b>,
	 * and additionally putting the data into <i>Song</i> objects in the <b>songs</b>
	 * array in the order they are listed. Each data element is stored as a
	 * String, except artists (TreeSet). If this method doesn't initialize to the
	 * last element of the <b>songs</b> array, places a null after the last element 
	 * initialized.
	 * 
	 * <p>The library reads a String expression to parse a JSON String, with ($) 
	 * signifying the root of the JSON, (.) signifying a child of the previous 
	 * JSONObject, and a JSONObject name following each (.) until a path to the 
	 * desired object is expressed (i.e. "$.track.item.preview_url").
	 * 
	 * <p>An additional <b>baseJSONPath</b> parameter can be passed in to this 
	 * method to begin the paths to parse the song data. It uses the following paths
	 * for each data element, based off the Spotify Web API's JSON structure:
	 * 
	 * <table>
	 * <tr> <th> Song Field </th> <th> Path </th> </tr>
	 * 
	 * <tr> <td><i>name</i></td> 		<td>.name </td> </tr>
	 * <tr> <td><i>artists</i></td> 	<td>.artists[*].name </td> </tr>
	 * <tr> <td><i>ID</i></td> 			<td>.id </td> </tr>
	 * <tr> <td><i>albumURL</i></td> 	<td>.album.images[0].url </td> </tr>
	 * <tr> <td><i>previewURL</i></td> 	<td>.preview_url </td> </tr>
	 * </table>
	 * 
	 * @param json The JSON String to parse the data from.
	 * @param baseJSONPath The path to begin each parsing path for song data.
	 * @return Returns An array of <i>Song</i> objects.
	 * @throws Exception 
	 */
	public static Song[] initializeFromJSON (String json, String baseJSONPath,
			String... extendedFields) {
		
		// initializes songs[]
		Song[] songs = new Song[0];
		
		if (json.equals("{}")) {
			throw new JSONSongException("Empty JSON String: " + json);
		}
		
		// adds "$." if not already there
		if (!baseJSONPath.startsWith("$.")) {
			switch (baseJSONPath.length()) {
				case 0: 
				case 1: baseJSONPath = "$"; break;
				default: 
					if (baseJSONPath.charAt(0) != '$') {
						baseJSONPath = "$".concat(baseJSONPath);
					}
					if (baseJSONPath.charAt(1) != '.') {
						baseJSONPath = baseJSONPath.charAt(0) + '.' + baseJSONPath.substring(1);
					}
			}
		}
		
		int songCount = 0;
		
		// gets the baseJSONPath class to determine songCount
		Object o = JsonPath.read(json, baseJSONPath);
		 if (o == null) {
			throw new JSONSongException("No song information at baseJSONPath.", json);
			
		} else if (o instanceof JSONArray) {
			songCount = ((JSONArray) o).size();
			
			baseJSONPath = baseJSONPath.concat("[0]");
			
		} else if (o instanceof LinkedHashMap) { //JSONObject in the JSONPath library
			songCount = 1;
		}
		
		// initialize songs[] with correct count
		songs = new Song[songCount];
		for (int i = 0; i < songs.length; i++) {
			songs[i] = new Song();
		}
		
		// puts defaultJSONFields and extendedFields into one String array
		String[] fields = new String[defaultJsonFields.length + extendedFields.length];
		System.arraycopy(defaultJsonFields, 0, 		  // (array, beginIndex,
				fields, 0, defaultJsonFields.length); // array, beginIndex, endPosition)
		System.arraycopy(extendedFields, 0, 
				fields, defaultJsonFields.length, extendedFields.length);
		
		int songIndex = 0;
		while (songIndex < songCount) {
			
			// adds index to path end if not the first index
			if (songIndex != 0) {
				baseJSONPath = baseJSONPath.substring(0, baseJSONPath.length() - 3)
						.concat(String.valueOf(songIndex))
						.concat("]");
			}
			
			for (String field : fields) {
				try {
					if (field != "artists") {
						
						// doesn't use baseJSONPath if '$' at beginning of jsonPaths value
						String jsonPath = (jsonPaths.get(field).charAt(0) == '$') ?
								jsonPaths.get(field) : 
									baseJSONPath.concat(jsonPaths.get(field));
						
						Object value = JsonPath.parse(json).read(jsonPath);
						songs[songIndex].set(field, value);
					
					// different parsing for artists
					} else {
						LinkedHashSet<String> artists = new LinkedHashSet<String>();
						
						// get the JSONArray of artists
						JSONArray artistsArray = JsonPath.read( json, 
								baseJSONPath.concat(jsonPaths.get(field)) );
	
						for (Object artist : artistsArray) {
							LinkedHashMap lhm = (LinkedHashMap) artist;
							
							if (lhm.containsKey("name")) {
								artists.add(lhm.get("name").toString());
							}
						}
						
						songs[songIndex].setArtists(artists);
					}
				} catch (Exception e) {
					throw new JSONSongException("Couldn't parse " + field + 
							" of track", json, e);
				}
			}
			
			songIndex++;
		}
		
		if (songCount == 0) {
			throw new JSONSongException("No songs found.", json);
		}
		
		// marks song after last song initialized to null in array if more size
		if (songs.length > songCount && songCount == 0) {
			songs[songCount] = null;
		}
		
		return songs;
	}
	
	/** Equates to true if every field of the song object equates in all getter 
	 * methods, except for albumCover.
	 */
	@Override
	public boolean equals(Object o) {
		
		if ( !(o instanceof Song) ) {
			return false;
		} else {
			Song otherSong = (Song) o;
			
			String[] exceptionFields = {"albumCover", "jsonPathsMap"};
			
			for (Field field : this.getClass().getDeclaredFields()) {
				
				// doesn't compare the exceptionFields[]
				for (String exField : exceptionFields) {
					if (field.getName().equals(exField)) {
						continue;
					}
				}
				
				// compares the rest of the fields using their get methods
				try {
		    		
					Method method = this.getClass().getDeclaredMethod(
							"get" + Character.toUpperCase(field.getName().charAt(0))
								+ field.getName().substring(1));
					Object thisSongValue = method.invoke(this);
					
					method = otherSong.getClass().getDeclaredMethod(
							"get" + Character.toUpperCase(field.getName().charAt(0))
								+ field.getName().substring(1));
					Object otherSongValue = method.invoke(otherSong);
					
					if ( !(thisSongValue == null && otherSongValue == null) &&
							((thisSongValue == null ^ otherSongValue == null) ||
							!thisSongValue.equals(otherSongValue)) ) {
						return false;
					}
					
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException | IllegalArgumentException 
						| InvocationTargetException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return true;
		}
	}
}
