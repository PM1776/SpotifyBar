package logic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.TreeSet;

import javax.imageio.ImageIO;

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
 *  <li> 2022-2-9: The program runs excellently in Eclipse, but fails to load the mp3 codec
 *  	when getAudioInputStream is called in an executable jar. There seems
 *  	to be many problems with this library, as problems concerning it
 *  	in executable jars is very common and with many different solutions working
 *  	and not working. It is also evident to perhaps not have been fully completed 
 *  	under the hood. I am leaving the problem here as I feel I have more
 *  	necessary things to do.
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
	
	/** Keeps track of if the player is playing. **/
	private static boolean playing;
	/** Keeps track of if the track was completely stopped. */
	private static boolean wasStopped;
	/** Keeps track of if the track was paused in the middle. */
	private static boolean wasPaused;
	
	/** Keeps track of how long the track has been playing in seconds. */
	private static double currentTrackSecs;
	
	/** Keeps track of the seconds between starting the track
	 * and pausing it in seconds. */
	private static double pausedTrackSecs;
	
	public static void main (String[] args) {
		
		display = Display.createDisplayInstance();
		
		// Puts a song on the player, but doesn't play it
		Song[] searchSongs = searchForSongs("pull up nobigdyl.", 1);
		display.placeSongOnPlayer( 
				(currentSong = searchSongs[0]).getDeepCopy());
	}
	
	/** Called when the user presses enter in searchBar in searching for a track.
	 * 
	 * @param search The keywords to search for that are sent to the Spotify API. 
	 * */
	public static void SearchEnter (String search) {
		
		if (PlayerLogic.playing) {
			stopPreview();
		}
		if (wasPaused) {
			wasPaused = false;
			display.switchPlayPauseButton(false);
		}
		
		Song[] searchSongs = searchForSongs(search, 1);
		
		if (searchSongs != null) {
			PlayerLogic.currentSong = searchSongs[0];
			display.switchPlayPauseButton(false);
			playPreview();
		}
	}
	
	/** Creates the query sent to the Spotify API and returns an Array of type 
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
			
			//gets response from Spotify
	        HttpResponse<JsonNode> response = null;
			try {
				response = Unirest.get(host + query)
						.header("Authorization", "Bearer " + accessToken)
						.asJson();
			} catch (UnirestException e) {
				e.printStackTrace();
				return songs;
			}
				
			//Creates quickly readable string of response string with Gson library
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement je = new JsonParser().parse(response.getBody().toString());
			String AuthJsonReturn = gson.toJson(je);
			
			// makes sure results came back from the search
			if ( response.getBody().getObject().has("tracks") && response.getBody()
					.getObject()
					.getJSONObject("tracks")
					.getJSONArray("items").length() != 0) {
				
				// for every song returned from the Spotify API, stores details in 
				// the JSONArray response of the song in songs[]
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
						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find name in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (2) Artists, by first getting the JSONArray
					try {
						JSONArray artistsJsonArray = ( 
									( (JSONObject) response.getBody()
									.getObject()
									.getJSONObject("tracks")
									.getJSONArray("items")
									.get(searchidx) )
									
									.getJSONArray("artists"));
						
						//loops through JSONArray to get the JSONObject element "name"
						TreeSet<String> artists = new TreeSet<String>();
						for (Object artist : artistsJsonArray) {
							artists.add( ((JSONObject) artist).getString("name") );
						}
						
						songs[searchidx].setArtists(artists);
						
					} catch (JSONException e) {
						// Prints error and returned response to find invalid parsing
						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find artists in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (3) Adds the ID on Spotify servers to songs[]
					try {
						songs[searchidx].setID( 
								( (JSONObject) response.getBody()
								.getObject()
								.getJSONObject("tracks")
								.getJSONArray("items")
								.get(searchidx) )
								
								.getString("id"));
						
					} catch (JSONException e) {
						// Prints error and returned response to find invalid parsing
						e.printStackTrace();
						System.out.println("\n" + AuthJsonReturn + "\nCouldn't "
								+ "find ID in the returned response for songs["
								 + searchidx + "]\n");
					}
					
					// (4) Adds the albumURL to songs[]
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
						// Prints error and returned response to find invalid parsing
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
						// Prints error and returned response to find invalid parsing
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
	
	/** Loads the albumCover of a song object from its albumURL if it hasn't already
	 * been loaded. 
	 * 
	 * @param song The <i>Song</i> object to load the album cover of. 
	 */
	public static void loadAlbumCover(Song song) {
		
		if (song == null) {
			return;
		}
		
		// Checks if already loaded
		if (song.getAlbumCover() == null &&
				song.getAlbumURL() != null) {
			
			BufferedImage albumCover = null;
			
			// (1) Gets the stream of the album cover from the albumURL
			try ( InputStream is = Unirest.get(song.getAlbumURL())
						.asBinary()
						.getRawBody() ) {
				
				// (2) Stores the stream in a BufferedImage
				albumCover = ImageIO.read(is);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnirestException e) {
				e.printStackTrace();
			}
			
			// (3) Scales the BufferedImage
			albumCover = PlayerLogic.scaleImage(albumCover, 
					54 * Display.getAlbumCoverScale(),
					54 * Display.getAlbumCoverScale());
			
			// and sets it as the song albumCover
			song.setAlbumCover(albumCover);
		}
	}

	/** Scales an image to a size with low loss of quality using Thumbnailator Library.
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
	
	/** Starts playing the preview on a low priority <i>Thread</i> and places the 
	 * song on <b>display</b>.
	 */
	public synchronized static void playPreview() {
		
		if (currentSong == null) {
			JOptionPane.showMessageDialog(null, "No song to play preview of.",
        			"Debugging", 0);
			return;
		}
		
		PlayerLogic.playing = true;
		PlayerLogic.wasStopped = false;
		
		Thread trackThread = new Thread( () -> streamMp3(currentSong.getPreviewURL()) );
		trackThread.setPriority(Thread.MIN_PRIORITY);
		trackThread.start();
		
		//places title and album artwork on the display
		display.placeSongOnPlayer(currentSong.getDeepCopy());
	}
	
	/** Stops the preview by setting booleans to their proper values, which makes
	 * playPreview() thread finish. Additionally, resets track values
	 * and updates the GUI.
	 */
	public static void stopPreview () {
		PlayerLogic.playing = false;
		PlayerLogic.wasPaused = false;
		PlayerLogic.wasStopped = true;
		currentTrackSecs = 0.0;
		display.updateTrackBar(0, 0);
		
		System.out.println("Stopped!");
	}
	
	/** Pauses the preview by setting proper boolean values which makes
	 * prayPreview() thread finish.
	 */
	public static void pausePreview () {
		PlayerLogic.playing = false;
		PlayerLogic.wasPaused = true;
		
		System.out.println("Paused!");
	}
    
	/** Returns a boolean of whether player is set to play.
	 * 
	 * @return the state of playing.
	 */
    public static boolean isPlaying () {
    	return PlayerLogic.playing;
    }
    /** Sets whether the player is ready to play or not.
     * 
     * @param playing The state of playing.
     */
    static void setPlaying (boolean playing) {
    	PlayerLogic.playing = playing;
    }
    
    /** The current AudioInputStream of the player. */
    static private AudioInputStream in;
    /** The current AudioFormat of the player. */
    static private AudioFormat outFormat;
    /** The current DataLine.Info of the player. */
    static private Info info;
    /** The current SourceDataLine created by <b>info</b>. */
    static private SourceDataLine line;
    
    /** Plays the MP3 preview of the Spotify track using an extension to read
     * MP3's with the Java Sound library.
     * 
     * @param previewURL the previewURL of the track returned from the Spotify API.
     * @author oldo (stackoverflow.com)
     * */
    public static void streamMp3(String previewURL) {
    	
        try {
        	// (1) Closes the current SourceDataLine and AudioInputStream
        	// if the track was stopped and no longer playing
            if (wasStopped) {
            	line.drain();
            	line.stop();
            	line.close();
            	
            	in.close();
            }
        	
        	// (2) Creates the new Audio objects of a track if player wasn't paused,
            // which is the only case the player shouldn't create new ones
            if (!wasPaused) {
            	
            	URL url = new URL(previewURL);
            	// line where code breaks in .jar
            	in = getAudioInputStream(url);
            	
		        outFormat = getOutFormat(in.getFormat());
		        info = new Info(SourceDataLine.class, outFormat);
		        
		        line = (SourceDataLine) AudioSystem.getLine(info);
		        line.open(outFormat);
            	line.start();
            }
            
            /* (3) Gets duration of the AudioInputStream (all preview tracks are 
             30 seconds). In order to actually get the frame length, which can
             be used to calculate duration, either the MPEGFormatFilerReader.class
             in the mp3spi-1.9.5-1.jar Maven dependecy must be updated, or another
             library should be selected. */
            currentSong.setDuration(30);
            
            // checks if the current line is still valid
            if (line != null) {
                stream(getAudioInputStream(outFormat, in), line);
            } else {
            	JOptionPane.showMessageDialog(null, "Line was broken. Please try again.",
            			"Connection Broke", 0);
            	wasPaused = false;
            }
            
        } catch (UnsupportedAudioFileException 
               | LineUnavailableException 
               | IOException e) {
            throw new IllegalStateException(e);
        }
    }
 
    /** Returns an AudioFormat object that only differs in a sampleSizeInBytes of 16
     * (retaining sampleRate, channel, frameSize, and frameRate of <b>inFormat</b>), which is 
     * a format that supports MP3's and OGG Vorbis'.
     * 
     * @param inFormat the AudioFormat object that the new one bases on.
     * @return an AudioFormat object of an MP3, WAV and OGG Vorbis.
     * @author oldo (stackoverflow.com)*/
    private static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }
    
    /** Writes the newly created AudioInputStream in mp3 format to a SourceDataLine
     *  received from AudioSystem.
     * 
     * @param in An AudioInputStream to receive the data from.
     * @param line The SourceDataLine to write the data to.
     * @author oldo (stackoverflow.com)*/
    private static void stream(AudioInputStream in, SourceDataLine line) 
        throws IOException {
    	
        final byte[] buffer = new byte[4096];
        int n;
        double startMilli = (!wasPaused) ? System.currentTimeMillis() :
        	System.currentTimeMillis() - pausedTrackSecs * 1000;
        
        for (n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
        	if (playing) {
        		line.write(buffer, 0, n);
        		
        		// Calculate current track second of the preview mp3
        		currentTrackSecs = (System.currentTimeMillis() - startMilli)
        				/ 1000;
        		
        		display.updateTrackBar(currentTrackSecs, currentSong.getDuration());
        	} else {
        		pausedTrackSecs = currentTrackSecs;
        		playing = false;
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
