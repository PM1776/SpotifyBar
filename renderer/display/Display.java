package renderer.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import logic.playerlogic.PlayerLogic;
import logic.song.Song;
import renderer.buttonpanel.ButtonPanel;
import renderer.images.Images;
import renderer.picturelabel.PictureLabel;

/** 
 * A Singleton class that creates an instance of the GUI. The display is entirely
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
	
	/** The width of the JFrame. */
	static final int WIDTH = 565;
	
	/** The height of the JFrame. */
	static final int HEIGHT = 54;
	
	/** The scale that album covers are scaled down to, which is * by the size it will
	 * be displayed into, with 1 equating its displayed size, and 4 being
	 * 4 * that size. A higher scale will increase the quality. */
	private static final int albumCoverScale = 3;
	
	private static Images images = new Images(4, WIDTH, HEIGHT, albumCoverScale);
	
	/** The invisible JFrame of the GUI. */
	private JFrame frame;
	
	/** The PictureLabel that album covers will be displayed on. */
	private PictureLabel albumCoverLabel = new PictureLabel();
	
	/** The search bar that users can search for songs from Spotify. */
	private JTextField searchBar;
	
	/** The label that song names will be displayed on. */
	private JLabel currentSongLabel = new JLabel();
	
	/** The label that song artists will be displayed on. */
	private JLabel currentArtistsLabel = new JLabel();
	
	/** PictureLabel with searchX of class Images as picture, and button highlighting enabled. */
	private PictureLabel searchX = new PictureLabel(images.searchX, true);
	
	/** PictureLabel that switches between "play" and "pause" BufferedImages from 
	 * class Images as picture, and button highlighting enabled. */
	private PictureLabel play = new PictureLabel(images.play, true);
	
	/** The track progress bar that will update according to the track's current
	 * frame and duration. */
	private PictureLabel trackBar = new PictureLabel(images.blankTrackBar);
	
	/** The JPanel that catches the mouse events to display the close icon. */
	private JPanel anchorHoverPanel;
	
	/** The panel that holds the close button. */
	private JPanel closePanel;
	
	/** The Ellipse that determines if the mouse clicked the close icon */
	private Ellipse2D.Double closeIconShape;
	
	/** Keeps track of if the JFrame is being dragged */
	private boolean frameIsBeingMoved = false;
	
	/** Stores the X mouse coordinate within the frame. */
	private int offSetMouseX = 0;
	
	/** Stores the Y mouse coordinate within the frame. */
	private int offSetMouseY = 0;
	
	/** The current <i>Song</i> object displayed. */
	private Song currentSong;
	
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
		frame.setBounds(screenSize.width - WIDTH - 30, 40, WIDTH, HEIGHT + 50);
		frame.setTitle("Spotify Previewer");
		frame.setIconImage(images.icon);
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
		PictureLabel background = new PictureLabel(images.background);
		background.setLayout(null);
		background.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		background.addMouseMotionListener(new MouseAdapter() {
		     public void mouseDragged(MouseEvent e) {
				//sets frame position when mouse dragged			
				frame.setLocation(e.getXOnScreen() - offSetMouseX,
						e.getYOnScreen() - offSetMouseY);
		     }
		     public void mouseMoved(MouseEvent e) {
		    	 redispatchMouseEvent(closePanel, background, e, true);
//		    	 Component targetComp = closePanel;
//		    	 
//		    	Point sourcePoint = e.getPoint();
//		 		Point targetPoint = SwingUtilities.convertPoint(
//		 		              background,
//		 		              sourcePoint,
//		 		              targetComp);
//		 		
//		 		if (targetPoint.y >= 0 && targetPoint.y <= targetComp.getHeight() &&
//		 				targetPoint.x >= 0 && targetPoint.x <= targetComp.getWidth() ) {
//		 			closePanel.setVisible(true);
//		 		}
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
		albumCoverLabel.setRenderingScale(1.0 / albumCoverScale);
		
		//and create a gray BufferedImage to put in place of album Cover
		BufferedImage blankAlbumBuf = new BufferedImage (54 * albumCoverScale, 
				54 * albumCoverScale, BufferedImage.TYPE_INT_RGB);
		
		Graphics g = blankAlbumBuf.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 54 * albumCoverScale, 54 * albumCoverScale);
		g.dispose();
		
		albumCoverLabel.setPicture(images.blankAlbumCover);
		
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
	        	  	redispatchMouseEvent(searchX, searchBar, e, true);
	          }
	          public void mousePressed(MouseEvent e) {
				  	redispatchMouseEvent(searchX, searchBar, e, true);
			  }
			  public void mouseReleased(MouseEvent e) {
			   		redispatchMouseEvent(searchX, searchBar, e, true);
			  }
			  public void mouseClicked(MouseEvent e) {
				  	redispatchMouseEvent(searchX, searchBar, e, true);
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
				if (!(PlayerLogic.getPlayerState() == PlayerLogic.PLAYING)) {
					PlayerLogic.play();
					play.setPicture(images.pause);
				} else {
					PlayerLogic.pause();
					play.setPicture(images.play);
				}
			}
		});
		
		// -------------------------------------------------------------------------
		
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
		
		/* Creates a essentially see-through panel (must be alpha 1 to be 
		 * considered part of the JFrame) that has an Ellipse shape drawn
		 * onto it that can determine if clicked
		 * */
		
		// The Shape object that will detect the mouse click
		closeIconShape = new Ellipse2D.Double(0, 40, 16, 16);
		
		closePanel = new JPanel () {
			
			@Override
		    protected void paintComponent(Graphics g)
		    {
				// Paints the background color
				super.paintComponent(g);
		        g.setColor( getBackground() );
		        g.fillRect(0, 0, getWidth(), getHeight());
		        
		        // Then, (1) sets up Graphics2D object
		        Graphics2D g2d = (Graphics2D) g;
		        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        		RenderingHints.VALUE_ANTIALIAS_ON);
		        
		        // (2) Paints the ellipse shape closeIconShape
		        g2d.setColor(Color.BLACK);
		        g2d.fill(closeIconShape);
		        
		        // and (3) draws an 'X' on closeIconShape
		        g2d.setColor(Color.WHITE);
		        g2d.setStroke(new BasicStroke(1f));
		        int margin = 5;
		        g2d.drawLine ((int) closeIconShape.x + margin, 						// x,
		        		(int) closeIconShape.y + margin, 							// y,
		        		(int) closeIconShape.width - margin,  						// w,
		        		(int) (closeIconShape.y + closeIconShape.height - margin));	// h
		        
		        g2d.drawLine( (int) closeIconShape.x + margin,						// x,
		        		(int) (closeIconShape.y + closeIconShape.height - margin), 	// y,
		        		(int) closeIconShape.width - margin, 						// w,
		        		(int) closeIconShape.y + margin);							// h
		    }
		};
		closePanel.setBounds(8, 16, 20, 56);
		closePanel.setBackground(new Color(255, 255, 255, 1));
		closePanel.setVisible(false);
		closePanel.addMouseListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				JOptionPane.showConfirmDialog(null, "Hey!", "woking", JOptionPane.CANCEL_OPTION);
				closePanel.setVisible(true);
			}
			public void mouseEntered(MouseEvent e) {
				closePanel.setVisible(true);
			}
			
			public void mousePressed(MouseEvent e) {
				
				// if clicking the close button
				if (closeIconShape.contains(e.getPoint())) {
					System.exit(0);
					
				} else {
					// drags JFrame otherwise
					redispatchMouseEvent(background, closePanel, e, true);
				}
			}
			public void mouseExited(MouseEvent e) {
				closePanel.setVisible(false);
			}
		});
		
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
		layeredPane.add(closePanel, 21);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(layeredPane);
		frame.setVisible(true); 
	}
	
	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	/** Switches and play and pause images on PictureLabel play; <i>true</i> is play,
	 * and <i>false</i> is pause.
	 * 
	 * @param playButton <i>true</i> is play, <i>false</i> is pause.
	 */
	public void switchPlayPauseButton (boolean playButton) {
		if (playButton) {
			play.setPicture(images.play);
		} else {
			play.setPicture(images.pause);
		}
	}
	
	/** Displays the title and album artwork of the song on the player,
	 * but does not play it.
	 * 
	 * @param song The song to display on the player.
	 */
	public void displaySong(Song song) {
		if (song != null) {
			
			String name = song.getName();
			String artists = "";
			
			// Artists are in order of last put in, so iterator runs them backwards
			Iterator<String> it = song.getArtists().iterator();
			String artist = String.valueOf(it.next());
			
			while (it.hasNext()) {
				artists += artist.concat(", ");
				artist = it.next();
			}
			
			artists += artist;
			
			currentSongLabel.setText(name);
			currentArtistsLabel.setText(artists);
            
			albumCoverLabel.setPicture(song.getAlbumCover());
			 
			if (song.getProgress() != 0 && song.getDuration() != 0) {
				display.updateTrackBar(song.getProgress(), 
					song.getDuration());
			}
			
			currentSong = song;
			
		} else {
			System.out.println("No song to display on player.");
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
	public BufferedImage scaleImage(BufferedImage buffI, int WIDTH, int HEIGHT) {
		return images.scaleImage(buffI, WIDTH, HEIGHT);
	}
	
	/** Returns the scale albums are resized down to, with 1 being its
	 * display size, and 4 being 4 times that display size.
	 * 
	 * @return The album cover scale.
	 */
	public int getAlbumCoverScale() {
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
			trackBar.setPicture(images.blankTrackBar);
		}
		
		BufferedImage bImg = images.createTrackBarBufImg(current, duration, false);
		trackBar.setPicture(bImg);
	}
	
	/** Redispatches a MouseEvent to a <b>targetComp</b> from a <b>sourceComp</b>.
	 * 
	 * @param targetComp The component to dispatch the MouseEvent to.
	 * @param sourceComp The component that forwards its MouseEvent.
	 * @param e The MouseEvent.
	 * @param repaint Repaints the <b>targetComp</b> if true.
	 */
	private void redispatchMouseEvent(Component targetComp, Component sourceComp,
			MouseEvent e, boolean repaint) {
		
		Point sourcePoint = e.getPoint();
		Point targetPoint = SwingUtilities.convertPoint(
		              sourceComp,
		              sourcePoint,
		              targetComp);
		
		if (targetPoint.y >= 0 && targetPoint.y <= targetComp.getHeight() &&
				targetPoint.x >= 0 && targetPoint.x <= targetComp.getWidth() ) {
		//The mouse event is probably over the content pane.
		//Find out exactly which component it's over.
		Component component =
		SwingUtilities.getDeepestComponentAt(
		              targetComp,
		              targetPoint.x,
		              targetPoint.y);
		
			if ((component != null)
			&& (component.equals(targetComp))) {
				//Forward events over the targetComponent
				Point componentPoint = SwingUtilities.convertPoint(
				                  sourceComp,
				                  sourcePoint,
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
		
	    if (repaint) {
	        targetComp.repaint();
	    }
	}
}