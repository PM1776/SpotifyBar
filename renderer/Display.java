package renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import logic.PlayerLogic;
import logic.Song;

/** A Singleton class that creates an instance of the GUI. The display is entirely
 * composed of images from the <i>Images</i> class, and the album cover quality, 
 * using multiplier scale <b>albumCoverScale</b>, which can renders album covers bigger,
 * is also set in this class. This variable also inversely sets the scale the 
 * album cover is rendered in, allowing for much better graphics. Additionally, 
 * this class handles updating the GUI.
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class Display {
	
	/** The singleton instance of this class. */
	private static Display display;
	
	/** The invisible JFrame of the GUI. */
	private JFrame frame;
	/** The width of the JFrame. */
	static final int WIDTH = 565;
	/** The height of the JFrame. */
	static final int HEIGHT = 54;
	private final static String IMAGE_DIR = "\\src\\main\\resources\\";
	
	/** The PictureLabel that album covers will be displayed on. */
	private PictureLabel albumCoverLabel = new PictureLabel();
	/** The search bar that users can search for songs from Spotify. */
	private JTextField searchBar;
	private JPanel currentSongPanel = new JPanel();
	/** The label that song names will be displayed on. */
	private JLabel currentSongLabel = new JLabel();
	/** The label that song artists will be displayed on. */
	private JLabel currentArtistsLabel = new JLabel();
	
	/** PictureLabel with searchX of class Images as picture, and button highlighting enabled. */
	private PictureLabel searchX = new PictureLabel(Images.searchX, true);
	
	/** PictureLabel that switches between "play" and "pause" BufferedImages from 
	 * class Images as picture, and button highlighting enabled. */
	private PictureLabel play = new PictureLabel(Images.play, true);
	
	/** The track progress bar that will update according to the track's current
	 * frame and duration. */
	private PictureLabel trackBar = new PictureLabel(Images.blankTrackBar);
	
	/** Keeps track of if the JFrame is being dragged */
	boolean frameIsBeingMoved = false;
	/** Stores the X mouse coordinate within the frame. */
	int offSetMouseX = 0;
	/** Stores the Y mouse coordinate within the frame. */
	int offSetMouseY = 0;  
	
	/** The scale that album covers are scaled down to, which is * by the size it will
	 * be displayed into, with 1 equating its displayed size, and 4 being
	 * 4 * that size. A higher scale will increase the quality. */
	private static final int albumCoverScale = 3;
	
	/** Creates a <i>Display</i> instance if none is instantiated, and returns the same
	 * instance if already created. This object comprises a Spotify player bar GUI
	 * created from BufferedImages on an invisible JFrame, and creates "clear search"
	 * and play/pause buttons created from those BufferedImages. It has a search bar to
	 * enter Spotify server queries and displays the song info, cover art and a 
	 * player bar of the track.
	 * 
	 * @return a single display instance.
	 */
	public static Display createDisplayInstance () {
		if (display == null) {
			display = new Display();
		}
		
		return display;
	}
	
	private Display () {
		
		//create invisible JFrame
		frame = new JFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(screenSize.width - WIDTH - 30, 40, WIDTH, HEIGHT);
		frame.setTitle("Spotify Previewer");
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setAlwaysOnTop(true);
		
		// --------------------------------------------------------------------
		
		//create JLayeredPane component
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		layeredPane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		layeredPane.repaint();
		
		// ---------------------------------------------------------------------
		
		//create background image and allow it to be dragged
		PictureLabel background = new PictureLabel(Images.background);
		background.setLayout(null);
		background.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		background.addMouseMotionListener(new MouseAdapter() {
		     public void mouseDragged(MouseEvent e) {
				//sets frame position when mouse dragged			
				frame.setLocation(e.getXOnScreen() - offSetMouseX,
						e.getYOnScreen() - offSetMouseY);
		     }
		});
		background.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				offSetMouseX = e.getX(); //e.get-() is the mouse
				offSetMouseY = e.getY(); //location in the frame
			}
		});
		
		// -------------------------------------------------------------------------
		
		//place albumCoverLabel where cover art will be,
		albumCoverLabel.setLayout(null);
		albumCoverLabel.setBounds(511, 0, 54, 54);
		albumCoverLabel.setScale(1.0 / albumCoverScale);
		
		//and create a gray BufferedImage to put in place of album Cover
		BufferedImage blankAlbumBuf = new BufferedImage (54 * albumCoverScale, 
				54 * albumCoverScale, BufferedImage.TYPE_INT_RGB);
		
		Graphics g = blankAlbumBuf.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 54 * albumCoverScale, 54 * albumCoverScale);
		g.dispose();
		
		albumCoverLabel.setPicture(blankAlbumBuf);
		
		// -------------------------------------------------------------------------
		
		//create searchBar that is see-through with white text
		searchBar = new JTextField()  {
		    @Override public void setBorder(Border border) {
		        // Removing this code removes borders
		    }
		    
		    /** Overrides in order to completely repaint background, working
		     * with {@code setOpaque(false)}, which makes sure the
		     * parent components paint first, to allow for a background Color
		     * that is semi-transparent. */
		    @Override
		    protected void paintComponent(Graphics g)
		    {
		        g.setColor( getBackground() );
		        g.fillRect(0, 0, getWidth(), getHeight());
		        super.paintComponent(g);
		    }
		};
		searchBar.setLayout(null);
		searchBar.setBounds(28, 15, 235, 28);
		searchBar.requestFocusInWindow();
		searchBar.setBackground(new Color(0, 0, 0, 0));
		searchBar.setOpaque(false);
		searchBar.setForeground(Color.WHITE);
		searchBar.setCaretColor(Color.WHITE);
		searchBar.setSelectionColor(Color.WHITE);
		searchBar.setSelectedTextColor(new Color(165, 165, 165, 255));
		searchBar.addKeyListener(new KeyAdapter() {
	          public void keyPressed(KeyEvent e) {
	            //System.out.println("Pressed " + e.getKeyChar());
	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	            	PlayerLogic.SearchEnter(searchBar.getText());
	            }
	          }
	    });
		searchBar.addMouseListener(new MouseListener() {
	          public void mouseEntered(MouseEvent e) {
	        	  	searchBar.setBackground(new Color(255, 255, 255, 105));
	          }
	          public void mouseExited(MouseEvent e) {
	        	  	searchBar.setBackground(new Color(0, 0, 0, 0));
	        	  	redispatchToSearchX(e, true);
	          }
	          public void mousePressed(MouseEvent e) {
				  	redispatchToSearchX(e, true);
			  }
			  public void mouseReleased(MouseEvent e) {
			   		redispatchToSearchX(e, true);
			  }
			  public void mouseClicked(MouseEvent e) {
				  	redispatchToSearchX(e, true);
			  }
	    });
		searchBar.add(searchX);
		
		// ------------------------------------------------------------------------
		
		//place searchX in right location and add its listeners
		searchX.setLayout(null);
		searchX.setBounds(241, 20, 18, 18); //18x18
		searchX.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				searchBar.setText("");
			}
		});
		
		// ------------------------------------------------------------------------
		
		//place Play/Pause button in the right location and add listeners
		play.setLayout(null);
		play.setBounds(278, 2, 48, 48); //48x48
		play.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!PlayerLogic.isPlaying()) {
					PlayerLogic.playPreview();
					play.setPicture(Images.pause);
				} else {
					PlayerLogic.pausePreview();
					play.setPicture(Images.play);
				}
			}
		});
		
		// -------------------------------------------------------------------------
		
		currentSongPanel.setLayout(null);
		currentSongPanel.setBounds(334, 9, 175, 20);
		
		// Place song name label where it will be, and change font
		currentSongLabel.setLayout(null);
		currentSongLabel.setBounds(334, 6, 175, 20);
		currentSongLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
		currentSongLabel.setForeground(Color.WHITE);
		
		// Place artists label where it will be, and change font
		currentArtistsLabel.setLayout(null);
		currentArtistsLabel.setBounds(334, 21, 175, 20);
		currentArtistsLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
		currentArtistsLabel.setForeground(Color.LIGHT_GRAY);
		
		// -----------------------------------------------------------------------
		
		// Places the player track bar where it will be
		trackBar.setLayout(null);
		trackBar.setBounds(334, 38, 175, 10);
		
		// -----------------------------------------------------------------------
		
		//add components to layeredPane (the higher, the further on the bottom)
		layeredPane.add(play, 10);
		layeredPane.add(currentSongLabel, 11);
		layeredPane.add(currentArtistsLabel, 11);
		layeredPane.add(trackBar, 11);
		layeredPane.add(albumCoverLabel, 15);
		layeredPane.add(searchBar, 17);
		layeredPane.add(searchX, 19);
		layeredPane.add(background, 20);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(layeredPane);
		frame.setVisible(true); 
	}
	
	/** Switches and play and pause images on PictureLabel play. True is <i>play</i>,
	 * and false is <i>pause</i>.
	 * 
	 * @param playButton true is play, false is pause.
	 */
	public void switchPlayPauseButton (boolean playButton) {
		if (playButton) {
			play.setPicture(Images.play);
		} else {
			play.setPicture(Images.pause);
		}
	}
	
	/** Displays the title and album artwork of the song on the player,
	 * but does not play it.
	 * 
	 * @param song The song to display on the player.
	 */
	public void placeSongOnPlayer(Song song) {
		if (song != null) {
			String currentTrackText = song.getName();
			String currentArtists = "";
			
			//place currentSongNameLabel where it will be
			JLabel currentSongNameLabel = new JLabel(song.getName());
			currentSongNameLabel.setLayout(null);
			currentSongNameLabel.setBounds(340, 9, 175, 20);
			currentSongNameLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
			currentSongNameLabel.setForeground(Color.WHITE);
			currentSongNameLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					
				}
				public void mouseEntered(MouseEvent e) {
					e.getComponent().setForeground(Color.GRAY);
				}
				public void mouseExited(MouseEvent e) {
					e.getComponent().setForeground(Color.WHITE);
				}
			});
			
			// Artists are in order of last put in, so iterator runs them backwards
			Iterator<String> it = song.getArtists().descendingIterator();
			String nextArtist;
			
			while ((nextArtist = it.next()) != song.getArtists().first()) {
				currentArtists += nextArtist + ", ";
				
				JLabel currentSongArtistLabel = new JLabel();
				currentSongArtistLabel.setLayout(null);
				currentSongArtistLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
				currentSongArtistLabel.setForeground(Color.WHITE);
				currentSongArtistLabel.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						
					}
					public void mouseEntered(MouseEvent e) {
						e.getComponent().setForeground(Color.GRAY);
					}
					public void mouseExited(MouseEvent e) {
						e.getComponent().setForeground(Color.WHITE);
					}
				});
			}
			currentArtists += song.getArtists().first();
			
			currentSongLabel.setText(currentTrackText);
			currentArtistsLabel.setText(currentArtists);
			//ready currentSongAlbumLabel to move
			
			PlayerLogic.loadAlbumCover(song);
			
			albumCoverLabel.setPicture(song.getAlbumCover());
		} else {
			System.out.println("No song to display on player.");
		}
	}
	
	/** Returns the scale albums are resized down to, with 1 being its
	 * display size, and 4 being 4 times that display size.
	 * 
	 * @return The album cover scale.
	 */
	public static int getAlbumCoverScale() {
		return albumCoverScale;
	}
	
	/** Updates the track bar of the player according to the seconds played and seconds
	 * total. Setting <b>current</b> and <b>duration</b> both to 0 will set a blank
	 * track bar BufferedImage to trackBar.
	 * 
	 * @param current The current second in the track as a double.
	 * @param duration The length of the track in seconds as a double.
	 */
	public void updateTrackBar (double current, double duration) {
		if (current == 0 && duration == 0) {
			trackBar.setPicture(Images.blankTrackBar);
		}
		
		BufferedImage bImg = Images.createTrackBarBufImg(current, duration, false);
		trackBar.setPicture(bImg);
	}
	
	/** Redispatches mouse events to the searchX component underneath searchBar.
	 * 
	 * @param e The MouseEvent.
	 * @param repaint Repaints the searchX component if true.
	 */
	private void redispatchToSearchX(MouseEvent e, boolean repaint) {
		Point searchBarPoint = e.getPoint();
		Container container = searchX;
		Point containerPoint = SwingUtilities.convertPoint(
		              searchBar,
		              searchBarPoint,
		              container);
		
		if (containerPoint.y < 0) { //we're not in the content pane
		//Could have special code to handle mouse events over
		//the menu bar or non-system window decorations, such as
		//the ones provided by the Java look and feel.
		} else {
		//The mouse event is probably over the content pane.
		//Find out exactly which component it's over.
		Component component =
		SwingUtilities.getDeepestComponentAt(
		              container,
		              containerPoint.x,
		              containerPoint.y);
		
			if ((component != null)
			&& (component.equals(searchX))) {
				//Forward events over the check box.
				Point componentPoint = SwingUtilities.convertPoint(
				                  searchBar,
				                  searchBarPoint,
				                  component);
				component.dispatchEvent(new MouseEvent(component,
				                           e.getID(),
				                           e.getWhen(),
				                           e.getModifiers(),
				                           componentPoint.x,
				                           componentPoint.y,
				                           e.getClickCount(),
				                           e.isPopupTrigger()));
			}
		}
		
		//Update the glass pane if requested.
	    if (repaint) {
	        searchX.repaint();
	    }
	}
}

/** Adds BufferedImage <b>picture</b> field to a JLabel class,
 * which can be highlighted appropriately when the mouse hovers over
 * or clicks the PictureLabel. */
class PictureLabel extends JLabel {
	
	private BufferedImage originalPicture = null;
	private BufferedImage picture2Paint = null;
	
	private boolean circleButton = false;
	private boolean scaled = true;
	
	private double scale = 1.0 / Images.getScale();
	
	/** A PictureLabel without a picture, with scaling at default <b>true</b>.
	 * 
	 * @see PictureLabel */
	public PictureLabel () {}
	
	/** A PictureLabel with scaling at default <b>true</b>. */
	public PictureLabel (BufferedImage bufImg) {
		this.originalPicture = bufImg;
		this.picture2Paint = originalPicture;
		
		if (bufImg.equals(Images.play)) {
			circleButton = true;
		}
	}
	
	/** Stores <b>bufImg</b> and determines if a circleButton. Additionally,
	 * can allow button highlighting functionality. 
	 * 
	 * @param bufImg BufferedImage to set as the picture of the PictureLabel.
	 * @param buttonHighlight If true, will highlight the PictureLabel when the mouse
	 * hovers over and clicks the PictureLabel. 
	 * */
	public PictureLabel(BufferedImage bufImg, boolean buttonHighlight) {
		this(bufImg);
		
		if (buttonHighlight) {
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(0, 0, 0, 57)));
					repaint();
				}
				public void mouseReleased(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(255, 255, 255, 57)));
				}
				public void mouseEntered(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(255, 255, 255, 57)));
					repaint();
				}
				public void mouseExited(MouseEvent e) {
					setPaintedPicture(originalPicture);
				}
			});
		}
	}
	
	/** Stores <b>bufImg</b> and determines if a circleButton. Additionally,
	 * can allow button highlighting functionality as well as change scaling to
	 * false. It is set to true by default to allow high quality pictures to
	 * retain their quality in scaling the picture with the Graphics library, 
	 * which can scale about 4x smaller without loosing much quality.
	 * 
	 * @param bufImg BufferedImage to set as the picture of the PictureLabel.
	 * @param buttonHighlight If true, will highlight the PictureLabel when the mouse
	 * 		hovers over and clicks the PictureLabel. 
	 * @param scaled Sets the scaling instance Pictures are painted with. A lower
	 * 		scale with a high resolution picture will render the picture with 
	 * 		higher quality.
	 * */
	public PictureLabel (BufferedImage bImg, boolean buttonHighlight, double scale) {
		this (bImg, buttonHighlight);
		
		this.scale = scale;
	}
	
	/** Returns another BufferedImage of the picture of this PictureLabel.
	 * 
	 * @return A BufferedImage of picture.
	 */
	public BufferedImage getPicture () {
		if (originalPicture != null) {
			// Creates a new BufferedImage to return to protect its reference
			BufferedImage bufImg = new BufferedImage(originalPicture.getWidth(), 
					originalPicture.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			
			Graphics g = bufImg.getGraphics();
			g.drawImage(originalPicture, 0, 0, null);
			g.dispose();
			
			return bufImg;
		} else {
			return null;
		}
	}
	/** Sets the BufferedImage to be painted on the PictureLabel. 
	 * 
	 * @param bufImg BufferedImage to be painted. */
	public void setPicture (BufferedImage bufImg) {
		originalPicture = bufImg;
		picture2Paint = originalPicture;
		this.repaint();
	}
	
	private void setPaintedPicture (BufferedImage bufImg) {
		picture2Paint = bufImg;
		this.repaint();
	}
	
	public double getScale () {
		return scale;
	}
	void setScale(double scale) {
		this.scale = scale;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (scaled) {
			g2d.scale(scale, scale);
		}
		g2d.drawImage(picture2Paint, null, 0, 0);
		g2d.dispose();
	}
	
	/** Overlays a highlighted area of Color <b>hlColor</b> onto the 
	 * <b>originalPicture</b> BufferedImage. Intended to used to create a button
	 * from a base picture.
	 * 
	 *  @param hlColor The Color to highlight the button area with. 
	 *  @return The highlighted BufferedImage. */
	private BufferedImage highlightPicture (Color hlColor) {
		// (1) Creates a BufferedImage in size of originalPicture
		BufferedImage hlBufImg = new BufferedImage(
				originalPicture.getWidth(), originalPicture.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		
		// (2) Paints originalPicture onto the BufferedImage
		Graphics g = hlBufImg.getGraphics();
		g.drawImage(originalPicture, 0, 0, null);
		
		// (3) Fills the intended button area with param hlColor,
		// either with a circle or square based on circleButton
		g.setColor(hlColor);
		if (!circleButton) {
			g.fillRect(1, 2, originalPicture.getWidth() - 6,
					originalPicture.getHeight() - 5);
		} else {
			g.fillOval(0, 0, originalPicture.getWidth() - 6,
					originalPicture.getHeight() - 6);
		}
		g.dispose();
		
		return hlBufImg;
	}
}
