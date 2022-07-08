package logic.playerlogic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import kong.unirest.Unirest;
import logic.device.Device;
import logic.preview.Preview;
import logic.song.JSONPreset;
import logic.song.JSONSongException;
import logic.song.Song;
import logic.spotifyapi.SpotifyAPI;
import logic.spotifyapi.SpotifyAPIException;
import logic.spotifycredentials.SpotifyCredentials;
import net.coobird.thumbnailator.Thumbnails;
import renderer.display.Display;

/** Handles all the logic of a music player, including loading songs from
 * Spotify servers, loading album art, and playing, pausing, and stopping a
 * preview of a song. This class holds the instance of the <i>Display</i> class
 * and the main method for the program.
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class PlayerLogic implements PlayerActionConstants {
	
	/** An instance of the <i>Display</i> class. */
	private static Display display;
	
	/** Handles the Spotify authorization using either the Client Authorization flow
	 * or Authorization Code + PKCE flow as well as the Web API requests. */
	private static SpotifyAPI spotifyAPI = new SpotifyAPI();
	
	/** A boolean that is <b>true</b> if using the <i>Authorization Code + PKCE</i> flow 
	 * (accesses a Spotify account), and <b>false</b> if using the <i>Client 
	 * Authorization</i> flow (doesn't access a Spotify account and plays previews)
	 * to get resources from Spotify servers. 
	 * 
	 * @see SpotifyCredentials*/
	private static boolean authCodeFlow = true;
	
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
	
	/** A static field that all Preview instances are initialized to. */
	private static Preview preview;
	
	/** This device on Spotify servers. */
	private static Device thisDevice;
	
	/** A thread that gets updates on the playback state of Spotify every second. */
	private static Thread updatePlaybackState = createUpdateThread();
	
	/** The time the last accessToken was granted in seconds. */
	private static long accessTokenTime;
	
	/** ReentrantLock for thread stability. */
	private static ReentrantLock lock = new ReentrantLock();
	
	public static void main (String[] args) {
		loadPlayer();
	}
	
	public static void loadPlayer () {
		if (authCodeFlow) { // Auth Code flow
			
			try {
				spotifyAPI = new SpotifyAPI(() -> {
					
					if (spotifyAPI.authorizationSuccessful()) {
						accessTokenTime = System.currentTimeMillis() / 1_000;
						display = Display.createDisplayInstance(authCodeFlow);
						updatePlaybackState.run();	
					} else { // User probably closed approvalBrowser
						showUserUninterestedAlert();
					}
					
				});
			} catch (SpotifyAPIException se) {
				if (se.getMessage().contains("Could not get connection.")) {
					noConnectionAlert();
				}
			}
			
		} else { // Client Auth flow
			authorizeClientFlow();
		}
	}
	
	public static void authorizeClientFlow() {
		spotifyAPI = new SpotifyAPI();
		try {
			spotifyAPI.setSpotifyCredentials(false, null);
		} catch (SpotifyAPIException se) {
			if (se.getMessage().contains("Could not get connection")) {
				noConnectionAlert();
			}
		}
		
		display = Display.createDisplayInstance();
		
		try {
			String returnJson = spotifyAPI.searchForSongs("battle scars", 1);
			currentSong = Song.initializeFromJSON(returnJson, 
					JSONPreset.getJSONPresetbyName("search"))[0];
			currentSong.setAlbumCover(loadAlbumCover(currentSong.getAlbumURL(), true));
		} catch (SpotifyAPIException se) {
			if (se.getMessage().contains("Authorization was unsuccessful.")) {
				currentSong = new Song("No internet connection");
			}
		}
		
		display.displaySong(currentSong.getDeepCopy());
		
	}
	
	public static Thread createUpdateThread () {
		return new Thread( () -> {
			while (true) {
				lock.lock();
				
				Device activeDevice = new Device();
				
				try {
					// Checks if access token needs to be refreshed
					if (System.currentTimeMillis() / 1_000 - accessTokenTime > 355) {
						spotifyAPI.requestRefreshToken();
						accessTokenTime = System.currentTimeMillis() / 1_000;
					}
					// Gets the song on Spotify without albumCover
					
					String json = spotifyAPI.getPlaybackState(false);
					Song playbackSong = Song.initializeFromJSON(json, 
							JSONPreset.getJSONPresetbyName("playbackState"))[0];
					activeDevice = Device.initializeThisDevice(json);
					
					if (playbackSong != null) {
						
						// Loads the album cover if not already loaded
						if (currentSong == null ||
								currentSong.getID() != playbackSong.getID()) {
							
							currentSong = playbackSong;
							currentSong.setAlbumCover(
									loadAlbumCover(currentSong.getAlbumURL(), true));
						
						} else { // Just updates the data
							
							playbackSong.setAlbumCover(currentSong.getAlbumCover());
							currentSong = playbackSong;
							
						}
						
						display.displaySong(currentSong.getDeepCopy(), 
								!currentSong.isPlaying());
						
						playerState = (currentSong.isPlaying()) ? PLAYING : PAUSED;
					} else {
						System.out.println("No song returned.");
					}
				} catch (JSONSongException jse) {
					if (jse.getMessage().contains("Empty JSON String: {}")) {
						System.out.println("No active player.");
					} else if (jse.getMessage().contains("No song information at "
							+ "baseJSONPath")) {
						
					} else {
						jse.printStackTrace();
					}
					
				} catch (SpotifyAPIException e) {
					System.out.print("Could not connect to the internet");
				} finally {
					lock.unlock();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/** 
	 * Called when the user presses enter in searchBar in searching for a track.
	 * 
	 * @param search The keywords to search for that are sent to the Spotify API. 
	 * */
	public static void SearchEnter (String search) {
		
		// Client flow
		if (!authCodeFlow) { 
			// Stops preview if not already
			switch (playerState) {
				case PAUSED:
				case PLAYING:
					stopPreview();
			}
		}
		
		String json = spotifyAPI.searchForSongs(search, 1);
		Song searchSong = Song.initializeFromJSON(json,
				JSONPreset.getJSONPresetbyName("search"))[0];
		
		// came back with valid response
		if (searchSong != null) {
			
			searchSong.setAlbumCover(loadAlbumCover(searchSong.getAlbumURL(), true));
			
			try {
				boolean unlocked = false;
				do {
					unlocked = lock.tryLock();
					if (unlocked) {
						currentSong = searchSong;
						display.displaySong(currentSong.getDeepCopy(), false); // pause
						play();
					} 
				} while (!unlocked);
			} finally {
				lock.unlock();
			}
		} else {
			JOptionPane.showMessageDialog(null, "No songs found", "Nothin'", 0);
		}
	}
	
	/** 
	 * Starts playing the preview on a low priority <i>Thread</i> and places the 
	 * song on <b>display</b>.
	 */
	public synchronized static void play() {
		
		if (authCodeFlow) { // Authorization Code play
			
			String response = playerAction(PLAY);
			
		} else { // Client Authorization (plays preview)
			if (currentSong == null) {
				JOptionPane.showMessageDialog(null, "Must search for a song to play, or "
						+ "begin playback on another device to load song.",
	        			"Song not loaded", 0);
				
			} else if (currentSong.getPreviewURL() == null) {
				JOptionPane.showMessageDialog(null, "No preview URL for this track.",
        			"I Am Outdated", 0);
				
			} else {
				playPreview();
				SwingUtilities.invokeLater(() -> {
					display.displaySong(currentSong.getDeepCopy());
				});
				
				playerState = PLAYING;
			}
		}
	}
	
	/** 
	 * Pauses preview by setting the playerState to PAUSED, which makes
	 * playPreview() thread finish.
	 */
	public static String pause () {
		if (authCodeFlow) {
			return playerAction(PAUSE);
		} else {
			pausePreview();
			return "";
		}
	}
	
	public static String next() {
		return playerAction(NEXT);
	}
	
	public static String previous() {
		return playerAction(PREVIOUS);
	}
	
	public static String addToQueue(String search) {
		
		String json = spotifyAPI.searchForSongs(search, 1);
		Song songToQueue = Song.initializeFromJSON(json)[0];
		
		String response = "";
		
		if (songToQueue == null) {
			JOptionPane.showMessageDialog(null, "No song found to add to queue.",
        			"Song Not Found", 0);
			
		} else {
			response = spotifyAPI.addToQueue(songToQueue.getID(), null);
			if (response.contains("NO_ACTIVE_DEVICE")) {
				response = spotifyAPI.addToQueue(songToQueue.getID(), thisDevice);
			}
		}
		return response;
	}
	
	public static String playerAction (int action) {
		
		String response = "";
		Device thisDevice = Device.initializeThisDevice(spotifyAPI.getDevices());
			
		if (currentSong == null) { // no active device, no song, simply clicked
			
			switch (action) {
				case PLAY:
					if (thisDevice != null) {
						response = spotifyAPI.transferPlayback(true, thisDevice.getId());
					} else {
						showNoActiveDevicesAlert();
						display.setPlayPauseImage(true);
					}
					break;
				case PAUSE:
					JOptionPane.showMessageDialog(null, "No song to pause.",
		        			"No Song Playing", 0);
					return "";
				case NEXT:
					if (thisDevice != null) {
						response = spotifyAPI.nextFrom(thisDevice);
					} else {
						showNoActiveDevicesAlert();
					}
					break;
				case PREVIOUS:
					if (thisDevice != null) {
						response = spotifyAPI.previousFrom(thisDevice);
					} else {
						showNoActiveDevicesAlert();
					}
			}
			
		} else {
			// Tries to perform action from active device
			switch (action) {
				case PLAY:
					response = spotifyAPI.play(currentSong.getDeepCopy(), null);
					break;
				case PAUSE:
					response = spotifyAPI.pauseFrom(null);
					break;
				case NEXT:
					response = spotifyAPI.nextFrom(null);
					break;
				case PREVIOUS:
					response = spotifyAPI.previousFrom(null);
			}
			
			if (response.contains("NO_ACTIVE_DEVICE")) {
				// Perform action on this Device
				if (thisDevice != null) {
					switch (action) {
						case PLAY:
							response = spotifyAPI.transferPlayback(false, 
									thisDevice.getId());
							response = spotifyAPI.play(currentSong.getDeepCopy(), 
									thisDevice);
							break;
						case PAUSE:
							spotifyAPI.pauseFrom(thisDevice);
							break;
						case NEXT:
							spotifyAPI.nextFrom(thisDevice);
							break;
						case PREVIOUS:
							spotifyAPI.previousFrom(thisDevice);
					}
					
				} else { // Spotify not open (thisDevice = null), and no active device
					showNoActiveDevicesAlert();
				}
			}
			
			if (response.equals("{}")) {
				playerState = (action == PAUSED) ? PAUSED : PLAYING;
			} else {
				throw new RuntimeException(response);
			}
	
			// updatePlaybackState thread will update GUI
		}
		
		return response;
	}
	
	/** 
	 * Loads and returns a BufferedImage from the specified url. Can additionally
	 * scale to size used in display to prevent continual scaling.
	 * 
	 * @param url The URL to load the album cover from as a String. 
	 * @param displaySize if true, additionally scales albumCover to the size used
	 * by display.
	 * */
	public static BufferedImage loadAlbumCover (String url, boolean displaySize) {
		
		BufferedImage albumCover = spotifyAPI.loadAlbumCover(url);
		
		if (displaySize) {
			// scales albumCover to prevent continual scaling
			albumCover = display.scaleToAlbumCoverDisplaySize(albumCover);
		}
		
		return albumCover;
	}
	
	public static void playPreview() {
		try {
			if ( (preview == null) ||
				!(currentSong.getPreviewURL().equals(preview.getPreviewURL()))) {
				
				if (preview != null) {
					preview.close();
				}
				
				// creates a new Preview from the currentSong previewURL with a 
				// Runnable interface for GUI updating
				preview = new Preview(currentSong.getPreviewURL(), () -> {
					display.updateTrackBar(preview.getCurrentSec(),
							preview.getDuration());
				});
				
				// adds a halted listener to set play button back to 'play'
				preview.addHaltedListener(() -> {
					if (preview.getPreviewState() == Preview.FINISHED) {
			        	display.setPlayPauseImage(true);
			        	playerState = PAUSED;
			        }
				});
			}
			preview.playPreview();
			
		} catch (RuntimeException re) {
			JOptionPane.showMessageDialog(null, "Line was broken. Please try again.",
    			"Connection Broke", 0);
        	playerState = PlayerLogic.STOPPED;
        	re.printStackTrace();
		}
	}
	
	public static void pausePreview() {
		preview.pausePreview();
		playerState = PAUSED;
	}
	
	/** 
	 * Stops preview by setting playerState to STOPPED, which makes
	 * playPreview() thread finish. Additionally, resets track value
	 * and updates the GUI.
	 */
	public static void stopPreview () {
		PlayerLogic.playerState = STOPPED;
		preview.closePreview();
		display.updateTrackBar(0, 0);
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
    
	public static void noConnectionAlert() {
		int result = JOptionPane.showConfirmDialog(null, "Could not connect to Spotify."
				+ "\nRetry?",
				"WWW.YEETED...", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			loadPlayer();
		} else {
			System.exit(0);
		}
	}
	
	public static void showUserUninterestedAlert() {
		Object[] choices = {"Close", "Play Without Account"};
		Object defaultChoice = choices[0];
		int result = JOptionPane.showOptionDialog(null,
		             "Close, or play without account?",
		             "Anonymous One",
		             JOptionPane.YES_NO_OPTION,
		             JOptionPane.QUESTION_MESSAGE,
		             null,
		             choices,
		             defaultChoice);
		if (result == JOptionPane.YES_OPTION || result == JOptionPane.CLOSED_OPTION) {
			System.exit(0);
		} else {
			authCodeFlow = false;
			authorizeClientFlow();
		}
	}
	
	public static void showNoActiveDevicesAlert() {
		JOptionPane.showMessageDialog(null, "The Spotify application must"
				+ " be open, or another device's Spotify \napplication must"
				+ " be active (played a song recently) to play a song.", 
				"No Active Devices", JOptionPane.OK_OPTION);
	}
}