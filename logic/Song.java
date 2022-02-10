package logic;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

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
	private TreeSet<String> artists;
	/** The name of the album. */
	private String albumName;
	/** The album cover as a BufferedImage. */
	private BufferedImage albumCover = null;
	
	/** The URL to retrieve the album cover. */
	private String albumURL;
	/** The URL to retrieve the song preview. */
	private String previewURL;
	
	/** The ID of the track on Spotify servers. */
	private String ID;
	
	/** The duration of the song in seconds. */
	private double duration;
	
	/** Creates a Song object to store a song's data. */
	public Song() {
		
	}
	
	public Song(String name, TreeSet<String> artists, String imageURL, String previewURL) {
		this.name = name;
		this.artists = artists;
		this.albumURL = imageURL;
		this.previewURL = previewURL;
	}
	
	public Song(String name, TreeSet<String> artists, String imageURL, String previewURL, 
			String albumName, String ID) {
		
		this(name, artists, imageURL, previewURL);
		
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
	public Song(String name, TreeSet<String> artists, String imageURL, String previewURL, 
			String albumName, String ID, double duration, BufferedImage albumCover) {
		
		this(name, artists, imageURL, previewURL, albumName, ID);
		
		this.duration = duration;
		this.albumCover = albumCover;
	}
	
	// Name
	public String getName() {
		return name;
	}
	void setName (String name) {
		this.name = name;
	}
	
	// Artists
	public TreeSet<String> getArtists() {
		return new TreeSet<String>(artists);
	}
	void setArtists(TreeSet<String> artists) {
		this.artists = artists;
	}
	
	// Album name
	public String getAlbumName() {
		return albumName;
	}
	
	// Album URL
	public String getAlbumURL() {
		return albumURL;
	}
	void setAlbumURL (String url) {
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
	void setAlbumCover(BufferedImage bi) {
		this.albumCover = bi;
	}
	
	// Preview URL
	public String getPreviewURL() {
		return previewURL;
	}
	void setPreviewURL (String previewURL) {
		this.previewURL = previewURL;
	}
	
	// Duration
	public double getDuration () {
		return duration;
	}
	void setDuration (double d) {
		this.duration = d;
	}
	
	// ID
	public String getID() {
		return ID;
	}
	void setID (String ID) {
		this.ID = ID;
	}
	
	/** Returns another song object with all the data of this one. 
	 * 
	 * @return A deep copy of this Song instance.
	 */
	public Song getDeepCopy () {
		Song copy = new Song(this.getName(), this.getArtists(), this.getAlbumURL(),
				this.getPreviewURL(), this.getAlbumName(), this.getID(),
				this.getDuration(), this.getAlbumCover());
		
		return copy;
	}
}
