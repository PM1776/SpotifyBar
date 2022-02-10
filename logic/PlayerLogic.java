package logic;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.coobird.thumbnailator.Thumbnails;
import renderer.Display;
import renderer.Images;

/** Handles all the logic of a music player, including loading songs from
 * Spotify servers, loading album art, and playing, pausing, and stopping a
 * preview of a song. This class holds the instance of the <i>Display</i> class
 * and the main method for the program.
 * 
 * </br></br>Bugs:
 * <ul>
 *  <li> 2022-2-9: The program runs excellently in Eclipse, but fails to create 
 *  	the AudioInputStream when getAudioInputStream is called in an executable jar 
 *  	and just stops the method. It wouldn't play a .wav file either, nor would
 *  	play in creating a separate URL of BufferedInputStream into AudioInputStream.
 *  	I am leaving the problem here as I feel I have more necessary things to do.
 *  </ul>
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class PlayerLogic {
	
	/** An instance of the <i>Display</i> class. */
	private static Display display;
	
	/** The token received by Spotify to access their servers. */
	private static String accessToken = SpotifyCredentials.getSpotifyAccessToken();
	
	/** The current song of the player. **/
	private static Song currentSong;
	
	/** Keeps track of if the player is playing, paused, or stopped. */
	private static int playerState;
	
	/** The int value for the player to be in a playing state. */
	public static final int PLAYING = 1;
	
	/** The int value for the player to be in a paused state. */
	public static final int PAUSED = 2;
	
	/** The int value for the player to be in a stopped state. */
	public static final int STOPPED = 3;
	
	/** Keeps track of how long the track has been playing in seconds. */
	private static double currentTrackSecs;
	
	/** Keeps track of the seconds between starting the track
	 * and pausing it in seconds. */
	private static double pausedTrackSecs;
	
	public static void main (String[] args) {
		
		display = Display.createDisplayInstance();
		
		// Loads a song on the player, but doesn't play it
		currentSong = searchForSongs("battle scars", 1)[0];
		display.placeSongOnPlayer(currentSong);
	}
	
	/** 
	 * Called when the user presses enter in searchBar in searching for a track.
	 * 
	 * @param search The keywords to search for that are sent to the Spotify API. 
	 * */
	public static void SearchEnter (String search) {
		
		// Resets player by stopping if not already
		switch (playerState) {
			case PAUSED:
			case PLAYING:
				stopPreview();
		}
		
		Song[] searchSongs = searchForSongs(search, 1);
		
		// came back with valid response
		if (searchSongs != null) {
			currentSong = searchSongs[0];
			display.switchPlayPauseButton(false);
			playPreview();
		}
	}
	
	/** 
	 * Creates the query sent to the Spotify API and returns an Array of type 
	 * <i>Song</i>.
	 * 
	 * @param search The keywords of the query.
	 * @param limit The number of songs to return.
	 * @return A <i>Song</i> array of size <b>limit</b> returned from the search results.
	 */
	public static Song[] searchForSongs(String search, int limit) {
		
		Song songs[] = new Song[limit];
		for (int i = 0; i < limit; i++) {
			songs[i] = new Song();
		}
		
		if (accessToken != null) {
			
		    String host = "https://api.spotify.com/v1/search";
		    
	        String q = search.replaceAll(" ", "+"); //q param is the search, spaces must be '+' or '%20' (UTF-8)
	        String type = "track";
	        String lim = Integer.toString(limit);
	        
		    String query = "?q=" + q +
		    		"&type=" + type +
		    		"&limit=" + lim;
			
			// Gets response from Spotify
	        HttpResponse<JsonNode> response = null;
			try {
				response = Unirest.get(host + query)
						.header("Authorization", "Bearer " + accessToken)
						.asJson();
			} catch (UnirestException e) {
				e.printStackTrace();
				return songs;
			}
				
			// Creates easily readable string of response with Gson library
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement je = new JsonParser().parse(response.getBody().toString());
			String AuthJsonReturn = gson.toJson(je);
			
			// makes sure results came back from the search
			if ( response.getBody().getObject().has("tracks") && response.getBody()
					.getObject()
					.getJSONObject("tracks")
					.getJSONArray("items").length() != 0) {
				
				/*
				 *  for every song returned from the Spotify API, stores 
				 *  certain song data of the JSONArray response in a 
				 *  <i>Song</i> object in array songs[]
				 * */
				for (int searchidx = 0; searchidx < limit; searchidx++) {
					
					// (1) Adds name of track to songs[]
					try {
						songs[searchidx].setName( 
								( (JSONObject) response.getBody()
								.getObject()
								.getJSONObject("tracks")
								.getJSONArray("items")
								.get(searchidx) )
								
								.getString("name"));
						
					} catch (JSONException e) {
						
						// Prints error and returned response to find invalid parsing
						// in all try/catches
						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find name in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (2) Adds artists, by first getting the JSONArray, 
					try {
						JSONArray artistsJsonArray = ( 
									( (JSONObject) response.getBody()
									.getObject()
									.getJSONObject("tracks")
									.getJSONArray("items")
									.get(searchidx) )
									
									.getJSONArray("artists"));
						
						// (2b) Loops through JSONArray to get the 
						// JSONObject elements, "name"
						TreeSet<String> artists = new TreeSet<String>();
						for (Object artist : artistsJsonArray) {
							artists.add( ((JSONObject) artist).getString("name") );
						}
						
						songs[searchidx].setArtists(artists);
						
					} catch (JSONException e) {

						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find artists in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (3) Adds ID on Spotify servers to songs[]
					try {
						songs[searchidx].setID( 
								( (JSONObject) response.getBody()
								.getObject()
								.getJSONObject("tracks")
								.getJSONArray("items")
								.get(searchidx) )
								
								.getString("id"));
						
					} catch (JSONException e) {

						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find ID in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (4) Adds albumURL to songs[]
					try {
						songs[searchidx].setAlbumURL(
								( (JSONObject)	
										
								((JSONObject) response.getBody()
								.getObject()
								.getJSONObject("tracks")
								.getJSONArray("items")
								.get(searchidx))
								
								.getJSONObject("album")
								.getJSONArray("images")
								.get(0) )
								
								.getString("url") );
						
					} catch (JSONException e) {

						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find image url in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// and (5) adds previewURL to songs[]
					try {
						songs[searchidx].setPreviewURL( 
								( (JSONObject) response.getBody()
								.getObject()
								.getJSONObject("tracks")
								.getJSONArray("items")
								.get(searchidx) )
								
								.getString("preview_url") );
						
					} catch (JSONException e) {

						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find preview url in the returned response for songs["
								 + searchidx + "]\n");
						JOptionPane.showMessageDialog(null, "No preview URL found for this track.", "I Am Outdated", 0);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "No songs found", "Nothin'", 0);
				return null;
			}
		} else {
			JOptionPane.showMessageDialog(null, "Cannot access Spotify Servers", "Problem with Connection", 0);
			return null;
		}

		return songs;
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
			
			BufferedImage albumCover = null;
			
			/*
			 *  (1) Gets stream of the album cover from the albumURL
			 *  and stores it in a BufferedImage
			 */ 
			try ( InputStream is = Unirest.get(song.getAlbumURL())
						.asBinary()
						.getRawBody() ) {
				
				albumCover = ImageIO.read(is);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnirestException e) {
				e.printStackTrace();
			}
			
			// (2) Scales BufferedImage,
			albumCover = PlayerLogic.scaleImage(albumCover, 
					54 * Display.getAlbumCoverScale(),
					54 * Display.getAlbumCoverScale());
			
			// and sets it as the song albumCover
			song.setAlbumCover(albumCover);
		}
	}

	/** 
	 * Scales a BufferedImage to a size using Thumbnailator Library.
	 * 
	 * @param buffI The <i>BufferedImage</i> to scale.
	 * @param WIDTH The width to scale to.
	 * @param HEIGHT The height to scale to.
	 * @return the scaled <i>BufferedImage</i>.
	 */
	public static BufferedImage scaleImage(BufferedImage buffI, int WIDTH, int HEIGHT) {
		try {
			buffI = Thumbnails.of(buffI)
			        .size(WIDTH, HEIGHT)
			        .asBufferedImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffI;
	}
	
	/** 
	 * Starts playing the preview on a low priority <i>Thread</i> and places the 
	 * song on <b>display</b>.
	 */
	public synchronized static void playPreview() {
		
		if (currentSong == null) {
			JOptionPane.showMessageDialog(null, "No song to play preview of.",
        			"Song not loaded", 0);
			return;
		}
		
		Thread trackThread = new Thread( () -> streamMp3(currentSong.getPreviewURL()) );
		trackThread.setPriority(3);
		trackThread.start();
		
		//places title and album artwork on the display
		display.placeSongOnPlayer(currentSong.getDeepCopy());
	}
	
	/** 
	 * Stops preview by setting playerState to STOPPED, which makes
	 * playPreview() thread finish. Additionally, resets track value
	 * and updates the GUI.
	 */
	public static void stopPreview () {
		PlayerLogic.playerState = STOPPED;
		
		currentTrackSecs = 0.0;
		display.updateTrackBar(0, 0);
		
		System.out.println("Stopped!");
	}
	
	/** 
	 * Pauses preview by setting the playerState to PAUSED, which makes
	 * playPreview() thread finish.
	 */
	public static void pausePreview () {
		playerState = PAUSED;
		
		System.out.println("Paused!");
	}
    
	/** 
	 * Returns the int value of the player state, with public constants of
	 * the class <i>PLAYING</i> = 1, <i>PAUSED</i> = 2, and <i>STOPPED = 3</i>.
	 * 
	 * @return the state of the player.
	 */
    public static int getPlayerState () {
    	return PlayerLogic.playerState;
    }
    
    /** The current AudioInputStream of the player. */
    static private AudioInputStream in;
    
    /** The current AudioFormat of the player. */
    static private AudioFormat outFormat;
    
    /** The current DataLine.Info of the player. */
    static private Info info;
    
    /** The current SourceDataLine created by <b>info</b>. */
    static private SourceDataLine line;
    
    /** 
     * Plays the MP3 preview of the Spotify track with the Java Sound library
     * using an MP3 extension from tritonus.
     * 
     * @param previewURL the previewURL of the track returned from the Spotify API.
     * @author oldo (stackoverflow.com)
     * */
    public static void streamMp3(String previewURL) {
    	
        try {
        	
        	// (1) Establish audio connections if stopped or first song played
        	switch (playerState) {
				
	        	// Closes current Audio objects if stopped
	        	case STOPPED:
	        		
				line.drain();
				line.stop();
				line.close();
				in.close();
	            	
	            	// Entry point for first song played 
	        	case 0:
	        		
	        		URL url = new URL(previewURL);
	        		in = getAudioInputStream(url);
	        		
			        outFormat = getOutFormat(in.getFormat());
			        info = new Info(SourceDataLine.class, outFormat);
			        line = (SourceDataLine) AudioSystem.getLine(info);
			        
			        line.open(outFormat);
	            		line.start();
        	}
        	
             /* 
             (2) Gets duration of the AudioInputStream (set to 30 seconds; 
             in order to actually get the frame length, which can be used 
             to calculate duration, either the MPEGFormatFilerReader.class 
             in the mp3spi-1.9.5-1.jar Maven dependecy must be updated, or 
             another library should be selected.      
             */
            currentSong.setDuration(30);
            
            // checks if current line is still valid
            if (line != null) {
                stream(getAudioInputStream(outFormat, in), line);
            } else {
            	JOptionPane.showMessageDialog(null, "Line was broken. Please try again.",
            			"Connection Broke", 0);
            	playerState = STOPPED;
            }
            
        } catch (UnsupportedAudioFileException 
               | LineUnavailableException 
               | IOException e) {
            throw new IllegalStateException(e);
        }
    }
 
    /** 
     * Returns an AudioFormat object in a format that supports MP3's and OGG Vorbis'.
     * 
     * @param inFormat the AudioFormat object that the new one bases on.
     * @return an AudioFormat object of an MP3, WAV and OGG Vorbis.
     * @author oldo (stackoverflow.com)
     * */
    private static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }
    
    /** 
     * Writes the newly created AudioInputStream in mp3 format to a SourceDataLine
     *  received from AudioSystem.
     * 
     * @param in An AudioInputStream to receive the data from.
     * @param line The SourceDataLine to write the data to.
     * @author oldo (stackoverflow.com)
     * */
    private static void stream(AudioInputStream in, SourceDataLine line) 
        throws IOException {
    	
        double startMilli = (playerState == PAUSED) ? System.currentTimeMillis() - 
        	pausedTrackSecs * 1000 : System.currentTimeMillis();
        
        playerState = PLAYING;
        
        int n;
        final byte[] buffer = new byte[4096];
        
        for (n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
        	
        	if (playerState == PLAYING) {
        		
        		line.write(buffer, 0, n);
        		 
        		// Calculates current track second of the preview mp3
        		currentTrackSecs = (System.currentTimeMillis() - startMilli)
        				/ 1000;
        		
        		// Updates display with currentTrackSecs
        		display.updateTrackBar(currentTrackSecs, currentSong.getDuration());
        		
        	} else {
        		
        		pausedTrackSecs = currentTrackSecs;
        		break;
        		
        	}
        }
        
        // finished playing track
        if (n == -1) {
        	stopPreview();
        	display.switchPlayPauseButton(true);
        }
    }
}
