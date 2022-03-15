package logic.playerlogic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import kong.unirest.Unirest;
import logic.device.Device;
import logic.preview.Preview;
import logic.song.JSONSongException;
import logic.song.Song;
import logic.spotifyapi.SpotifyAPI;
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
public class PlayerLogic {
	
	/** An instance of the <i>Display</i> class. */
	private static Display display;
	
	/** Handles the Spotify authorization using either the Client Authorization flow
	 * or Authorization Code + PKCE flow as well as the Web API requests. */
	private static SpotifyAPI spotifyAPI;
	
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
	
	private static Preview preview;
	
	private static Device thisDevice;
	
	private static Thread updatePlaybackState = createUpdateThread();
	
	/** The time the last accessToken was granted in seconds. */
	private static long accessTokenTime;
	
	/** ReentrantLock for thread stability. */
	private static ReentrantLock lock;
	
	public static void main (String[] args) {
		
		if (authCodeFlow) { // Auth Code flow
			
			spotifyAPI = new SpotifyAPI(() -> {
				
				accessTokenTime = System.currentTimeMillis() / 1_000;
				
				display = Display.createDisplayInstance();
				thisDevice = spotifyAPI.getDevice();
				
				updatePlaybackState.run();
				
//				if (currentSong != null) {
//					display.displaySong(currentSong.getDeepCopy());
//					display.switchPlayPauseButton(!currentSong.getPlaying());
//				}
			});
			
		} else { // Client Auth flow
			spotifyAPI = new SpotifyAPI();
			currentSong = spotifyAPI.searchForSongs("battle scars", 1)[0];
			display = Display.createDisplayInstance();
			display.displaySong(currentSong.getDeepCopy());
		}
		
	}
	
	public static Thread createUpdateThread () {
		return new Thread( () -> {
			
			lock = new ReentrantLock();
			
			while (true) {
				lock.lock();
				try {
					// Checks if access token needs to be refreshed
					if (System.currentTimeMillis() / 1_000 - accessTokenTime > 355) {
						spotifyAPI.requestRefreshToken();
						accessTokenTime = System.currentTimeMillis() / 1_000;
					}
					
					Song playbackSong = spotifyAPI.getPlaybackState();
					
					if (playbackSong != null) {
						
						// Loads album cover if the first or not the same song
						if (currentSong == null ||
								currentSong.getID() != playbackSong.getID()) {
							
							currentSong = playbackSong;
							loadAlbumCover(currentSong);
						
						} else {
							
							playbackSong.setAlbumCover(currentSong.getAlbumCover());
							currentSong = playbackSong;
						}
						
						display.displaySong(currentSong.getDeepCopy());
						display.switchPlayPauseButton(!currentSong.getPlaying());
						if (currentSong.getPlaying()) {
							playerState = PLAYING;
						} else {
							playerState = PAUSED;
						}
						
					}
				} catch (JSONSongException jse) {
					if (jse.getMessage().contains("Empty JSON String: {}")) {
						System.out.println("No active player.");
					} else if (jse.getMessage().contains("No song information at baseJSONPath.")) {
						System.out.println("No song returned");
					} else {
						jse.printStackTrace();
					}
					
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
		
		Song[] searchSongs = spotifyAPI.searchForSongs(search, 1);
		
		// came back with valid response
		if (searchSongs != null) {
			try {
				boolean unlocked = false;
				do {
					unlocked = lock.tryLock();
					if (unlocked) {
						currentSong = searchSongs[0];
						display.switchPlayPauseButton(false);
						play();
					} 
				} while (!unlocked);
			} finally {
				lock.unlock();
			}
		}
	}
	
	/** 
	 * Starts playing the preview on a low priority <i>Thread</i> and places the 
	 * song on <b>display</b>.
	 */
	public synchronized static void play() {
		
		if (authCodeFlow) { // Authorization Code play
			if (currentSong == null) {
				spotifyAPI.transferPlayback(true, thisDevice);
			} else {
				// tries to play from active device
				String response = spotifyAPI.play(currentSong.getDeepCopy(), null); 
				if (response.contains("NO_ACTIVE_DEVICE")) {
					spotifyAPI.transferPlayback(false, thisDevice);
					Thread playThread = new Thread(() -> {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(spotifyAPI.play(currentSong.getDeepCopy(),
								thisDevice));
					});
					playThread.start();
					
				} else if (!response.equals("{}")){
					throw new RuntimeException(response);
				}
			}
			
			playerState = PLAYING;
			
			// updatePlaybackState thread will update the GUI
			
		} else { // Client Authorization play
			if (currentSong == null) {
				JOptionPane.showMessageDialog(null, "Must search for a song to play, or "
						+ "begin playback on another device to load song.",
	        			"Song not loaded", 0);
				
			} else if (currentSong.getPreviewURL() != null) {
				JOptionPane.showMessageDialog(null, "No preview URL for this track.",
        			"I Am Outdated", 0);
				stopPreview();
				
			} else {
				playPreview();
				SwingUtilities.invokeLater(() -> {
					
					display.displaySong(currentSong.getDeepCopy());
				});
				
				playerState = PLAYING;
			}
		}
	}
	
	public static void loadAlbumCover (Song song) {
		spotifyAPI.loadAlbumCover(song);
		
		// the song.getAlbumURL() is a link to a 640x640 image, so scale
		BufferedImage scaledAlbumCover = display.scaleImage(song.getAlbumCover(), 
				54 * display.getAlbumCoverScale(),
				54 * display.getAlbumCoverScale());
		song.setAlbumCover(scaledAlbumCover);
	}
	
	/** 
	 * Pauses preview by setting the playerState to PAUSED, which makes
	 * playPreview() thread finish.
	 */
	public static void pause () {
		if (currentSong == null) {
			JOptionPane.showMessageDialog(null, "No song playing.",
        			"Song not playing", 0);
			return;
			
		} else {
			if (authCodeFlow) {
				String response = spotifyAPI.pauseFrom(null); // from active device
				if (response.contains("NO_ACTIVE_DEVICE")) {
					spotifyAPI.pauseFrom(thisDevice);
				}
				playerState = PAUSED;
				
			} else {
				preview.pausePreview();
				playerState = PAUSED;
			}
		}
	}
	
	public static void playPreview() {
		try {
			if ((preview == null) ||
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
			        	display.switchPlayPauseButton(true);
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
	
	/** 
	 * Stops preview by setting playerState to STOPPED, which makes
	 * playPreview() thread finish. Additionally, resets track value
	 * and updates the GUI.
	 */
	public static void stopPreview () {
		PlayerLogic.playerState = STOPPED;
		preview.stopPreview();
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
}